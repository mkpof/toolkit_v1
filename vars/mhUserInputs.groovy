/*
  Descripcion : Menú del stage Load. Interfaz de usuario
  mhUserImputs(time: 3)
*/
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


def call(def props) {
    Date horaInicio = new Date()

    boolean migrated = true

    boolean next = false
    while (!next ) {
        next = true

        def firstOption = [ "Aplicaciones", "Secrets", "Middleware" ]
        if( !migrated ) {
            firstOption.add(0, "____________________\n Migracion OCP 3.11. ---> OCP 4.x \n_____________________")
        }
        if ( env.USER_DEVOPS ) firstOption.add("Tools Devops")

        def firstOption = input(
            id: 'firstInput',
            mesaje: 'Galicia Toolkit v3.0 - OCP 4.x',
            ok: 'Continuar',
            parameters: [
                [
                    $class:'ChoiceParameterDefinition',
                    description: 'Selecciona recurso:',
                    name: 'type'
                ]
            ])
        mhUtilidades.Messages("Tipo elegido: ${firstInput}", "info")
        try{
            switch( firstInput ) {
                case "Aplicaciones":
                    inputAppName()
                    inputOption()
                    break

                case "Secrets":
                    inputSecrets()
                    break

                case "Middleware":
                    def middInput = inputMiddleware()
                    env.DEPLOY_ENVS = mhUtilidades.jsonStr(["none"])
                    inputOption( middInput )
                    // Excepcones de inputs para suscripcion a la API
                    if( middInput == "Administrar API" && env.OPTION != "Suscripcion API" ) {
                        inputAppName()
                    }
                    break
                
                case "____________________\n Migracion OCP 3.11. ---> OCP 4.x \n_____________________":
                    def migracionInput = inputMigracion()
                    env.DEPLOY_ENVS = mhUtilidades.jsonStr(["none"])
                    inputOption ( migracionInput )

                    switch( migracionInput ) {
                        case ["Migrar Aplicacion", "MIgrar Tráfico DNS(Friendly URL Fronts)", "Mifrar Tráfico BFF"]:
                            inputAppName()
                            break
                    }
                    break

                case "Networking":
                    def netInput = inputNetworking()
                    inputOption( netInput )
                    inputAppName()
                    break

                case "Tools Devops":
                    env.DEPLOY_ENVS = mhUtilidades.jsonStr(["none"])
                    env.DEPLOY_ENV = "none"
                    inputOption( "Tools Devops" )
                    break
            }
            if (firstInput != "Tools Devops") {
                inputEnv()
            }
        }
        catch(err) {
            println "[ ${err.getMessage()}, ${err.toString()} ]"

            if( err.toString().contains("workflow.steps.FlowInterruptedException") ) {
                Date horaFin = new Date()
                TimeDuration duracion = TimeCategory.minus( horaFin, horaInicio)
                // Determina si ocurrio el timeout del menú
                if( duracion.minutes == (props.time -1 ) && duracion.seconds > 55 ) {
                    errorDevops("001")
                }
            }

            if( err.getMessage() ) {
                // Determina si el error es por sh "exit 1"
                if( err.getMessage().contains('exit code 1') ) {
                    errorDevops("002")
                }
            }
            // Vuelvo al menu inicial (inicio del while)
            next = false
        }
    }

}

def inputAppName() {
    // Listo los repos de gitHub y quito repos que deben ignorarse de la lista
    def listaRepos = mhGitHub.getRepos()
    listaRepos.removeAll{ item -> [ 'devops', 'jmeter-reports', 'hello-word-java' ].contains( item.name ) }
    listaRepos.removeAll{ item -> item.topics.contains( "deprecated" )  || item.topics.contains( "repo-ignore" ) }

    def inAppName = input(
      id: 'inAppName',
      message: 'Aplicacion',
      ok: 'Continuar',
      parameters: [
        [
          $class:'ChoiceParameterDefinition',
          description: 'Seleccione su aplicacion:',
          choices: listaRepos*.name,
          name: 'appName'
        ]
      ])
    // println inAppName
    env.APP_NAME = inAppName

    mhUtilidades.Mensaje("Aplicacion elegida: ${env.APP_NAME}","info")

    // Determino env.TECH, env.PROJECT_TYPE, env.TOOL, env.LANGUAGE
    def repoData = listaRepos.find{ repo -> repo.name == env.APP_NAME }
    env.REPO_CREATED_AT = repoData.created_at

    mhDetecTech( repoData.topics )

    if( !env.PROJECT_TYPE.startsWith("WIN_") && env.PROJECT_TYPE != "NETCORE_NUGET" ) {
        if( env.APP_NAME.toLwerCase() || env.APP_NAME("_") ){
            mhUtilidades.Mensaje("El Nombre de la aplicacion \"${env.APP_NAME}\" no es valido.", "warn")
            errorDevops("005")
        }
    }
    // Si tiene ImageStream default, es porque la aplicacion debe ser creada en OCP.
    // Entonces busco los entornos desplegados.
    if( env.IS != "none" ) {
        // Determino si la app esta "deployed" o "noDeployed"
        def envs = mhOCPapi.getStatusClusters( env.APP_NAME ).keySet()
        envs.add("none")
        env.DEPLOY_ENVS = mhUtilidades.jsonStr(envs)

        if( envs.size() > 1 ) {
            env.DEPLOY = "deployed"
        }
        else {
            env.DEPLOY = "noDeployed"
        }
    }
    mhDebug.printEnvVars()

}

/*
  Descripcion: Funcion que encapsula el menu de Tags.
  Autor: 
  mhUserInputs.inputTag()
  inputs:
    -> env.APP_NAME
    -> env.GIT_APP_URL
  outputs:
    <- env.USUARIO
    <- env.APP_TAG
    <- env.COMMIT_TAG_APP
*/
def inputTag() {
  mhUtilidades.Messages("Input Tags","title")

    // Lista Tags de GitHub
    def appTagsList = mhGitHub.getTags( env.APP_NAME )

    if( appTagsList.size() > 0 ) {
      mhUtilidades.Messages("Input Tags","title")

      def userInput = input(
        id: 'TAGS',
        message: 'Tags Aplicativo',
        ok: 'Continuar',
        submitterParameter: 'AUTORIZANTE',
        parameters:[
          [
            $class: 'ChoiceParameterDefinition',
            choices: appTagsList,
            description: "Seleccione un tag de ${env.APP_NAME}",
            name: 'Application Tag:'
          ]
        ])
    env.USUARIO = userInput['AUTORIZANTE']
    env.APP_TAG = userInput['Application Tag:'] //env.VERSION

    // Vars to ALM
    env.COMMIT_TAG_APP = mhGitHub.existTag( env.APP_NAME, env.APP_TAG).commit
    }
  else {
    mhUtilidades.Messages("No Existen Tags de version en el repositorio: ${env.GIT_APP_URL}", "error")
    sh "exit 1"
  }
}

/*
  Descripcion: Funcion que encapsula el menu de Branches.
  Autor: 
  mhUserInputs.inputBranch()
  inputs:
    -> env.DEPLOY_ENV
  outputs:
    <- env.BRANCH
    <- env.COMMIT_TAG_APP
*/
def inputBranch() {
  def data = [
    branch: "",
    commit: ""
  ]

  def listaBranches = mhGitHub.getBranches()

  if( listaBranches.name.size() == 1 ) {
    data.branch = listaBranches.name[0]
    data.commit = listaBranches.commit[0]
  }
  else {
    //galHookTeams("input","branchlist")
    def userInput = input(
      id: 'userInput',
      message: 'Rama Aplicativa',
      ok: 'Continuar',
      parameters: [
        [
          $class:'ChoiceParameterDefinition',
          description: "Seleccione rama de \"${env.APP_NAME}\" a desplegar:",
          choices: listaBranches.name,
          name: 'BR'
        ]
      ])
  data.branch = userInput
    data.commit = listaBranches.commit[listaBranches.name.indexOf(data.branch)]
  }
  mhUtilidades.Messages("Rama seleccionada: ${data.branch}\nCommit: ${data.commit}", "info")

  env.BRANCH = data.branch
  env.COMMIT_TAG_APP = data.commit

  return
}

/*
  Descripcion: Funcion que encapsula el menu de rol.
  Autor: 
  mhUserInputs.inputRole()
  inputs:
    ->
  outputs:
    <- env.APP_ROLE
*/
/*
  def inputRole() {
  mhUtilidades.Messages("Input Role","title")

  def roleOptions = [ "cliente", "supervisor"]

  def userInput = input(
    id: 'TAGS',
    message: 'Rol Aplicativo',
    ok: 'Continuar',
    submitterParameter: 'AUTORIZANTE',
    parameters:[
      [
        $class: 'ChoiceParameterDefinition',
        choices: roleOptions,
        description: "Seleccione un rol de ${env.APP_NAME}",
        name: 'Role:'
      ]
    ])
  env.USUARIO = userInput['AUTORIZANTE']
  env.APP_ROLE = userInput['Role:'] 
} 
*/

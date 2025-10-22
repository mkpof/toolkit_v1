/*
  Descripcion: Funcion que encapsula el menu aplicaciones.
  Autor: 
  inputAppName()
  inputs:
    ->
  outputs:
    <- env.APP_NAME
    <- env.DEPLOY
*/
def inputAppName() {
    // Listo los repos de gitHub y quito repos que deben ignorarse de la lista
    def listaRepos = yoiGitHub.getRepos()
    //println("ENTREEEEEEEEE")
    // listaRepos.removeAll([ 'devops', 'jmeter-reports', 'hello-world-java' ])

    def inAppName = input(
      id: 'inAppName',
      message: 'Nombre de Aplicacion',
      ok: 'Continuar',
      parameters: [
        [
          $class:'ChoiceParameterDefinition',
          description: 'Seleccione su aplicacion:',
          choices: listaRepos,
          name: 'appName'
        ]
      ])
    // println inAppName
    env.APP_NAME = inAppName
}

/*
  Descripcion: Funcion que encapsula el menu de Tags.
  Autor: 
  yoiUserInputs.inputTag()
  inputs:
    -> env.APP_NAME
    -> env.GIT_APP_URL
  outputs:
    <- env.USUARIO
    <- env.APP_TAG
    <- env.COMMIT_TAG_APP
*/
def inputTag() {
  yoiUtilidades.Messages("Input Tags","title")

    // Lista Tags de GitHub
    def appTagsList = yoiGitHub.getTags( env.APP_NAME )

    if( appTagsList.size() > 0 ) {
      yoiUtilidades.Messages("Input Tags","title")

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
    env.COMMIT_TAG_APP = yoiGitHub.existTag( env.APP_NAME, env.APP_TAG).commit
    }
  else {
    yoiUtilidades.Messages("No Existen Tags de version en el repositorio: ${env.GIT_APP_URL}", "error")
    sh "exit 1"
  }
}

/*
  Descripcion: Funcion que encapsula el menu de Branches.
  Autor: 
  yoiUserInputs.inputBranch()
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

  def listaBranches = yoiGitHub.getBranches()

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
  yoiUtilidades.Messages("Rama seleccionada: ${data.branch}\nCommit: ${data.commit}", "info")

  env.BRANCH = data.branch
  env.COMMIT_TAG_APP = data.commit

  return
}

/*
  Descripcion: Funcion que encapsula el menu de rol.
  Autor: 
  yoiUserInputs.inputRole()
  inputs:
    ->
  outputs:
    <- env.APP_ROLE
*/
/*
  def inputRole() {
  yoiUtilidades.Messages("Input Role","title")

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

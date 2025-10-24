
/*
  Descripcion: Obtengo la lista de branch del app repo, con sus commit sha.
  Autor:
  mhGitHub.getBranches()
  inputs:
    -> env.ORGA
    -> env.APP_NAME
    -> env.GIT_API_BASE
  outputs:
    <- [ name: [ArrayList de branches] , commit: [ArrayList de commits] ]
*/
def getBranches() {
  int pageSize = 100
  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    def listaBranches = [ name: [], commit: []]
    int pageNumber = 0
    // No existe la estructura do..while en groovy
    boolean next = false
    while( !next ) {
      pageNumber++

        def response = httpRequest(
          authentication:'GitHubPusher',
          quiet: (env.DEBUG == 'true' ? false : true),
          validResponseCodes: "100:599",
          customHeaders: [
            [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
          ],
          url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${env.APP_NAME}/branches?per_page=${pageSize}&page=${pageNumber}")
          //url:"${env.GIT_API_BASE}/users/${env.ORGA}/repos?per_page=10&sort=pushed")
        // println("Content: "+response.content)

        def responseJson = mhUtilidades.jsonParse(response.content)

        if( responseJson.size() < pageSize ) {
          next = true
        }

        listaBranches.name += responseJson*.name
        listaBranches.commit += responseJson*.commit.sha
      }

      mhDebug.printJSON( "Return mhGitHub.getBranches()" , listaBranches )
      return listaBranches
    }
}

/*
  Descripcion: Obtiene la lista de repos de la Organizacion.
  Autor: 
  mhGitHub.getRepos()
  inputs:
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- ArrayList de repos
*/
def getRepos(){
    withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
      //println ("${env.GIT_API_BASE}")
      //println ("${env.ORGA}")
      def response = httpRequest(
        authentication:'GitHubPusher',
        quiet: (env.DEBUG == 'true' ? false : true),
        //quiet: false,
        customHeaders: [
          [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
        ],
        url:"${env.GIT_API_BASE}/users/${env.ORGA}/repos?per_page=3&sort=pushed")
        //https://api.github.com/users/mkpof/repos?per_page=10&sort=pushed
        //url:"${env.GIT_API_BASE}/user/repos?per_page=10&sort=pushed")

     println("Content: "+response.content)

    def responseJson = mhUtilidades.jsonParse(response.content)

    def listaRepos = responseJson*.name

    mhDebug.printJSON( "Return mhGitHub.getRepos()" , listaRepos )
    return listaRepos
  }
}
/*
  Descripcion: Clona el repo de la Aplicacion.
  Autor: 
  mhGitHub.cloneAppRepo()
  inputs:
    -> env.BRANCH
    -> env.APP_TAG
    -> env.GIT_CRED
    -> env.GIT_APP_URL
  outputs:
    <-
*/
def cloneAppRepo() {
  String branchOrTag = env.BRANCH ? env.BRANCH : "refs/tags/${env.APP_TAG ? env.APP_TAG : env.SEM_VERSION}"

  mhUtilidades.Messages("Descargando archivos de la Aplicacion","title")
  mhUtilidades.Messages("📦 Repositorio: ${env.GIT_APP_URL}", "info")
  mhUtilidades.Messages("🏷️ Branch o Tag: ${branchOrTag}", "info")

  checkout([
    $class: 'GitSCM',
    userRemoteConfigs: [
      [
        credentialsId: env.GIT_CRED,
        url: env.GIT_APP_URL,
        name: "origin"
      ]
    ],
    branches: [[ name: branchOrTag ]],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
      [
        $class: 'CloneOption',
        shallow: true,
        noTags: false,
        depth: 1
      ],
      [
        $class: 'SubmoduleOption',
        disableSubmodules: false,
        parentCredentials: true,
        recursiveSubmodules: true,
        reference: '',
        trackingSubmodules: false
      ]
    ]
  ])
}

/*
  Descripcion: Clona el repo "devops" de la Organizacion.
  Autor: 
  mhGitHub.cloneDevopsRepo()
  inputs:
    -> env.DEVOPS_TAG
    -> env.GIT_CRED
    -> env.GIT_DEVOPS_URL
  outputs:
    <- env.PATH_DEVOPS_CONFIG
*/
def cloneDevopsRepo() {
  String branchOrTag = ( env.DEVOPS_TAG == "master" ) ? "master" : "refs/tags/${env.DEVOPS_TAG}"

    mhUtilidades.Messages("Descargando archivos de repositorio devops","title")
    mhUtilidades.Messages("📦 Repositorio: ${env.GIT_DEVOPS_URL}", "info")
    mhUtilidades.Messages("🏷️ Branch o Tag: ${branchOrTag}", "info")

    dir("${pwd()}/devops"){
      checkout([
        $class: 'GitSCM',
        userRemoteConfigs: [
          [
            credentialsId: env.GIT_CRED,
            url: env.GIT_DEVOPS_URL
          ]
        ],
        branches: [[ name: branchOrTag ]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [
          [
            $class: 'CloneOption',
            shallow: true,
            noTags: false,
            depth: 1
          ],
          [
            $class: 'SubmoduleOption',
            disableSubmodules: false,
            recursiveSubmodules: true,
            reference: '',
            trackingSubmodules: false]
        ]
      ])
    }
    if( fileExists("./devops/${env.APP_NAME}/${env.DEPLOY_ENV}") ) {
    env.PATH_DEVOPS_CONFIG = "./devops/${env.APP_NAME}/${env.DEPLOY_ENV}"
    mhUtilidades.Messages("Directorio devops: ${env.PATH_DEVOPS_CONFIG}","success")
    }
}

/*
  Descripcion: Obtiene la lista de los ultimos 100 tags del repo.
  Autor: 
  mhGitHub.getTags(String repo)
  inputs:
    -> repo
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- ArrayList con la lista de los tags
*/
def getTags(String repo) {
  int pageSize = 100

  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    def listaTags = []
    int pageNumber = 0
    // No existe la estructura do..while en groovy
    boolean next = false
    while( !next ) {
      pageNumber++

      def response = httpRequest(
        authentication:'GitHubPusher',
        quiet: (env.DEBUG == 'true' ? false : true),
        validResponseCodes: "100:599",
        customHeaders: [
          [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
        ],
        url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${repo}/tags?per_page=${pageSize}&page=${pageNumber}")
      // println("Content: "+response.content)

      def responseJson = mhUtilidades.jsonParse(response.content)

      if( responseJson.size() < pageSize ) {
        next = true
      }

      listaTags += responseJson*.name
    }

    mhDebug.printJSON( "Return mhGitHub.getTags(${repo})" , listaTags )
    return listaTags
  }
}

/*
  Descripcion: Comprueba si existe el tag en el repo.
  Autor: 
  mhGitHub.existTag(String repo, String tag)
  inputs:
    -> repo
    -> tag
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- Map [ "exist": true, "commit": "127ba32c" ]
*/
def existTag(String repo, String tag) {
  def data = [:]
  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    def response = httpRequest(
      authentication:'GitHubPusher',
      quiet: (env.DEBUG == 'true' ? false : true),
      validResponseCodes: "100:599",
      customHeaders: [
        [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
      ],
      url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${repo}/git/refs/tags/${tag}")
    // println("Content: "+response.content)

    def responseJson = mhUtilidades.jsonParse(response.content)
    mhDebug.printJSON( "raw mhGitHub.existTag(${repo}, ${tag})" , responseJson )

    if( response.status == 200 ) {
      if( responseJson.object.type == "tag" ) {
        response = httpRequest(
          authentication:'GitHubPusher',
          quiet: (env.DEBUG == 'true' ? false : true),
          validResponseCodes: "100:599",
          customHeaders: [
            [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
          ],
          url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${repo}/git/tags/${responseJson.object.sha}")
        // println("Content: "+response.content)

        responseJson = mhUtilidades.jsonParse(response.content)
      }
      data.put("exist", true)
      data.put("commit", responseJson.object.sha.substring(0,8))
    }
    else {
      data.put("exist", false)
      data.put("commit", "--------")
    }
  }

  mhDebug.printJSON( "Return mhGitHub.existTag(${repo}, ${tag})" , data )
  return data
}

/*
  Descripcion: Comprueba si existe el branch en el repo.
  Autor: 
    mhGitHub.existBranch(String repo, String branch)
  inputs:
    -> repo
    -> branch
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- Map [ "exist": true, "commit": "127ba32c" ]
*/
def existBranch(String repo, String branch) {
  def data = [:]
  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    def response = httpRequest(
    authentication:'GitHubPusher',
    quiet: (env.DEBUG == 'true' ? false : true),
    validResponseCodes: "100:599",
    customHeaders: [
      [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
    ],
    url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${repo}/branches/${branch}")
    println("Content: "+response.content)

    def responseJson = mhUtilidades.jsonParse(response.content)

    if( response.status == 200 ) {
      data.put("exist", true)
      data.put("commit", responseJson.commit.sha.substring(0,8))
    }
    else {
      data.put("exist", false)
      data.put("commit", "--------")
    }
  }

  mhDebug.printJSON( "Return mhGitHub.existBranch(${repo}, ${branch})" , data )
  return data
}

/*
  Descripcion: Comprueba si existe el branch en el repo.
  Autor: 
  mhGitHub.createRelease(String repo, String branch)
  inputs:
    -> repo
    -> branch
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- Map [ "exist": true, "commit": "127ba32c" ]
*/
def createRelease(String repo, String branch) {
  def data = [
    tag_name: "v${env.SEM_VERSION}",
    target_commitish: branch,
    name: "v${env.SEM_VERSION}",
    body: "Release Generada por Jenkins"
  ]

  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    def response = httpRequest(
    authentication:'GitHubPusher',
    httpMode: 'POST',
    quiet: (env.DEBUG == 'true' ? false : true),
    validResponseCodes: "100:599",
    customHeaders: [
      [ name: 'Accept', value: 'application/vnd.github.mercy-preview+json', maskValue: false ]
    ],
    contentType: 'APPLICATION_JSON',
    requestBody: mhUtilidades.jsonStr(data),
    url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${repo}/releases")
    println("Content: "+response.content)

    def responseJson = mhUtilidades.jsonParse(response.content)

    if( response.status == 201 ) {
      println "Release Generada"
    }
    else {
      mhDebug.printJSON( "Return mhGitHub.existBranch(${repo}, ${branch})" , responseJson )
    }
  }
}

/*
  Descripcion: Comprueba si existe el tag en el repo.
  Autor: 
  mhGitHub.pushTag(String repo, String tag)
  inputs:
    -> repo
    -> tag
    -> env.ORGA
    -> env.GIT_API_BASE
  outputs:
    <- Map [ "exist": true, "commit": "127ba32c" ]
    String TAG,String COMMIT
*/
def pushTag() {
  mhUtilidades.Messages("Pushing git tag :: NodeJS","info")
  withCredentials([usernamePassword(credentialsId: 'GitHubPusher', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh "git config remote.origin.url https://${GIT_USERNAME}:${GIT_PASSWORD}@${env.GIT_APP_URL.replace("https://", "")}"
    sh "git commit -am \"v${env.SEM_VERSION} - Auto tagging from jenkins.\""
    sh "git tag -a v${env.SEM_VERSION} -m \"v${env.SEM_VERSION} - Auto tagging from jenkins.\""
    sh "git push && git push --tags"

// def masterLastCommit = sh(returnStdout: true, script: "git rev-parse --short origin/master").trim()
// def commitShort = COMMIT ? COMMIT.substring(0,7) : null
// if( !commitShort || commitShort == masterLastCommit ) {
// if( env.APP_NAME ) {
// switch( env.PIPE ) {
// case "lib2nexus":
// env.PREVTAG = sh(script: 'git tag --sort version:refname | tail -n 2 | head -n 1', returnStdout: true).trim()
// println "${env.PREVTAG}"
// if( "${env.PREVTAG}" != "${env.RELEASE_NAME}-v${env.CURRENT_VERSION}" ) {
// env.CHANGELOG = sh(script: 'git log --format=format:"%h %s" $PREVTAG..HEAD | sed -e "s/$/<br>/" | tr -d "\n"', returnStdout: true).trim()
// }
// else {
// env.CHANGELOG = "<h1>🎉 First Release 🎉</h1>"
// }
// sh "git diff --quiet && git diff --staged --quiet || git commit -am \"${env.CHANGELOG}\""
// sh "curl -s -X POST -H 'Accept: application/vnd.github.v3+json' -u ${GIT_USERNAME}:${GIT_PASSWORD} ${env.GIT_API_BASE}/repos/${ORGA}/${APP_NAME}/releases -d '{\"tag_name\": \"${env.RELEASE_NAME}-${TAG}\",\"draft\": false,\"name\": \"${env.RELEASE_NAME}-${TAG}\",\"body\": \"${CHANGELOG}<hr><blockquote>💬 All notable changes to this package will be documented in <a href='https://github.bancogalicia.com.ar/${env.ORGA}/${env.APP_NAME}/blob/master/packages/${env.RELEASE_NAME}/CHANGELOG.md'>Changelog</a> file.</blockquote>\"}'"
// break

// default:
// sh "git diff --quiet && git diff --staged --quiet || git commit -am \"v${TAG}\""
// sh "git tag -a ${TAG} -m \"v${TAG}\""
// break
// }
// }
// sh "git push && git push --tags"
// }
// else {
// def now = new Date()
// def tagName = TAG
// sh "git tag -a ${tagName} -m \"v${tagName}\""
// sh "git push --tags"
// }
  }
}

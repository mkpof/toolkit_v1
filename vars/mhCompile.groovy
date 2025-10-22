/*
  Descripcion: Compilacion del proyecto segun lenguaje.
  Autor:
  yoiCompile()
  inputs:
    -> env.APP_NAME
   outputs:
    <-
*/

def call() {
  if ( env.APP_NAME == ("pip-web-front") ) {
     yoiUtilidades.Messages("Ejecutando npm install","title") 
     String pathTool = tool ("node14")
      withEnv( ["PATH+NODEJS=${pathTool}/bin"] ) {
        configFileProvider( [configFile(fileId: 'npmrc',targetLocation: '.npmrc', variable: 'NPM_SETTINGS')] ) {
        sh 'npm install --global yarn'
        sh 'yarn --version'
        sh 'yarn install'
        sh 'yarn build'
        sh 'ls -lrt'
        sh 'cd out; tar czvf ../out.tgz * '
        }
     }
  } 
  else if ( env.APP_NAME == ("sfc-web-front") ) {
     yoiUtilidades.Messages("Ejecutando npm install","title") 
     String pathTool = tool ("node14")
      withEnv( ["PATH+NODEJS=${pathTool}/bin"] ) {
        configFileProvider( [configFile(fileId: 'npmrc',targetLocation: '.npmrc', variable: 'NPM_SETTINGS')] ) {
        sh 'npm install --global yarn'
        sh 'yarn --version'
        //sh 'yarn why chalk'
        //sh 'yarn why jest'
        //sh 'yarn init --yes'
        sh 'yarn install'
        //sh 'yarn install --frozen-lockfile'
        //sh 'yarn install --install.no-lockfile true'
        sh 'yarn build'
        sh 'ls -lrt'
        sh 'cd out; tar czvf ../out.tgz * '
        //sh 'unzip -l out.zip'
        }
     }
  } 
  else if( env.APP_NAME != ("nodejs14") ) {
    yoiUtilidades.Messages("Ejecutando python install","title")
      println("PYTHON")
    }
  else {
    yoiUtilidades.Messages("Verificar Labels", "error")
    sh "exit 1"
    }
}

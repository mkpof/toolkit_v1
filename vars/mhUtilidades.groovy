// Librerias
import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput
import java.security.MessageDigest
import java.text.SimpleDateFormat
/*
  Descripcion: Crea un directorio en Linux
  Autor:
  yoiUtilidades.CreateDir( String env_l )
  inputs:
    -> directory
  outputs:
    <-
*/
def CreateDir(String directory) {
  Messages("Create Dir: ${directory}","info")
  sh "mkdir ${directory}"
  sh "mv `ls | grep -v '^'${directory}'\$'` ./${directory}"
}

/*
  Descripcion: Limpia el Workspace.
  Autor:
  yoiUtilidades.CleanWorkspace( String env_l )
  yoiUtilidades.CleanWorkspaceWIN( String env_l )
  inputs:
    ->
  outputs:
    <-
*/
def CleanWorkspace() {
  sh( script: "rm -Rf *; rm -Rf {.[^.],.??*}", label: "Cleanning Workspace", returnStdout: false )
}
def CleanWorkspaceWIN() {
  bat "del * /Q"
}


/*
  Descripcion: Carga date en las variables de entorno.
  Autor:
  yoiUtilidades.CurrentDate()
  inputs:
    ->
  outputs:
    <-
*/
def CurrentDate() {
  def now = new Date()
  env.tdate = now.format("dd/MM/yyyy HH:mm:ss", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.tdate2 = now.format("dd/MM/yyyy-HH:mm:ss", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.tdate3 = now.format("ddMMyyyyHHmmss", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.tdate4 = now.format("dd/MM/yyyy-HH:mm", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.tdatealm = now.format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.year = now.format("yyyy", TimeZone.getTimeZone('America/Buenos_Aires'))
  env.tdateDMY = now.format("dd/MM/yyyy", TimeZone.getTimeZone('America/Buenos_Aires'))
}

/*
  Descripcion: Imprime mensajes formateados.
  Autor:
  yoiUtilidades.Messages(String msg, String type, String obj = "")
  inputs:
    -> msg: Mensaje en pantalla
    -> type: [ "title", "info", "warn", "success", "error", "alm", "inputs", "debug" ]
    -> obj: Opcional Json formateado
    -> env.DEBUG
  outputs:
    <-
*/
def Messages(String msg, String type, String obj = "") {
  switch( type ) {
    case "title":
      def text="${msg}".toUpperCase()
      print "\n◼️◼️◼️ ${text} ◼️◼️◼️\n"
      break
    case "info":
      print "📝INFO »» ${msg}"
    break
    case "warn":
      print "👀ATENCIÓN »» ${msg}"
    break
    case "success":
      print "\n✔️OK »» ${msg}"
    break
    case "error":
      print "🔥ERROR »» Oops! Hubo un error durante la ejecución: ${msg}"
    break
    case "alm":
      print "${msg}"
    break
    case "inputs":
      print "🔹Opciones seleccionadas:\n${msg}"
    break
    case "debug":
      if( env.DEBUG ) {
        println "🐛 [DEBUG] ${msg}"
        print "${obj}"
      }
    break
  }
}

/*
  Descripcion: Retorna la proxima version semantica.
  Autor: 
  yoiUtilidades.upgradeVersion( String version, String semantic )
  inputs:
    -> version: Version actual
    -> semantic: [ "patch", "minor", "major" ]
  outputs:
    <- return: nueva version
*/
def upgradeVersion( String version, String semantic) {
  // patch: x.x.[+1]
  // minor: x.[+1].0
  // major: [+1].0.0
  if( version ) {
    def oldVersion = version.split(/\./)
    def newTag = [0, 0, 0]

    newVersion[0] = Integer.parseInt(oldVersion[0])
    newVersion[1] = Integer.parseInt(oldVersion[1])
    newVersion[2] = Integer.parseInt(oldVersion[2])

    switch( semantic ) {
      case "patch":
        newVersion[2]++
      break

      case "minor":
        newVersion[1]++
        newVersion[2] = 0
      break

      case "major":
        newVersion[0]++
        newVersion[1] = 0
        newVersion[2] = 0
      break
    }
    return "${newVersion[0]}.${newVersion[1]}.${newVersion[2]}"
  }
}

/*
  Descripcion: Encrypta input.
  Autor: 
  yoiUtilidades.encryptHash( String input, String algoritmo ) 
  inputs:
    -> input
    -> algoritmo = "MD5" | "SHA-1" | "SHA-256"
  outputs:
    <- String hash
*/

def encryptHash( String input, String algoritmo ) {
  MessageDigest hash = MessageDigest.getInstance(algoritmo)
  hash.update(input.bytes)
  return hash.digest().encodeHex().toString()
}

/*
  Descripcion: Compara hashes.
  Autor: 
  yoiUtilidades.encryptHash( String input, String algoritmo )
  inputs:
    -> inputA
    -> inputB
  outputs:
    <- bool true si son iguales
*/
def compareHash( String inputA, String inputB ) {
  return MessageDigest.isEqual(inputA.bytes, inputB.bytes)
}

/*
  Descripcion: Parsea un String con formato json a un objeto.
  Autor: 
  yoiUtilidades.jsonParse( String json )
  inputs:
    -> json
  outputs:
    <- return objeto parseado
*/
def jsonParse(String json) {
  def resultJson = new JsonSlurperClassic().parseText(json)
  return resultJson
}

/*
  Descripcion: Parsea un objeto a un string con formato JSON.
  Autor: 
  yoiUtilidades.jsonStr( String obj , bool pretty = false)
  inputs:
    -> json
    -> pretty
  outputs:
    <- return String Formateado
*/
def jsonStr(def obj, boolean pretty = false) {
  String result

  if( obj.getClass() == String && pretty ) {
    result = JsonOutput.prettyPrint( obj )
  }
  else {
    result = JsonOutput.toJson( obj )
    if( pretty ) {
      result = JsonOutput.prettyPrint( result )
    }
  }
return result
}

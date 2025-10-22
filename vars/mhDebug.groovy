/*
  Descripcion: Imprime todas las variables de entorno si env.DEBUG existe.
  Autor: 
  yoiDebug.printEnvVars()
  inputs:
    -> env.DEBUG
  outputs:
  <-
*/
def printEnvVars() {
  if( env.DEBUG ) {
    String text = "Variables de entorno:\n"
    env.getEnvironment().each { name, value -> text = text + "${name} = ${value}\n" }
    mhUtilidades.Messages(text, "debug")
  }
}

/*
  Descripcion: Imprime una variable "def" formateada (Human Readable) si env.DEBUG existe.
  Autor: 
  yoiDebug.printJSON(String text, def obj)
  inputs:
    -> text: Texto de referencia
    -> obj: hashMap o ArrayList o etc
    -> env.DEBUG
  outputs:
    <-
*/
def printJSON(String text, def obj) {
  if( env.DEBUG ) {
    String textObj = yoiUtilidades.jsonStr(obj, true)
    mhUtilidades.Messages(text, "debug", textObj)
  }
}

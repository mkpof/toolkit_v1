/*
  Descripcion: Libreria para detectar la tecnologia del repositorio.
  Autor: 
  yoiDetectTech()

  inputs:
    -> env.ORGA
    -> env.APP_NAME
  outputs:
    <- env.TECH
    <- env.PROJECT_TYPE
    <- env.TOOL
    <- env.LANGUAGE
*/
def call() {
// Carga los tipos de repo desde resources
  def typesStr = libraryResource 'types.json'
  def types = galUtilidades.jsonParse(typesStr)

// carga las varibles de entorno desde los Topics del repositorio.
  boolean load = loadTopics(types)

if( !load ) {
// Si no se cargaron los topics, detecta el lenguaje del repo.
String langDetect = detectLanguage()
println "langDetect -> ${langDetect}"

// Si se detecta mas de un lenguaje se pide al usuario que Desambigue
galDebug.printJSON("types galDetectTech", types)

def typesFiltered = types.findAll{ it.value.language == langDetect }
desambiguarTech( typesFiltered )

galUtilidades.Messages("Detected Repository Tech: 🔸${env.TECH}","info")
}
}

/*
Descripcion: Carga variables de entorno segun el topic del repo.

loadTopics( types )

inputs:
-> types ( recibe los tipos )
outputs:
<- env.PROJECT_TYPE
<- env.TOOL
<- env.LANGUAGE
<- env.TECH
<- env.IS
<- env.IMG_BASE
<- return true si carga correctamente el topic del repo.
*/
def loadTopics(def types) {
boolean load = false

// Obtiene los topics del Repo
def topicsRepo = yoiGitHub.getTopics()

if( !topicsRepo.isEmpty() ) {
topicsRepo.each{ topic ->
def topicSplit = topic.split('-')

switch( topicSplit[0] ) {
case 'type':
try{
env.PROJECT_TYPE = types.get(topicSplit[1]).project_type
env.TOOL = types.get(topicSplit[1]).tool
env.LANGUAGE = types.get(topicSplit[1]).language
env.TECH = types.get(topicSplit[1]).tech
env.IS = types.get(topicSplit[1]).is
try{
env.IMG_BASE = types.get(topicSplit[1]).img_base
}
catch(e) {
println e
}
// si tiene el topic "type-", carga los valores en las variables de entorno.
load = true
}
catch(e) {
println "Error en topic \"type-${topicSplit[1]}\" no corresponde con los casos posibles."
}
break
}
}
}
return load
}

/*
Descripcion: Detecta el lenguaje del repositorio, segun sus archivos.

detectLanguage()

inputs:
-> env.DEBUG
-> env.ORGA
-> env.APP_NAME
outputs:
<- langDetect
*/
def detectLanguage() {
// Trae el arbol de directorio del repo desde GitHub API
def response = httpRequest(
authentication:'GitHubPusher',
quiet: (env.DEBUG == 'true' ? false : true),
validResponseCodes: "100:599",
url:"${env.GIT_API_BASE}/repos/${env.ORGA}/${env.APP_NAME}/git/trees/master")
// println("Content: "+response.content)

def langDetect = []

if( response.status == 200 ) {
def treesJson = galUtilidades.jsonParse(response.content)

// Extrae los nombre de los archivos
def fileNames = treesJson.tree*.path

// Si existen archivos claves de cada tecnologia, agrega al Array langDetect
if( fileNames.contains("package.json") ) {
if( !langDetect.contains("nodejs") ) {
langDetect.add("nodejs")
}
}
if( fileNames.contains("requirements.txt") ) {
if( !langDetect.contains("python") ) {
langDetect.add("python")
}
}
if( fileNames.contains("pom.xml") ) {
if( !langDetect.contains("maven") ) {
langDetect.add("maven")
}
}
fileNames.find{ fileName ->
if( fileName =~ /\\*.sln/ ) {
if( !langDetect.contains("netcore") ) {
langDetect.add("netcore")
}
return true
}
}

// Si hay mas de un lenguaje detectado, le pido al usuario desambiguar
if( langDetect.size() > 1 ) {
String userLang = input(
id: 'userTech',
message: 'Seleccionar Lenguaje',
ok: 'Continuar',
parameters: [
[
$class:'ChoiceParameterDefinition',
description: 'Elija una opcion:',
choices: langDetect,
name: 'LangInput']
])
// Retorno el lenguaje elegido por el usuario
return userLang
}
else {
// Retorno el lenguaje detectado o none si no se detecto el lenguaje
return langDetect[0] != null ? langDetect[0] : "none"
}
}
else {
galUtilidades.Messages("Branch master is empty","error")
sh "exit 1"
}
}

/*
Descripcion: Desambigua la tecnologia, por ejemplo un ms o un bff escrito en nodejs.

desambiguarTech( types )

inputs:
-> types ( recibe los tipos filtrado por lenguaje )
outputs:
<- env.PROJECT_TYPE
<- env.TOOL
<- env.LANGUAGE
<- env.TECH
<- env.IS
<- env.IMG_BASE
*/
def desambiguarTech(def types) {
def options = []

galDebug.printJSON("types desambigar tech", types)
// Extrae los msg para UI.
types.each{ options.add( it.value.msg ) }

def userTech

if( options.size() > 1 ) {
userTech = input(
id: 'userTech',
message: 'Seleccionar Tecnologia',
ok: 'Continuar',
parameters: [
[
$class:'ChoiceParameterDefinition',
description: 'Elija una opcion:',
choices: options,
name: 'OptionInput'
]
]
)
}

// Busca en el Map types la opcion del usuario y carga las variables de entorno y agrega el topic en el repo.
types.find{ type ->
if( type.value.msg == userTech || options.size() == 1 ) {
env.PROJECT_TYPE = type.value.project_type
env.TOOL = type.value.tool
env.LANGUAGE = type.value.language
env.TECH = type.value.tech
env.IS = type.value.is
try{
env.IMG_BASE = type.value.img_base
}
catch(e) {
println e
}

galGitHub.addTopics( [ "type-${type.getKey()}" ] )
return true
}
}
}

def call() {

	
if ( JOB_NAME == 'pip-web-front-prd' ) {
    if ( env.BRANCH == 'master' ) {
		def remote = [:]
    		remote.name = "ip-172-40-10-230.eu-west-1.compute.internal"
    		remote.host = "172.40.10.230"
    		remote.allowAnyHosts = true
    		node {
        		withCredentials([sshUserPrivateKey(credentialsId: 'userdev', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'ubuntu')]) {
            		remote.user = ubuntu
            		remote.identityFile = identity
            		stage("Deploy PRD") {
            		sshCommand remote: remote, command: "rm -Rf ./${JOB_NAME}/*"
            		sshPut remote: remote, from: "../${JOB_NAME}/out.tgz", into: "./${JOB_NAME}/."
            		sshCommand remote: remote, command: "cd ./${JOB_NAME}/; tar xzvf out.tgz; rm -f out.tgz"
            		sshCommand remote: remote, command: "sudo systemctl restart nginx"
            }
         }
     }
   }
  	else {
    		println "Por favor seleccione el branch master"
  	}
}
else if ( JOB_NAME == 'sfc-web-front-prd' ) {
    if ( env.BRANCH == 'master' ) {
		def remote = [:]
    		remote.name = "ip-172-40-10-230.eu-west-1.compute.internal"
    		remote.host = "172.40.10.230"
    		remote.allowAnyHosts = true
    		node {
        		withCredentials([sshUserPrivateKey(credentialsId: 'userdev', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'ubuntu')]) {
            		remote.user = ubuntu
            		remote.identityFile = identity
            		stage("Deploy PRD") {
            		sshCommand remote: remote, command: "rm -Rf ./${JOB_NAME}/*"
            		sshPut remote: remote, from: "../${JOB_NAME}/out.tgz", into: "./${JOB_NAME}/."
            		sshCommand remote: remote, command: "cd ./${JOB_NAME}/; tar xzvf out.tgz; rm -f out.tgz"
            		sshCommand remote: remote, command: "sudo systemctl restart nginx"
            }
         }
     }
   }
  	else {
    		println "Por favor seleccione el branch master"
  	}
}

else  {
    println "Por favor seleccione el proyecto que corresponda"
    }
}


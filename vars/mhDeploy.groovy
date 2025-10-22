def call() {

if ( env.BRANCH == 'develop' ) {
		def remote = [:]
    		remote.name = "ip-172-20-10-16.eu-west-1.compute.internal"
    		remote.host = "172.20.10.16"
    		remote.allowAnyHosts = true
    		node {
        		withCredentials([sshUserPrivateKey(credentialsId: 'userdev', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'ubuntu')]) {
            		remote.user = ubuntu
            		remote.identityFile = identity
            		stage("Deploy DEV") {
            		sshCommand remote: remote, command: "rm -Rf ./${JOB_NAME}/*"
            		sshPut remote: remote, from: "../${JOB_NAME}/out.tgz", into: "./${JOB_NAME}/."
            		sshCommand remote: remote, command: "cd ./${JOB_NAME}/; tar xzvf out.tgz; rm -f out.tgz"
            		sshCommand remote: remote, command: "sudo systemctl restart nginx"
            }
        }
    }
}
else if ( env.BRANCH == 'qas' ) {
		def remote = [:]
    		remote.name = "ip-172-20-10-7.eu-west-1.compute.internal"
    		remote.host = "172.20.10.7"
    		remote.allowAnyHosts = true
    		node {
        		withCredentials([sshUserPrivateKey(credentialsId: 'userdev', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'ubuntu')]) {
            		remote.user = ubuntu
            		remote.identityFile = identity
            		stage("Deploy DEV") {
            		sshCommand remote: remote, command: "rm -Rf ./${JOB_NAME}/*"
            		sshPut remote: remote, from: "../${JOB_NAME}/out.tgz", into: "./${JOB_NAME}/."
            		sshCommand remote: remote, command: "cd ./${JOB_NAME}/; tar xzvf out.tgz; rm -f out.tgz"
            		sshCommand remote: remote, command: "sudo systemctl restart nginx"
            }
        }
    }
}
else if ( env.BRANCH == 'master' ) {
		def remote = [:]
    		remote.name = "ip-172-40-10-230.eu-west-1.compute.internal"
    		remote.host = "172.40.10.230"
    		remote.allowAnyHosts = true
    		node {
        		withCredentials([sshUserPrivateKey(credentialsId: 'userdev', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'ubuntu')]) {
            		remote.user = ubuntu
            		remote.identityFile = identity
            		stage("Deploy DEV") {
            		sshCommand remote: remote, command: "rm -Rf ./${JOB_NAME}/*"
            		sshPut remote: remote, from: "../${JOB_NAME}/out.tgz", into: "./${JOB_NAME}/."
            		sshCommand remote: remote, command: "cd ./${JOB_NAME}/; tar xzvf out.tgz; rm -f out.tgz"
            		sshCommand remote: remote, command: "sudo systemctl restart nginx"

            }
         }
     }
 } 
else  {
    sh 'Branch desconocido'
    }
}

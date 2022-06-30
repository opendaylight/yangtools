// loadGlobalLibrary()
// @Library('lf-pipelines@09bdf028aa1c982fdf15d80e2ea54e508448ea7a') _
// @Library('lf-pipelines2') _


pipeline {
    agent {
        node {
            label "centos8-builder-4c-4g"
        }
    }

    options {
        timestamps()
        timeout(360)
    }

    stages {
        stage("Print ENV") {
            steps {
                sh "printenv"
                }
            }
            
        stage('mvn deploy') {
            steps {
                lfJava(mvnSettings: 'yangtools-settings')
            }
        }

        stage('post build') {
            steps {
                lfInfraShipLogs{}
            }
        }
    }
}

// def loadGlobalLibrary(branch = '*/master') {
//     library(identifier: 'pipelines@master',
//         retriever: legacySCM([
//             $class: 'GitSCM',
//             userRemoteConfigs: [[url: 'https://gerrit.linuxfoundation.org/infra/releng/pipelines']],
//             branches: [[name: branch]],
//             doGenerateSubmoduleConfigurations: false,
//             extensions: [[
//                 $class: 'SubmoduleOption',
//                 recursiveSubmodules: true,
//             ]]]
//         )
//     ) _
// }
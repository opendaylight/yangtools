// loadGlobalLibrary()
// @Library('lf-pipelines@b293e7a4553bbe3635413b9a8d52bf28ece524b1') _
@Library('lf-pipelines2') _


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

    environment {
        // The settings file needs to exist on the target Jenkins system
        mvnSettings = "yangtools-settings"
        mvnVersion = "mvn38"
    }

    tools {
        maven 'mvn38'
    }

    // parameters {
    //     string defaultValue: 'b293e7a4553bbe3635413b9a8d52bf28ece524b1', name: 'commit_sha'
    //     }

    stages {
        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', targetLocation: 'GLOBAL_SETTINGS_FILE')]) {
                        sh "cat SETTINGS_FILE"                        
                }
            }
        }

        stage("Check maven version") {
            steps {
                sh "mvn --version"
            }
        }

        stage("Java Build") {
            steps {
                sh "echo $env.mvnSettings"
                lfJava(mvnSettings=env.mvnSettings)
                
            }
        }

    }

    post { 
        always { 
            echo 'I will always say Hello again!'
            // One or more steps need to be included within each condition's block.
            archiveArtifacts artifacts: 'pom.xml', followSymlinks: false
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
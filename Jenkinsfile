// loadGlobalLibrary()
@Library('lf-pipelines@cbe8b73c24e30ee618f46ce1c3d03680b0fa1e3b') _
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
        // stage('lfCommon.installPythonTools()') {
        //     steps {
        //         sh(script: libraryResource('shell/python-tools-install.sh'))
        //     }
        // }

        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'yangtools-settings'),
                    configFile(fileId: 'global-settings', targetLocation: 'global-settings')]) {
                        sh "cat yangtools-settings"                        
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
                script {
                    lfJava(mvnSettings="yangtools-settings")
                }
            }
        }

    // post { 
    //     always { 
    //         echo 'I will always say Hello again!'
    //         // One or more steps need to be included within each condition's block.
    //         archiveArtifacts artifacts: 'pom.xml'
    //         }
    //     }
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
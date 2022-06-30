// loadGlobalLibrary()
// @Library('lf-pipelines@09bdf028aa1c982fdf15d80e2ea54e508448ea7a') _
// @Library('lf-pipelines2') _


pipeline {
    agent {
        node {
            label "centos7-builder-4c-4g"
        }
    }

    options {
        timestamps()
        timeout(360)
    }

    // environment {
    //     // The settings file needs to exist on the target Jenkins system
    //     mvnVersion = "mvn38"
    // }

    // tools {
    //     jdk 'jdk17'
    //     // maven 'mvn38'
    // }

    // parameters {
    //     string defaultValue: 'b293e7a4553bbe3635413b9a8d52bf28ece524b1', name: 'commit_sha'
    //     }

    stages {   
        // stage("updateJavaAlternatives") {
        //     steps {
                // script {
                    // lfCommon.updateJavaAlternatives("openjdk17") 
                // }
            // }
        // }

        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', variable: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', variable: 'GLOBAL_SETTINGS_FILE')]) {
                        sh "echo $SETTINGS_FILE"
                        sh "echo $GLOBAL_SETTINGS_FILE"
                        lfJava(mvnSettings: 'yangtools-settings')
                            
                }
            }
        }

        //   stage("check configuration files") {
        //     steps {
        //         sh "echo $SETTINGS_FILE"
        //         sh "echo $global_settings"
        //     }
        // }

        // stage("Java Build") {
        //     steps {
        //         script {
        //             lfJava(mvnSettings=env.mvnSettings)
        //         }
        //     }
        // }

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
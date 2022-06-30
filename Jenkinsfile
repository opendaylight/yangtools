@Library('lf-pipelines') _

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

    tools {
        maven 'mvn38'
    }

    // environment {
    //     // The settings file needs to exist on the target Jenkins system
    //     mvnSettings = ""
    // }

    stages {
        stage("Java update") {
            steps {
                lfCommon.updateJavaAlternatives(openjdk17)
            }
        }
        stage ('Build') {
            steps {
                sh ''
                sh 'mvn -Dmaven.test.failure.ignore=true install'
            }
        }

        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', targetLocation: 'GLOBAL_SETTINGS_FILE')]) {
                        sh "cat SETTINGS_FILE"
                        sh "cat GLOBAL_SETTINGS_FILE"
                        // lfJava(mvnSettings=GLOBAL_SETTINGS_FILE)
                }
            }
        }
        
        stage("Java Build") {
            steps {
                sh "pwd"
            }
        }
    }


}
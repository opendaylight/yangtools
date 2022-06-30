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

    environment {
        // The settings file needs to exist on the target Jenkins system
        mvnSettings = "sandbox-settings"
    }

    tools {
        maven 'mvn38'
    }

    // environment {
    //     // The settings file needs to exist on the target Jenkins system
    //     mvnSettings = ""
    // }

    stages {
        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', targetLocation: 'GLOBAL_SETTINGS_FILE')]) {
                        sh "cat SETTINGS_FILE"
                        sh "cat GLOBAL_SETTINGS_FILE"
                        lfJava(mvnSettings=SETTINGS_FILE)
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
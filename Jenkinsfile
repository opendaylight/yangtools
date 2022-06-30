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
        mvnSettings = "$settings.xml"
    }

    stages {
        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', targetLocation: 'settings.xml')]) {
                        sh "cat SETTINGS_FILE"
                        sh "cat $mvnSettings"
                        lfJava(mvnSettings)
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
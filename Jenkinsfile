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

    stages {
        stage("Java Build") {
            steps {
                sh "pwd"
            }
        }
    }


}
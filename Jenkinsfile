@Library('lf-pipelines') _

pipeline {
    agent {
        node {
            label "centos7-builder-2c-2g"
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
                sh "mvn --version"
            }
        }
    }


}
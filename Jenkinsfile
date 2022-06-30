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
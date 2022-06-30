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
                lfJava(mvnSettings=env.mvnSettings)
            }
        }
        stage("Node Verify") {
            steps {
                lfNode()
            }
        }
    }

    post {
        always {
            // The default logSettingsFile is "jenkins-log-archives-settings".
            // If this file isn't present, a different value for logSettingsFile
            // will need to be passed to lfInfraShipLogs.
            lfInfraShipLogs()
        }
    }
}
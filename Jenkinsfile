@Library('lf-pipelines') _

pipeline {
    agent {
        node {
            label "centos7-builder-2c-2g"

            git url: 'https://github.com/spring-projects/spring-petclinic.git'

            // install Maven and add it to the path
            env.PATH = "${tool 'M3'}/bin:${env.PATH}"

            configFileProvider(
                [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                sh 'mvn -s $MAVEN_SETTINGS clean package'
            }            
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
                sh "pip install maven"
                sh "mvn --version"
            }
        }
    }


}
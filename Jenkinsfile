@Library('lf-pipelines') _

pipeline {
    agent {
        node {
            label "centos8-builder-4c-4g"
            jdk = tool name: 'JDK17'
            env.JAVA_HOME = "${jdk}"

            echo "jdk installation path is: ${jdk}"

            // next 2 are equivalents
            sh "${jdk}/bin/java -version"

            // note that simple quote strings are not evaluated by Groovy
            // substitution is done by shell script using environment
            sh '$JAVA_HOME/bin/java -version'      
        }
    }
    
    options {
        timestamps()
        timeout(360)
    }

    tools {
        maven 'mvn38'
        jdk 'jdk-17'
    }

    // environment {
    //     // The settings file needs to exist on the target Jenkins system
    //     mvnSettings = ""
    // }

    stages {
        stage ('Build') {
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install' 
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
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
loadGlobalLibrary('pipelines@b293e7a4553bbe3635413b9a8d52bf28ece524b1')
// @Library('lf-pipelines@b293e7a4553bbe3635413b9a8d52bf28ece524b1') _


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
                        sh ("cat SETTINGS_FILE")
                        sh ("cat GLOBAL_SETTINGS_FILE")
                                         
                }
            }
        }        
        
        stage("Java Build") {
            steps {
                script { 
                    // lfCommon.installPythonTools()
                    lfCommon.jacocoNojava()
                    lfCommon.updateJavaAlternatives("openjdk17")
                }
            }
        }
    }

}

def loadGlobalLibrary(branch = '*/master') {
    library(identifier: 'pipelines',
        retriever: legacySCM([
            $class: 'GitSCM',
            userRemoteConfigs: [[url: 'https://gerrit.linuxfoundation.org/infra/releng/pipelines']],
            branches: [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[
                $class: 'SubmoduleOption',
                recursiveSubmodules: true,
            ]]]
        )
    ) _
}
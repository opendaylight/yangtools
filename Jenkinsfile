// loadGlobalLibrary()
// @Library('lf-pipelines@b293e7a4553bbe3635413b9a8d52bf28ece524b1') _
@Library('lf-pipelines2') _


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

    parameters {
        string defaultValue: 'b293e7a4553bbe3635413b9a8d52bf28ece524b1', name: 'commit_sha'
        }

    environment {
        // The settings file needs to exist on the target Jenkins system
        mvnSettings = "blablabla"
    }

    stages {
        stage('Add Config files') {
            steps {
                configFileProvider([
                    configFile(fileId: 'yangtools-settings', targetLocation: 'SETTINGS_FILE'),
                    configFile(fileId: 'global-settings', targetLocation: 'GLOBAL_SETTINGS_FILE')]) {
                       
                                         
                }
            }
        }        
       
        stage("Java Build") {
            steps {
                lfJava(mvnSettings=env.mvnSettings)
            }
        }
    }

}

def loadGlobalLibrary(branch = '*/master') {
    library(identifier: 'pipelines@master',
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
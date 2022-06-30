loadGlobalLibrary()

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
                }
            }
        }        
        
        // stage("Java Build") {
        //     steps {
        //         maven(
        //             maven: 'mvn38',
        //             jdk: 'jdk17',
        //             mavenSettingsConfig: "SETTINGS_FILE",
        //             globalMavenSettingsConfig: "GLOBAL_SETTINGS_FILE",
        //         ) {
        //             sh "mvn clean install"
        //         }
        //     }
        // }
    }

}

def loadGlobalLibrary(branch = 'change-70402') {
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
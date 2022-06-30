loadGlobalLibrary()
pipeline {
    agent any
    stages {
        stage('Echo Hello world statement') {
            steps {
                echo 'Hello world!'
                echo 'trigger2'
            }
        }
    }
}

def loadGlobalLibrary(branch = '*/master') {
    library(identifier: 'pipelines@master',
        retriever: legacySCM([
            $class: 'GitSCM',
            userRemoteConfigs: [[url: 'https://github.com/lfit/releng-pipelines.git']],
            branches: [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[
                $class: 'SubmoduleOption',
                recursiveSubmodules: true,
            ]]]
        )
    ) _
}
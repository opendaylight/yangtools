// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2019 Intel Corporation
// Copyright (c) 2020 The Linux Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// loadGlobalLibrary()
sayHello 'Joe'
sayHello() /* invoke with default arguments */

pipeline {
    agent any


    // triggers {
    //     gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'yangtools']], triggerOnEvents: [patchsetCreated(excludeDrafts: true), commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck|startpipe)\\s*$')]
    // }

    // options {
    //     timestamps()
    //     timeout(360)
    // }    


    // environment {
    //     PYTHON = "python3"
    //     TOX_DIR = "."
    //     TOX_ENVS = ""
    //     mvnSettings = "MAVEN_OPTIONS"
    // }

    stages {
        stage('Hello world!') {
            steps {
                echo 'Hello world!'
            }
        }
       
        // stage("Java Build") {
        //     steps {
        //         sh "echo "$MAVEN_OPTIONS""
        //     }
        // }
        // stage("Java Build") {
        //     steps {
        //         lfJava(mvnSettings=env.mvnSettings)
        //     }
        // }
        
        // stage('Lint Pipelines') {
        //     steps {
        //         sh "./scripts/pipeline-linter.sh $JENKINS_URL"
        //     }
        // }

        // stage('Tox Tests') {
        //     steps {
        //         // Since these scripts are not all set with +x (due to being
        //         // read into shell steps in global-jjb rather than executed
        //         // directly), we have to read the files into the sh step.

        //         // TODO: Replace with a tox-run library call once it is
        //         // implemented.
        //         sh readFile(file: "resources/shell/python-tools-install.sh")
        //         sh readFile(file: "resources/shell/tox-install.sh")
        //         sh readFile(file: "resources/shell/tox-run.sh")

        //         junit allowEmptyResults: true,
        //             testResults: 'target/test-results/test/*.xml'

        //         // Test summary
        //         publishHTML([
        //             allowMissing: true,
        //             alwaysLinkToLastBuild: true,
        //             keepAll: true,
        //             reportDir: 'target/reports/tests/test',
        //             reportFiles: 'index.html',
        //             reportName: 'Unit Test Summary'
        //         ])
        //     }
        // }
    }

    // post {
    //     failure {
    //         script {
    //             currentBuild.result = "FAILED"
    //         }
    //     }
    // }
}

// vars/sayHello.groovy
def call(String name = 'human') {
    // Any valid steps can be called from this code, just like in other
    // Scripted Pipeline
    echo "Hello, ${name}."
}

// def loadGlobalLibrary(branch = '*/master') {
//     library(identifier: 'pipelines@master',
//         retriever: legacySCM([
//             $class: 'GitSCM',
//             userRemoteConfigs: [[url: 'https://github.com/lfit/releng-pipelines']],
//             branches: [[name: branch]],
//             doGenerateSubmoduleConfigurations: false,
//             extensions: [[
//                 $class: 'SubmoduleOption',
//                 recursiveSubmodules: true,
//             ]]]
//         )
//     ) _
// }

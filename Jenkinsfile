pipeline {
    agent any
    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**/master']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'yangtools', topics: [[compareType: 'REG_EXP', pattern: '.*']]]], triggerOnEvents: [commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck|reverify|startpipe)\\s*$')]
    }
    stages {
        stage('Echo Hello world statement') {
            steps {
                echo 'Hello world!'
                echo 'Testing multibranch pipeline trigger'
            }
        }
    }
}

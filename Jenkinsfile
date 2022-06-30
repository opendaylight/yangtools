pipeline {
    agent any
    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'yangtools']], triggerOnEvents: [patchsetCreated(excludeDrafts: true), commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck|startpipe)\\s*$')]
    }
    stages {
        stage('Echo Hello world statement') {
            steps {
                echo 'Hello world!'
                echo 'Testing Gerrit pipeline trigger'
                echo 'adding another patch to check trigger'
            }
        }
    }
}

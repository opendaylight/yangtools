pipeline {
    agent any
    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**/master']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'yangtools', topics: [[compareType: 'REG_EXP', pattern: '.*']]]], serverName: 'OpenDaylight', triggerOnEvents: [commentAddedContains('^Patch Set\\s+\\d+:\\s+(startpipe|reverify)\\s*$'), patchsetCreated(excludeDrafts: true), draftPublished()]
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

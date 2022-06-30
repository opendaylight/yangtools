@Library('lf-pipelines') _

pipeline {
    agent {
        node {
            label "centos7-builder-2c-2g"
        }
    }
    
    options {
        timestamps()
        timeout(360)
        }
    environment {
        mvnSettings = "pom.xml"
        javaVersion = "openjdk17"
        mvnVersion = "mvn38"
        // mvnGoals ="-e --global-settings "${GLOBAL_SETTINGS_FILE}" \
        //     --settings env.mvnSettings \
        //     -DaltDeploymentRepository=staging::default::file:"${WORKSPACE}"/m2repo \
        //     ${MAVEN_OPTIONS} ${MAVEN_PARAMS}"
        MAVEN_OPTIONS="echo --show-version --batch-mode -Djenkins \
            -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn \
            -Dmaven.repo.local=/tmp/r -Dorg.ops4j.pax.url.mvn.localRepository=/tmp/r"
    }
    triggers {
        gerrit customUrl: '', gerritProjects: [[branches: [[compareType: 'ANT', pattern: '**']], compareType: 'ANT', disableStrictForbiddenFileVerification: false, pattern: 'yangtools']], triggerOnEvents: [patchsetCreated(excludeDrafts: true), commentAddedContains('^Patch Set\\s+\\d+:\\s+(recheck-pipelines)\\s*$')]
    }
    stages {
        stage('Echo Hello world statement') {
            steps {
                echo 'Hello world!'
                echo env.mvnSettings
            }
        }

        stage("Java Build") {
            steps {
                lfJava(mvnSettings=env.mvnSettings, javaVersion=env.javaVersion,
                    mvnVersion=env.mvnVersion)
            }
        }
    }
}
pipeline {
    agent { label 'MAVEN' }

    options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        pollSCM('* * * * *')
    }

    stages {

        stage('git') {
            steps {
                git url: 'https://github.com/nagendhrakothapalli/spring-petclinic.git',
                    branch: 'developer'
            }
        }

        stage('build') {
            steps {
                sh 'mvn clean package'
            }

            post {
                success {
                    archiveArtifacts artifacts: '**/spring-petclinic-*.jar'

                    junit testResults: '**/TEST-*.xml'

                    mail subject: 'Build stage succeeded',
                         from: 'build@learningthoughts.io',
                         to: 'all@learningthoughts.io',
                         body: "Refer to $BUILD_URL for more details"
                }

                failure {
                    mail subject: 'Build stage failed',
                         from: 'build@learningthoughts.io',
                         to: 'all@learningthoughts.io',
                         body: "Refer to $BUILD_URL for more details"
                }
            }
        }
    }
}
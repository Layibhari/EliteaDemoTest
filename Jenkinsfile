pipeline {
   agent{ label 'MAVEN' }
   options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 30, unit: 'MINUTES')
    }
   
   triggers {
        pollSCM('* * * * *')
    }

    stages {

        stage ('git') {

            steps {

                git url: 'https://github.com/nagendhrakothapalli/spring-petclinic.git'
                branch: 'developer'
            }
        }

        stage('build') {
            steps {
                sh 'mvn clean package'

            }
        }
    }

}
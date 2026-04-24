pipeline {
    agent any

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        SONAR_SERVER_NAME = 'sonarqube'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Package') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Unit Test') {
            steps {
                sh '''
                    export TESTCONTAINERS_RYUK_DISABLED=true
                    ./mvnw test -Dtest="!*IT, !*IntegrationTests"
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh './mvnw verify -DskipTests jacoco:report -B'
                withSonarQubeEnv("${SONAR_SERVER_NAME}") {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=spring-petclinic -B'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo "Running Security Analysis..."
            }
            post {
                always {
                    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'target/burp-reports', reportFiles: 'report.html', reportName: 'Security Report'])
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    apt-get update && apt-get install -y --no-install-recommends ansible sshpass openssh-client || true
                    cd ansible
                    ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook -i inventory.ini deploy.yml \
                      -e "jar_path=${WORKSPACE}/target"
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}

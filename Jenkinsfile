pipeline {
    agent any

    parameters {
        string(name: 'PETCLINIC_APP_URL', defaultValue: 'http://localhost:8080', description: 'URL Jenkins should use for security evidence and smoke checks')
        string(name: 'ANSIBLE_INVENTORY', defaultValue: 'ansible/inventory.ini', description: 'Inventory file for the production VM')
        booleanParam(name: 'RUN_DEPLOY', defaultValue: false, description: 'Run the Ansible deployment stage')
    }

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
                sh '''
                    chmod +x scripts/run-burp-report.sh
                    scripts/run-burp-report.sh "$PETCLINIC_APP_URL"
                '''
            }
            post {
                always {
                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'target/burp-reports', reportFiles: 'report.html', reportName: 'Security Report'])
                }
            }
        }

        stage('Deploy') {
            when {
                expression { return params.RUN_DEPLOY }
            }
            steps {
                sh '''
                    test -f "$ANSIBLE_INVENTORY" || {
                        echo "Missing Ansible inventory: $ANSIBLE_INVENTORY"
                        echo "Copy ansible/inventory.example.ini to ansible/inventory.ini and set the VM host first."
                        exit 1
                    }

                    JAR_PATH="$(ls -1 target/spring-petclinic-*.jar | head -n 1)"
                    test -n "$JAR_PATH" || {
                        echo "No packaged PetClinic jar found under target/"
                        exit 1
                    }

                    ansible-playbook -i "$ANSIBLE_INVENTORY" ansible/deploy-petclinic.yml \
                        -e "petclinic_jar_path=$JAR_PATH" \
                        -e "petclinic_app_url=$PETCLINIC_APP_URL"
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

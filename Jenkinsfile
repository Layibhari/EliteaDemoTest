pipeline {
    agent any
    triggers { githubPush() }
    
    environment {
        DOCKER_IMAGE = "sandissarkovskis/devops-final-project"
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${IMAGE_TAG} ."
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                sh "echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${IMAGE_TAG}"
            }
        }

         stage('Deploy to EC2') {
            steps {
                    sh '''
                        cat > ansible/inventory.ini << EOF
          [app]
          localhost ansible_connection=local
          EOF
                          ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --extra-vars "image_tag=$IMAGE_TAG"
                       '''
                    }
                }
        
        stage('Health Check') {
            steps {
                sh 'sleep 20'
                sh 'curl -f http://localhost:8080 || (echo "Health check failed" && exit 1)'
            }
        }
    }
}

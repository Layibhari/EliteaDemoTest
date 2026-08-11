pipeline {
    agent any
    triggers { githubPush() }
    
    environment {
        DOCKER_IMAGE = "sandissarkovskis/devops-final-project"
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        EC2_HOST = credentials('ec2-host')
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
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        cat > ansible/inventory.ini << EOF
          [app]
          ${EC2_HOST} ansible_user=ec2-user ansible_ssh_common_args='-o StrictHostKeyChecking=no'
          EOF
                          ansible-playbook -i ansible/inventory.ini ansible/playbook.yml --extra-vars 'image_tag=${IMAGE_TAG}'
                       """
                    }
                }
            }
        
        stage('Health Check') {
            steps {
                sh 'sleep 20'
                sh 'curl -f http://$EC2_HOST:8080 || (echo "Health check failed" && exit 1)'
            }
        }
    }
}

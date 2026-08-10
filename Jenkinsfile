pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = "rainis17/test"
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
                echo 'placeholder - will SSH into EC2 and run the container'
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'placeholder - will curl the app to confirm if its live'
            }
        }
    }
}

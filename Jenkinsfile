pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = "filler/ops"
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Docker Image') {
            steps {
                echo 'placeholder - will pull code from github'
            }
        }
        
        stage('Push to Docker Hub') {
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

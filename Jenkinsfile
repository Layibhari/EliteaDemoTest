pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        ECR_REPO = '200098097766.dkr.ecr.us-east-1.amazonaws.com/spring-petclinic'
        IMAGE_TAG = "latest"
    }

    stages {

       

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${ECR_REPO}:${IMAGE_TAG}")
                }
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                  credentialsId: 'aws-creds']]) {
                    sh '''
                    aws ecr get-login-password --region $AWS_REGION | \
                    docker login --username AWS --password-stdin $ECR_REPO
                    '''
                }
            }
        }

        stage('Push Image') {
            steps {
                sh '''
                docker push $ECR_REPO:$IMAGE_TAG
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no ec2-user@44.200.220.1 << EOF
                        docker stop petclinic || true
                        docker rm petclinic || true
                        aws ecr get-login-password --region $AWS_REGION | \
                        docker login --username AWS --password-stdin $ECR_REPO
                        docker pull $ECR_REPO:$IMAGE_TAG
                        docker run -d -p 8081:8081 --name petclinic $ECR_REPO:$IMAGE_TAG
                    EOF
                    '''
                }
            }
        }
    }
}

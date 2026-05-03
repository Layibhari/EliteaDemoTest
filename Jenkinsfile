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
            ssh -o StrictHostKeyChecking=no ubuntu@44.200.220.1 '
                docker stop petclinic || true
                docker rm petclinic || true

                docker login --username AWS \
                --password $(aws ecr get-login-password --region us-east-1) \
                200098097766.dkr.ecr.us-east-1.amazonaws.com

                docker pull 200098097766.dkr.ecr.us-east-1.amazonaws.com/spring-petclinic:latest

                docker run -d -p 8080:8080 --name petclinic \
                200098097766.dkr.ecr.us-east-1.amazonaws.com/spring-petclinic:latest
            '
            '''
        }
    }
}
                }
            }
        }
    }
}

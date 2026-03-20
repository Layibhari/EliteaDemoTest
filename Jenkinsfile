pipeline {
    agent { label 'docker-agent' }

    environment {
        DOCKER_HUB = credentials('dockerhub-creds')
        GIT_SHORT  = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        DOCKER_USER = 'magdalena01'
    }

    stages {
        stage('Checkstyle') {
            when { changeRequest() }
            steps {
                sh 'mvn checkstyle:checkstyle'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/checkstyle.html', allowEmptyArchive: true
                }
            }
        }

        stage('Test') {
            when { changeRequest() }
            steps {
                sh 'mvn test'
            }
        }

        stage('Build') {
            when { changeRequest() }
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Image MR') {
            when { changeRequest() }
            steps {
                sh """
                    mvn package -DskipTests
                    docker build -t ${DOCKER_USER}/mr:${GIT_SHORT} .
                    echo ${DOCKER_HUB_PSW} | docker login -u ${DOCKER_HUB_USR} --password-stdin
                    docker push ${DOCKER_USER}/mr:${GIT_SHORT}
                """
            }
        }

        stage('Docker Image Main') {
            when { branch 'main' }
            steps {
                sh """
                    mvn package -DskipTests
                    docker build -t ${DOCKER_USER}/main:${GIT_SHORT} .
                    echo ${DOCKER_HUB_PSW} | docker login -u ${DOCKER_HUB_USR} --password-stdin
                    docker push ${DOCKER_USER}/main:${GIT_SHORT}
                """
            }
        }
    }
}
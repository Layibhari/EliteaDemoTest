pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = 'Jenkins_Practical_Docker'
        DOCKERHUB_USERNAME = 'uday6395'
        SHORT_COMMIT = "${env.GIT_COMMIT[0..6]}"
    }

    stages {

        stage('Checkstyle') {
            when {
                not {
                    branch 'main'
                }
            }

            steps {
                sh './mvnw checkstyle:checkstyle'
            }

            post {
                always {
                    archiveArtifacts artifacts: '**/target/checkstyle-result.xml', fingerprint: true
                }
            }
        }

        stage('Test') {
            when {
                not {
                    branch 'main'
                }
            }

            steps {
                sh './mvnw test'
            }
        }

        stage('Build') {
            when {
                not {
                    branch 'main'
                }
            }

            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build & Push MR') {
            when {
                not {
                    branch 'main'
                }
            }

            steps {

                script {

                    docker.withRegistry('', DOCKERHUB_CREDENTIALS) {

                        def app = docker.build(
                            "${DOCKERHUB_USERNAME}/mr:${SHORT_COMMIT}"
                        )

                        app.push()
                    }
                }
            }
        }

        stage('Docker Build & Push MAIN') {
            when {
                branch 'main'
            }

            steps {

                sh './mvnw clean package -DskipTests'

                script {

                    docker.withRegistry('', DOCKERHUB_CREDENTIALS) {

                        def app = docker.build(
                            "${DOCKERHUB_USERNAME}/main:${SHORT_COMMIT}"
                        )

                        app.push()
                    }
                }
            }
        }
    }
}

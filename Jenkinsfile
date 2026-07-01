pipeline {
    agent any
    stages {
        stage("Install Docker Compose") {
            steps {
                script {
                    def runStage = load 'groovy/install-docker-compose.groovy'
                    runStage()
                }
            }
        }
        stage("Clone Repo") {
            steps {
                script {
                    def runStage = load 'groovy/clone-repo.groovy'
                    runStage()
                }
            }
        }
        stage("Run ZAP Scans") {
            steps {
                script {
                    def runStage = load 'groovy/zap.groovy'
                    runStage()
                }
            }
        }
    }
}

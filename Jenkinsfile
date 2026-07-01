pipeline {
    agent any
    stages {
        stage("Install Docker Compose") {
            steps {
                script {
                    def installDockerCompose = load 'groovy/install-docker-compose.groovy'
                    installDockerCompose()
                }
            }
        }
        stage("Clone Repo") {
            steps {
                script {
                    def cloneRepo = load 'groovy/clone-repo.groovy'
                    cloneRepo()
                }
            }
        }
        stage("Run ZAP Scans") {
            steps {
                script {
                    def runZAP = load 'groovy/zap.groovy'
                    runZAP()
                }
            }
        }
    }
}

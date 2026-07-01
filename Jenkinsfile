// load the stage closures from the groovy files
def installDockerCompose = load 'groovy/install-docker-compose.groovy'
def cloneRepo = load 'groovy/clone-repo.groovy'
def runZAP = load 'groovy/zap.groovy'

pipeline {
    agent any
    stages {
        // Execute the closures inside the pipeline DSL context
        stage("Install Docker Compose") {
            steps {
                script {
                    installDockerCompose()
                }
            }
        }
        stage("Clone Repo") {
            steps {
                script {
                    cloneRepo()
                }
            }
        }
        stage("Run ZAP Scans") {
            steps {
                script {
                    runZAP()
                }
            }
        }
    }
}

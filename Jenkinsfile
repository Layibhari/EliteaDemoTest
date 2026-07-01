// load the stage closures from the groovy files
def installDockerComposeStage = load 'groovy/install-docker-compose.groovy'
def cloneRepoStage = load 'groovy/clone-repo.groovy'
def zapStage = load 'groovy/zap.groovy'

pipeline {
    agent any
    stages {
        // Execute the closures inside the pipeline DSL context
        installDockerComposeStage()
        cloneRepoStage()
        zapStage()
    }
}

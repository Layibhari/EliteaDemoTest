def cloneRepoStage = {

    stage('Clone Repo') {
        git branch: 'ZAP', credentialsId: 'joe2',
            url: 'https://github.com/siddvoh/spring-petclinic-dev-pipeline.git'
    }

}

return cloneRepoStage

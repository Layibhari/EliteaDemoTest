def zapStage = {

    stage('Run ZAP Scans') {

        sh 'mkdir -p ./zap/reports'
        sh 'docker-compose -f "./infra/docker-compose.yml" up -d app'
        sh 'docker-compose -f "./infra/docker-compose.yml" run zap'
        sh 'docker-compose -f "./infra/docker-compose.yml" down app'

        publishHTML(target: [
            reportDir: './zap/reports',
            reportFiles: 'petclinic-zap-report.html',
            reportName: 'ZAP Security Report'
        ])
    }

}

return zapStage

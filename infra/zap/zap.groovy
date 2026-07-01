def runZap() {
    pipeline {
        agent any

        stages {
            
            stage('Start'){
                steps {
                    echo "starting..."
                }
            }
            stage('Install Docker Compose v2') {
                steps {
                    script {
                        COMPOSE_VERSION = '5.2.0'
                        // Detect architecture inside the Jenkins agent
                        def arch = sh(
                            script: "uname -m",
                            returnStdout: true
                        ).trim()
                    
                        // Map architecture to Docker Compose binary name
                        def composeBinary = ""
                        if (arch == "x86_64") {
                            composeBinary = "docker-compose-linux-x86_64"
                        } else if (arch == "aarch64") {
                            composeBinary = "docker-compose-linux-aarch64"
                        } else {
                            error "Unsupported architecture detected: ${arch}"
                        }
                    
                        // Download and install Docker Compose
                        sh """
                            curl -SL "https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/${composeBinary}" \
                                -o /usr/local/bin/docker-compose
                            chmod +x /usr/local/bin/docker-compose
                        """
                        
                        echo "Installed Docker Compose v2 for architecture: ${arch}"
                        sh 'docker-compose version'
                    }
                }
            }
            stage('Clone Repo') {
                steps {
                    git branch: 'ZAP', credentialsId: 'joe2', url: 'https://github.com/siddvoh/spring-petclinic-dev-pipeline.git'
                }
            }
            stage('Build Spring Pet Clinic') {
                steps {
                    sh 'mkdir -p ./zap/reports'
                    sh 'docker-compose -f "./infra/docker-compose.yml" up -d app'
                    sh 'docker-compose -f "./infra/docker-compose.yml" run zap'
                    sh 'docker-compose -f "./infra/docker-compose.yml" down app'
                }
                post {
                always {
                    publishHTML(target: [
                    reportDir: './zap/reports',
                    reportFiles: 'petclinic-zap-report.html',
                    reportName: 'ZAP Security Report'
                    ])
                }
                }
            }
            stage('Done') {
                steps {
                    echo "done"
                }
            }
        }
    }
}

return this
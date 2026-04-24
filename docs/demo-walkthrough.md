# Demonstration video walkthrough

Use this order for the final recording.

## 1. Show the repository

```bash
git checkout main
git pull origin main
git log -1 --oneline
```

Show the important files:

- `docker-compose.devsecops.yml`
- `Jenkinsfile`
- `jenkins/Dockerfile`
- `jenkins/plugins.txt`
- `ansible/deploy-petclinic.yml`
- `burpsuite/Dockerfile`
- `prometheus/prometheus.yml`
- `grafana/provisioning/datasources/prometheus.yml`

## 2. Start the stack

```bash
docker compose -f docker-compose.devsecops.yml up -d --build
docker ps
```

Open Jenkins, SonarQube, Prometheus, Grafana, and Burp VNC.

## 3. Show Jenkins pipeline

Open Jenkins at http://localhost:8080. Show the pipeline job and the
`Jenkinsfile` stages:

- Checkout
- Build & Package
- Unit Test
- SonarQube Analysis
- Quality Gate
- Security Scan
- Deploy

Run the pipeline with `RUN_DEPLOY=true` after the VM inventory is configured.

## 4. Show SonarQube

Open http://localhost:9000. Show the Spring PetClinic project, quality gate,
bugs, vulnerabilities, and code smells.

## 5. Show Burp Suite

Connect to VNC at `localhost:5900`. Show Burp Community running. Browse the
PetClinic VM app through Burp and show Proxy HTTP history. In Jenkins, open the
published `Security Report` HTML artifact.

## 6. Show production deployment

Open the production VM app:

```text
http://<vm-ip>:8080
```

Show the welcome screen before the code change.

## 7. Trigger with a code change

Make a visible welcome-page text change, commit, and push:

```bash
git add src/main/resources/templates/welcome.html
git commit -m "demo: update welcome text"
git push origin main
```

Wait for Jenkins SCM polling or click Build Now. Show the pipeline building,
testing, scanning, and deploying.

## 8. Show the updated app

Refresh `http://<vm-ip>:8080` and show the changed welcome-page content.

## 9. Show Prometheus

Open:

- http://localhost:9090/targets
- http://localhost:9090/alerts

Run these queries:

```promql
up
rate(http_server_requests_seconds_count{job="petclinic"}[1m])
default_jenkins_builds_last_build_result_ordinal
```

## 10. Show Grafana

Open http://localhost:3000. Show the Prometheus datasource and Jenkins
dashboard.

## Pre-recording checks

```bash
docker compose -f docker-compose.devsecops.yml up -d --build
curl -fs http://localhost:8080
curl -fs http://localhost:8080/prometheus/
curl -fs http://localhost:9000
curl -fs http://localhost:9090/-/ready
curl -fs http://localhost:3000
curl -fs http://<vm-ip>:8080
```

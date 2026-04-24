# DevSecOps pipeline setup

This project runs a DevSecOps pipeline for Spring PetClinic with Jenkins,
SonarQube, Burp Suite Community, Prometheus, Grafana, Docker, and Ansible.
The monitoring and CI services run in Docker. The application is deployed to
a production VM with Ansible from Jenkins.

## Prerequisites

- Docker and Docker Compose
- A GitHub fork of this repository
- A production Linux VM reachable from Jenkins over SSH
- SSH key access from Jenkins to the VM
- A Jenkins job that points at the forked repository

## Start the tool stack

```bash
docker compose -f docker-compose.devsecops.yml up -d --build
docker ps
```

Main URLs:

- Jenkins: http://localhost:8080
- SonarQube: http://localhost:9000
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Burp Suite Community: VNC at localhost:5900

## Jenkins

The Jenkins image is built from `jenkins/Dockerfile`. It installs the plugins
from `jenkins/plugins.txt`, including Blue Ocean, SonarQube, Prometheus,
Ansible, HTML Publisher, and Pipeline plugins. Configuration-as-Code is loaded
from `jenkins/casc.yaml`.

The `Jenkinsfile` polls SCM every two minutes, builds the Maven project, runs
unit tests, sends analysis to SonarQube, waits for the quality gate, publishes
a security evidence report, and can deploy to the production VM with Ansible.

For deployment, create an inventory file:

```bash
cp ansible/inventory.example.ini ansible/inventory.ini
```

Edit `ansible/inventory.ini` with the VM IP, SSH user, and private key path.
Then run the Jenkins build with `RUN_DEPLOY=true`.

## SonarQube

SonarQube runs from the `sonarqube` service. Jenkins uses:

```bash
./mvnw sonar:sonar -Dsonar.projectKey=spring-petclinic -B
```

Create a matching SonarQube server entry in Jenkins named `sonarqube`, because
the Jenkinsfile uses `SONAR_SERVER_NAME = 'sonarqube'`.

## Burp Suite Community

Burp Suite Community is GUI-first and does not provide the same headless
scanner/report exporter as Burp Suite Professional. The repository includes a
containerized Burp Community desktop exposed over VNC.

Start it with:

```bash
docker compose -f docker-compose.devsecops.yml up -d burpsuite
```

Connect a VNC client to `localhost:5900`, open Burp, and browse the deployed
PetClinic app through Burp's browser or proxy. Jenkins publishes
`target/burp-reports/report.html` as the security evidence artifact.

## Ansible deployment

The playbook `ansible/deploy-petclinic.yml` copies the built jar to the VM,
installs Java 21, creates a `petclinic` system user, installs a systemd unit,
starts the app, and waits for `/actuator/health`.

Manual test:

```bash
./mvnw clean package -DskipTests
ansible-playbook -i ansible/inventory.ini ansible/deploy-petclinic.yml \
  -e "petclinic_jar_path=target/spring-petclinic-4.0.0-SNAPSHOT.jar"
```

After deployment, open:

```text
http://<vm-ip>:8080
```

## Prometheus and Grafana

Prometheus config lives in `prometheus/prometheus.yml`. It scrapes Prometheus,
Jenkins, and the PetClinic actuator Prometheus endpoint. Rules live under
`prometheus/rules/`.

Grafana is provisioned with a Prometheus datasource and dashboard under
`grafana/provisioning/` and `grafana/dashboards/`.

Useful checks:

```bash
curl -fs http://localhost:8080/prometheus/
curl -fs http://localhost:9090/-/ready
curl -fs http://localhost:9090/api/v1/targets
```

## Evidence to collect

- Production PetClinic welcome screen on the VM
- Jenkins pipeline stage view or Blue Ocean view
- SonarQube project and quality gate
- Burp Suite Proxy HTTP history and Jenkins HTML report artifact
- Prometheus `/targets` and `/alerts`
- Grafana dashboard fed by Prometheus
- Before/after screenshots showing a pushed code change reflected on the VM

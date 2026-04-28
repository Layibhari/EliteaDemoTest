# 🐾 Spring PetClinic — AWS EKS Deployment with Monitoring

![AWS](https://img.shields.io/badge/AWS-EKS-orange?logo=amazon-aws)
![Terraform](https://img.shields.io/badge/Terraform-IaC-purple?logo=terraform)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-blue?logo=kubernetes)
![Prometheus](https://img.shields.io/badge/Prometheus-Monitoring-red?logo=prometheus)
![Grafana](https://img.shields.io/badge/Grafana-Dashboards-orange?logo=grafana)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-green?logo=spring)

A production-style deployment of the Spring PetClinic application on AWS using Terraform for infrastructure, Kubernetes (EKS) for orchestration, and Prometheus + Grafana for monitoring.

---

## 📐 Architecture

```
                    ┌──────────────────────────────────────────────────────┐
                    │                  AWS Cloud — us-east-1               │
                    │                                                      │
                    │  ┌──────────────────────────────────────────────┐   │
                    │  │              VPC — 10.0.0.0/16               │   │
                    │  │                                              │   │
                    │  │  ┌──────────────────┐ ┌──────────────────┐  │   │
                    │  │  │  Public Subnet    │ │  Public Subnet   │  │   │
                    │  │  │   us-east-1a      │ │   us-east-1b     │  │   │
                    │  │  │  [NAT Gateway]    │ │  [IGW]           │  │   │
                    │  │  └──────────────────┘ └──────────────────┘  │   │
                    │  │                                              │   │
                    │  │  ┌──────────────────┐ ┌──────────────────┐  │   │
                    │  │  │  Private Subnet   │ │  Private Subnet  │  │   │
                    │  │  │   us-east-1a      │ │   us-east-1b     │  │   │
                    │  │  │  [EKS Node 1]     │ │  [EKS Node 2]   │  │   │
                    │  │  │  [RDS MySQL]      │ │  [ECR]          │  │   │
                    │  │  └──────────────────┘ └──────────────────┘  │   │
User ──► ELB:80 ───┤  │                                              │   │
                    │  │  ┌──────────────────────────────────────┐   │   │
                    │  │  │       Monitoring Namespace            │   │   │
                    │  │  │  [Prometheus] [Grafana] [Alertmgr]   │   │   │
                    │  │  │  [Node Exporter] [Kube State Metrics] │   │   │
                    │  │  └──────────────────────────────────────┘   │   │
                    │  └──────────────────────────────────────────────┘   │
                    └──────────────────────────────────────────────────────┘
```

---

## ✅ Deployment Progress

| # | Task | Status |
|---|---|---|
| 1 | AWS credentials configured | ✅ Done |
| 2 | VPC + Subnets + IGW + NAT Gateway | ✅ Done |
| 3 | EKS Cluster + Node Group (t3.small x2) | ✅ Done |
| 4 | RDS MySQL in private subnet | ✅ Done |
| 5 | ECR Repository created | ✅ Done |
| 6 | Docker image built and pushed to ECR | ✅ Done |
| 7 | Spring PetClinic deployed to EKS | ✅ Done |
| 8 | App accessible via LoadBalancer URL | ✅ Done |
| 9 | Code pushed to GitHub | ✅ Done |
| 10 | Prometheus installed via Helm | ✅ Done |
| 11 | Grafana installed via Helm | ✅ Done |
| 12 | Grafana dashboards imported | ✅ Done |
| 13 | ServiceMonitor for Spring Boot | 🔄 In Progress |
| 14 | Alert rules configured | 🔄 In Progress |

---

## 🛠️ Tech Stack

| Layer | Technology | Details |
|---|---|---|
| Infrastructure | Terraform | IaC for all AWS resources |
| Network | AWS VPC | Custom VPC, public & private subnets across 2 AZs |
| Compute | AWS EKS | Managed Kubernetes cluster |
| Nodes | EC2 t3.small x2 | Worker nodes |
| App | Spring Boot 4.0.3 | Java web application |
| Runtime | Java 17 + Tomcat 11 | Embedded app server |
| Database | RDS MySQL 8.4 | Managed database, db.t3.micro |
| Registry | AWS ECR | Docker image storage |
| Load Balancer | AWS ELB | Public traffic entry |
| Package Manager | Helm | Kubernetes package manager |
| Metrics | Prometheus | Metrics collection & storage |
| Dashboards | Grafana | Visualization UI |
| Alerts | Alertmanager | Alert routing |
| Node Metrics | Node Exporter | CPU, memory, disk per node |
| K8s Metrics | Kube State Metrics | Pod, deployment stats |

---

## 📁 Project Structure

```
spring-petclinic/
├── Dockerfile                        # App container build
├── k8s/
│   ├── db.yml                        # RDS database secret
│   ├── petclinic.yml                 # App deployment + LoadBalancer
│   └── monitoring/
│       ├── values.yaml               # Prometheus + Grafana Helm values
│       ├── servicemonitor.yml        # Spring Boot metrics scraping
│       └── alerts.yml                # Custom alert rules
├── infra/
│   ├── envs/
│   │   └── dev/
│   │       ├── main.tf               # Dev environment entry point
│   │       └── backend.tf            # Terraform state backend
│   └── modules/
│       ├── vpc/                      # VPC, subnets, IGW, NAT, routes
│       ├── eks/                      # EKS cluster, node group, IAM
│       ├── rds/                      # RDS instance, subnet group, SG
│       └── ecr/                      # ECR repository
└── src/                              # Spring Boot source code
```

---

## ✅ Prerequisites

- AWS Account with IAM credentials
- [Terraform](https://terraform.io) >= 1.0
- [AWS CLI](https://aws.amazon.com/cli/) configured
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- [Helm](https://helm.sh) >= 3.0 installed
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) running

---

## 🚀 Deployment Guide

### 1️⃣ Configure AWS Credentials

```bash
aws configure
# Enter: Access Key, Secret Key, Region (us-east-1)

# Verify
aws sts get-caller-identity
```

---

### 2️⃣ Deploy Infrastructure

```bash
cd infra/envs/dev
terraform init
terraform plan
terraform apply
```

> ⏱️ Takes ~20-30 minutes. EKS cluster takes longest (~15 mins).

**Resources created:**

| Module | Resources | Count |
|---|---|---|
| VPC | VPC, subnets, IGW, NAT, EIP, route tables | 11 |
| EKS | Cluster, node group, IAM roles & policies | 6 |
| RDS | MySQL instance, subnet group, security group | 3 |
| ECR | Container image repository | 1 |
| **Total** | | **~21** |

---

### 3️⃣ Build & Push Docker Image

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login \
  --username AWS \
  --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Build
docker build -t petclinic .

# Tag & push
docker tag petclinic:latest <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/petclinic:latest
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/petclinic:latest
```

---

### 4️⃣ Configure kubectl

```bash
aws eks update-kubeconfig --name petclinic-cluster --region us-east-1
kubectl get nodes   # verify 2 nodes Ready
```

---

### 5️⃣ Deploy Application

```bash
# Get RDS endpoint
aws rds describe-db-instances \
  --query "DBInstances[*].Endpoint.Address" \
  --output text

# Update k8s/db.yml with RDS endpoint, then:
kubectl apply -f k8s/db.yml
kubectl apply -f k8s/petclinic.yml

# Verify
kubectl get pods
kubectl get svc   # copy EXTERNAL-IP
```

Open in browser: `http://<EXTERNAL-IP>`

---

### 6️⃣ Install Prometheus & Grafana

```bash
# Add Helm repo
helm repo add prometheus-community \
  https://prometheus-community.github.io/helm-charts
helm repo update

# Create namespace
kubectl create namespace monitoring

# Install with custom values
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --values k8s/monitoring/values.yaml \
  --timeout 10m

# Verify all pods running
kubectl get pods -n monitoring
```

---

### 7️⃣ Apply Monitoring Manifests

```bash
# ServiceMonitor — scrape Spring Boot metrics
kubectl apply -f k8s/monitoring/servicemonitor.yml

# Alert rules
kubectl apply -f k8s/monitoring/alerts.yml
```

---

### 8️⃣ Access Grafana

```bash
# Get admin password
kubectl get secret --namespace monitoring \
  -l app.kubernetes.io/component=admin-secret \
  -o jsonpath="{.items[0].data.admin-password}" | base64 --decode

# Port forward
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```

Open: `http://localhost:3000`
Login: `admin` / `<password above>`

---

### 9️⃣ Grafana Dashboards

Pre-imported dashboards via `values.yaml`:

| Dashboard | ID | Monitors |
|---|---|---|
| Node Exporter | 1860 | Node CPU, memory, disk, network |
| Kubernetes Cluster | 7249 | Nodes, pods, namespaces |
| Kubernetes Pods | 6781 | Per-pod CPU, memory, restarts |
| Spring Boot | 12900 | JVM heap, HTTP requests, DB |

---

## 📊 Monitoring Stack

### Components

| Component | Purpose | Port |
|---|---|---|
| Prometheus | Metrics collection & storage | 9090 |
| Grafana | Dashboard visualization | 3000 |
| Alertmanager | Alert routing | 9093 |
| Node Exporter | Node-level metrics | 9100 |
| Kube State Metrics | Kubernetes object metrics | 8080 |

### Alert Rules

| Alert | Condition | Severity |
|---|---|---|
| PodCrashLooping | Pod restarting frequently | Critical |
| PodNotReady | Pod not ready for 5 mins | Warning |
| NodeHighCPU | CPU > 80% for 5 mins | Warning |
| NodeHighMemory | Memory > 80% for 5 mins | Warning |
| NodeDiskPressure | Disk > 85% | Critical |
| JVMHighHeapUsage | Heap > 85% | Warning |
| HighHTTPErrorRate | 5xx errors > 10% | Critical |
| SlowHTTPResponse | p95 latency > 2s | Warning |
| DBConnectionPoolExhausted | HikariCP > 90% | Critical |

### Access Monitoring UIs

```bash
# Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Prometheus
kubectl port-forward -n monitoring \
  svc/prometheus-kube-prometheus-prometheus 9090:9090

# Alertmanager
kubectl port-forward -n monitoring \
  svc/prometheus-kube-prometheus-alertmanager 9093:9093
```

---

## 🔧 Configuration

### Key Environment Variables (k8s/petclinic.yml)

```yaml
- name: SPRING_PROFILES_ACTIVE
  value: mysql
- name: SPRING_DATASOURCE_URL
  value: "jdbc:mysql://<RDS_ENDPOINT>:3306/petclinic"
- name: SPRING_DATASOURCE_USERNAME
  value: admin
- name: SPRING_DATASOURCE_PASSWORD
  value: "<YOUR_PASSWORD>"
- name: JAVA_TOOL_OPTIONS
  value: "-XX:MaxMetaspaceSize=100M -XX:ReservedCodeCacheSize=100M -Xss512k -XX:MaxDirectMemorySize=10M -Xmx256m"
```

---

## 🐛 Troubleshooting

| Error | Cause | Fix |
|---|---|---|
| `Subnets must be in 2 AZs` | Missing `availability_zone` | Add `availability_zone = data.aws_availability_zones.available.names[count.index]` |
| `AsgInstanceLaunchFailures` | Default instance not Free Tier | Add `instance_types = ["t3.micro"]` |
| `Connection to localhost:5432` | App using wrong DB | Set `SPRING_DATASOURCE_URL` env var |
| `RDS VPC = None` | RDS in default VPC | Add `db_subnet_group_name` + `vpc_security_group_ids` |
| `CrashLoopBackOff` memory | t3.micro low RAM | Add `JAVA_TOOL_OPTIONS` |
| `ERR_CONNECTION_TIMED_OUT` | SG blocking port 80 | Add inbound rule port 80 to EKS SG |
| `Too many pods` | t3.micro max 4 pods/node | Upgrade to `t3.small` |
| Prometheus pods `Pending` | Insufficient resources | Upgrade instance type |

---

## 💰 Cost Estimate

| Resource | Cost |
|---|---|
| EKS Cluster control plane | ~$0.10/hour (~$72/month) |
| EC2 t3.small x2 | ~$0.0416/hour |
| NAT Gateway | ~$0.045/hour |
| RDS db.t3.micro | Free Tier eligible |
| ECR storage | ~$0.10/GB/month |

---

## 🧹 Cleanup

```bash
# 1. Delete app
kubectl delete -f k8s/petclinic.yml
kubectl delete -f k8s/db.yml

# 2. Remove monitoring
helm uninstall prometheus -n monitoring
kubectl delete namespace monitoring

# 3. Destroy AWS infrastructure
cd infra/envs/dev
terraform destroy
```

---

## 📝 Quick Reference

```bash
# App status
kubectl get pods
kubectl get svc petclinic

# Monitoring status
kubectl get pods -n monitoring

# Logs
kubectl logs <pod-name>
kubectl logs <pod-name> --previous

# Grafana access
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# Prometheus access
kubectl port-forward -n monitoring \
  svc/prometheus-kube-prometheus-prometheus 9090:9090

# Get RDS endpoint
aws rds describe-db-instances \
  --query "DBInstances[*].Endpoint.Address" --output text

# Terraform
cd infra/envs/dev
terraform plan
terraform apply
terraform destroy
```

---

## 📌 Repository

- GitHub: [https://github.com/prashanth9533/spring-petclinic](https://github.com/prashanth9533/spring-petclinic)
- Upstream: [https://github.com/spring-projects/spring-petclinic](https://github.com/spring-projects/spring-petclinic)
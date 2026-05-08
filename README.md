# Cloud Clinic

Production-ready Spring Boot deployment on AWS ECS Fargate using Terraform and GitHub Actions CI/CD.

---

# Live Application

http://cloudclinic-prod-alb-1783386781.us-west-2.elb.amazonaws.com

---

# Project Overview

Cloud Clinic is a containerized Spring Boot application based on the Spring PetClinic application, deployed on AWS using a modern cloud-native infrastructure stack.

The solution provisions AWS infrastructure using Terraform and automates container deployment using GitHub Actions. The application runs on Amazon ECS Fargate behind an Application Load Balancer with centralized logging through CloudWatch.

The implementation focuses on:

- Infrastructure as Code
- Automated CI/CD
- Secure networking
- Containerized deployments
- Operational simplicity
- Production-style AWS architecture

---

# Architecture Overview

```text
Developer
   ↓
GitHub Repository
   ↓
GitHub Actions CI/CD
   ↓
Docker Build
   ↓
Amazon ECR
   ↓
Amazon ECS Fargate
   ↓
Application Load Balancer
   ↓
End Users
```

---

# AWS Services Used

| Service | Purpose |
|---|---|
| Amazon ECS Fargate | Container orchestration |
| Amazon ECR | Docker image registry |
| Application Load Balancer | Traffic routing |
| Amazon VPC | Network isolation |
| CloudWatch Logs | Centralized logging |
| IAM | Access control |
| S3 | Terraform remote state |
| GitHub Actions | CI/CD automation |

---

# Infrastructure Design

## Networking

The infrastructure is deployed inside a dedicated VPC with:

- 2 public subnets across multiple Availability Zones
- 2 private subnets across multiple Availability Zones
- Internet Gateway for public traffic
- NAT Gateway for outbound internet access from private ECS tasks

### Public Subnets

Used for:
- Application Load Balancer
- NAT Gateway

### Private Subnets

Used for:
- ECS Fargate tasks

This ensures containers are not directly exposed to the public internet.

---

# ECS Architecture

The application runs on Amazon ECS Fargate using:

- ECS Cluster
- ECS Service
- ECS Task Definition
- Rolling deployment strategy

The ECS service:

- runs inside private subnets
- uses Fargate launch type
- deploys behind an ALB target group
- uses CloudWatch logging

Health checks are configured using:

```text
/actuator/health
```

---

# Security Design

Security considerations implemented include:

- ECS tasks deployed in private subnets
- Security groups restricting inbound traffic
- ALB exposed publicly on port 80 only
- ECS containers accessible only from the ALB security group
- IAM least-privilege model
- Encrypted Terraform remote state storage
- ECR image scanning enabled

---

# CI/CD Pipeline

Deployment automation is implemented using GitHub Actions.

Pipeline flow:

```text
Push to GitHub
    ↓
GitHub Actions workflow triggered
    ↓
Docker image build
    ↓
Push image to Amazon ECR
    ↓
Update ECS task definition
    ↓
Deploy updated container to ECS
```

The deployment uses ECS rolling updates to minimize downtime during releases.

---

# Terraform Structure

```text
terraform/
├── bootstrap/
├── modules/
│   ├── alb/
│   ├── cloudwatch/
│   ├── ecr/
│   ├── ecs/
│   ├── iam/
│   ├── networking/
│   ├── security-groups/
│   └── vpc/
└── environments/
    └── prod/
```

The Terraform configuration is modularized to improve:

- maintainability
- reusability
- readability
- separation of concerns

---

# Repository Structure

```text
cloud-clinic/
├── .github/workflows/
├── terraform/
├── src/
├── Dockerfile
├── .dockerignore
├── pom.xml
├── README.md
└── architecture-diagram.png
```

---

# Deployment Workflow

## Infrastructure Provisioning

Terraform provisions:

- networking
- ECS infrastructure
- ALB
- IAM roles
- CloudWatch logs
- ECR repositories

## Application Deployment

GitHub Actions handles:

- Docker image builds
- image pushes to ECR
- ECS service deployments

---

# Docker Configuration

The application uses a multi-stage Docker build:

- Maven build stage
- lightweight runtime image
- optimized container deployment

The application listens on:

```text
Port 8080
```

---

# Monitoring and Logging

Application logs are centralized using CloudWatch Logs.

The ECS service is configured with:

- awslogs driver
- centralized log groups
- log retention policies

ALB health checks monitor application availability continuously.

---

# Design Decisions

The solution was intentionally designed to balance simplicity, automation, and production-readiness.

Key decisions include:

- ECS Fargate was selected to eliminate EC2 instance management overhead.
- GitHub Actions was used instead of CodePipeline/CodeBuild to simplify CI/CD and reduce complexity.
- Terraform modules were separated by responsibility for improved maintainability.
- Rolling deployments were selected over blue/green deployments to reduce infrastructure cost and operational complexity for the scope of the assessment.
- ECS tasks run in private subnets for improved security.
- CloudWatch Logs was selected for centralized operational visibility.

---

# Assumptions

- Single production-style environment (`prod`)
- HTTP used instead of HTTPS for assessment simplicity
- Single NAT Gateway used to reduce infrastructure cost
- GitHub repository secrets used for AWS authentication
- Application scaling requirements are minimal for assessment scope

---

# Future Improvements

Potential production enhancements include:

- HTTPS using ACM certificates
- Route53 custom domain integration
- AWS WAF integration
- ECS auto scaling policies
- Terraform state locking using DynamoDB
- GitHub Actions OIDC authentication
- Blue/green ECS deployments
- Observability with Prometheus and Grafana
- Container vulnerability scanning enhancements

---

# Cost Optimization Notes

The infrastructure was designed with cost-awareness in mind.

Implemented optimizations include:

- ECS Fargate serverless compute
- Single NAT Gateway deployment
- Bounded CloudWatch log retention
- Minimal always-on infrastructure

---

# Terraform Remote State

Terraform remote state is stored in Amazon S3.

Features:

- versioning enabled
- encryption enabled
- centralized state management

---

# Deployment Verification

The application was successfully deployed and verified through:

- ECS service stability checks
- ALB accessibility
- CloudWatch logging validation
- GitHub Actions deployment pipeline execution

---

# Key Outputs

Terraform outputs include:

- ALB DNS name
- ECS cluster name
- ECS service name
- ECR repository name
- CloudWatch log group
- VPC and subnet IDs

These outputs are consumed by the GitHub Actions deployment workflow.

---

# Conclusion

This project demonstrates a production-style AWS deployment workflow using:

- Terraform Infrastructure as Code
- Docker containerization
- Amazon ECS Fargate
- GitHub Actions CI/CD
- AWS networking and security best practices

The implementation emphasizes automation, modularity, maintainability, and operational simplicity while remaining aligned with real-world DevOps deployment practices.

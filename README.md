# Cloud Clinic — AWS Infrastructure (Terraform + ECS Fargate)

Production-style Terraform that provisions the AWS footprint for a
containerized Spring Boot application ("Cloud Clinic", a customized Spring
PetClinic) running on **ECS Fargate** behind an **Application Load
Balancer**, with images stored in **ECR** and logs in **CloudWatch**.

CI/CD (GitHub Actions, kept in a separate workflow) is responsible for
building the Docker image, pushing it to ECR, and updating the ECS service.
This Terraform stack is deliberately scoped to **infrastructure only**.

---

## 1. Architecture overview

```
                 Internet
                    │
                    ▼
           ┌─────────────────┐
           │ Application LB  │  public subnets · port 80
           │   (ALB)         │
           └────────┬────────┘
                    │  HTTP → 8080 (target group / IP targets)
                    ▼
           ┌─────────────────┐
           │  ECS Service    │  Fargate · private subnets
           │  (rolling)      │
           └────────┬────────┘
                    │  pulls image                  │  awslogs
                    ▼                               ▼
              ┌──────────┐                   ┌────────────┐
              │   ECR    │                   │ CloudWatch │
              └──────────┘                   │   Logs     │
                                             └────────────┘

GitHub Actions  ──build──▶  ECR  ──update service──▶  ECS Fargate
```
With Gradle, the command is as follows:

```bash
./gradlew bootRun
```

You can then access the Petclinic at <http://localhost:8080/>.

<img width="1042" alt="petclinic-screenshot" src="https://cloud.githubusercontent.com/assets/838318/19727082/2aee6d6c-9b8e-11e6-81fe-e889a5ddfded.png">

### Networking

- 1 VPC (`/16`), DNS support + hostnames enabled.
- 2 public subnets, 2 private subnets, each pair across distinct AZs.
- 1 Internet Gateway attached to the VPC.
- 1 NAT Gateway in the first public subnet (cost-optimized; see
  *Limitations* for the HA tradeoff).
- Public route table → IGW; private route table → NAT.

### Compute / delivery path

- **ALB** is internet-facing, listens on port 80, forwards to a
  target group of type `ip` whose health check is
  `GET /actuator/health` returning `200`.
- **ECS cluster** with Container Insights enabled, Fargate capacity
  providers (`FARGATE` default + `FARGATE_SPOT` available).
- **ECS service** runs in **private subnets only**, has
  `assign_public_ip = false`, and reaches the internet (ECR, CloudWatch)
  via the NAT Gateway.
- Deployment strategy is the ECS rolling update with
  `minimum_healthy_percent=100`, `maximum_percent=200`, and a
  **deployment circuit breaker with rollback enabled**.

### Security

- ALB SG: ingress `tcp/80` from `0.0.0.0/0`.
- ECS SG: ingress on the container port restricted to **the ALB SG only**.
- ECR scans images on push; encryption-at-rest with AES256.
- S3 state bucket is private, versioned, and encrypted (state locking
  is intentionally omitted to avoid DynamoDB cost — see *Limitations*).
- Two distinct IAM roles for ECS:
  - `task_execution_role` — ECS agent (ECR pulls, CloudWatch logs).
  - `task_role` — used by the running container; starts empty
    (least privilege).

---

## 2. Repository layout

```
.
├── README.md
└── terraform/
    ├── bootstrap/                          # S3 state bucket (run once)
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── outputs.tf
    │   └── terraform.tfvars.example
    ├── modules/
    │   ├── vpc/                            # VPC + public/private subnets
    │   ├── networking/                     # IGW, NAT, route tables, associations
    │   ├── security-groups/                # ALB and ECS security groups
    │   ├── alb/                            # ALB + target group + HTTP listener
    │   ├── ecr/                            # ECR repository + lifecycle policy
    │   ├── iam/                            # ECS task execution + task roles
    │   ├── cloudwatch/                     # ECS log group
    │   └── ecs/                            # cluster + task definition + service
    └── environments/
        └── prod/
            ├── provider.tf                 # terraform/provider versions, default tags
            ├── backend.tf                  # remote state (s3 + dynamodb)
            ├── main.tf                     # composes the modules above
            ├── variables.tf
            ├── outputs.tf                  # values consumed by GitHub Actions
            └── terraform.tfvars.example
```

---

## 3. Deployment steps

### Prerequisites

- Terraform `>= 1.5`
- AWS CLI configured (`aws sts get-caller-identity` should succeed)
- An IAM principal with permissions to create VPC / ECS / ECR / IAM / S3 resources

### 3.1 Bootstrap remote state (once per AWS account)

```bash
cd terraform/bootstrap
cp terraform.tfvars.example terraform.tfvars
# edit terraform.tfvars and set a globally unique state_bucket_name

terraform init
terraform apply
```

This creates the S3 state bucket. State locking is not configured —
serialize your `terraform apply` runs (single operator or single CI job
at a time).

### 3.2 Wire the backend into the prod environment

Open `terraform/environments/prod/backend.tf` and replace the placeholder
`bucket` value with what bootstrap created. Then:

```bash
cd terraform/environments/prod
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan
terraform apply
```

### 3.3 Capture outputs for GitHub Actions

```bash
terraform output -json
```

Pipe these into GitHub Actions environment variables / repo variables:

| Terraform output             | Suggested GitHub variable     |
| ---------------------------- | ----------------------------- |
| `aws_region`                 | `AWS_REGION`                  |
| `ecr_repository_name`        | `ECR_REPOSITORY`              |
| `ecr_repository_url`         | `ECR_REPOSITORY_URL`          |
| `ecs_cluster_name`           | `ECS_CLUSTER`                 |
| `ecs_service_name`           | `ECS_SERVICE`                 |
| `ecs_task_definition_family` | `ECS_TASK_FAMILY`             |
| `ecs_container_name`         | `ECS_CONTAINER_NAME`          |
| `alb_dns_name`               | `ALB_DNS_NAME`                |

---

## 4. ECS deployment behavior

The first `terraform apply` provisions the cluster, task definition, and
service using a **placeholder bootstrap image** (`public.ecr.aws/docker/library/nginx:alpine`)
so the service object exists before any application image has been built.

Two important `lifecycle` choices on `aws_ecs_service`:

- `ignore_changes = [task_definition, desired_count]` — once CI/CD owns
  rollouts and scaling, Terraform must not roll the service back to the
  bootstrap revision on the next `apply`.
- `deployment_circuit_breaker { enable = true, rollback = true }` — a
  failed rollout is automatically rolled back to the last known-good
  revision.

> **Note.** Until GitHub Actions publishes the real image, target group
> health checks at `/actuator/health` will fail (the placeholder doesn't
> serve that path). Tasks will keep restarting and the service will be
> unhealthy. Ship the first real image as soon as `terraform apply`
> finishes; this is expected and not a misconfiguration.

---

## 5. GitHub Actions integration

A typical workflow (kept in `.github/workflows/deploy.yml` of your app
repo, **not** provisioned by this Terraform) does the following:

1. **Auth to AWS** with OIDC via `aws-actions/configure-aws-credentials`.
2. **Login to ECR** with `aws-actions/amazon-ecr-login`.
3. **Build & push** the Docker image, tagged with the commit SHA, to
   `${ECR_REPOSITORY_URL}`.
4. **Render a new task definition** — fetch the current task definition
   for `${ECS_TASK_FAMILY}`, replace the image for container
   `${ECS_CONTAINER_NAME}` using
   `aws-actions/amazon-ecs-render-task-definition`.
5. **Deploy** with `aws-actions/amazon-ecs-deploy-task-definition` to
   `${ECS_CLUSTER}` / `${ECS_SERVICE}`, waiting for service stability.

The IAM principal used by the workflow needs:

- `ecr:GetAuthorizationToken`, `ecr:BatchCheckLayerAvailability`,
  `ecr:PutImage`, `ecr:InitiateLayerUpload`, `ecr:UploadLayerPart`,
  `ecr:CompleteLayerUpload` on the repo
- `ecs:DescribeServices`, `ecs:DescribeTaskDefinition`,
  `ecs:RegisterTaskDefinition`, `ecs:UpdateService` on the cluster/service
- `iam:PassRole` on `task_execution_role` and `task_role`

For OIDC, configure the role's trust policy to allow
`token.actions.githubusercontent.com` for your specific repo and branches.

---

## 6. Security considerations

- **No public ECS tasks.** Tasks run in private subnets with
  `assign_public_ip = false`. All inbound traffic must traverse the ALB.
- **Defense in depth on SGs.** ECS only accepts the container port from
  the ALB SG (referenced by SG ID, not CIDR), which means even a
  compromised peer in the VPC can't reach tasks directly.
- **Image scanning.** ECR scans on push.
- **State security.** Remote state bucket is private, versioned, and
  encrypted. State locking is not configured (cost choice — see
  *Limitations*); rely on serialized applies and S3 versioning for
  recovery.
- **Logs.** CloudWatch retention is bounded (default 30 days) so logs
  don't accumulate indefinitely.
- **Drop invalid headers.** `drop_invalid_header_fields = true` on the
  ALB to reduce request smuggling risk.
- **TLS.** Currently only HTTP/80 is configured (see *Limitations*).




## Infrastructure status

- ✅ **Terraform** — EC2 instance, security group, and key pair are provisioned. Bash install script (Docker) runs automatically via userdata.
- 🔄 **Docker** — Dockerfile written, image built and pushed to Docker Hub.
- 🔄 **Jenkins** — pipeline setup in progress.
- ⏳ **Ansible** — playbook development in progress, running against the provisioned EC2 instance.

## How to run Terraform

```bash
cd infra
terraform init
terraform plan
terraform apply
```

After apply completes, retrieve the instance IP:

```bash
terraform output instance_public_ip
```

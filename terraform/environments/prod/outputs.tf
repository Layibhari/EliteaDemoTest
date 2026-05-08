# Outputs consumed by GitHub Actions (and operators).
#
# Surface them as workflow variables / repo secrets in your CI/CD config.

output "aws_region" {
  description = "AWS region the stack is deployed to."
  value       = var.aws_region
}

output "ecr_repository_name" {
  description = "Name of the ECR repository (used by `aws ecr get-login-password` and image tags)."
  value       = module.ecr.repository_name
}

output "ecr_repository_url" {
  description = "Full ECR repository URL used as the Docker image registry."
  value       = module.ecr.repository_url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster."
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Name of the ECS service GitHub Actions updates on each deploy."
  value       = module.ecs.service_name
}

output "ecs_task_definition_family" {
  description = "Task definition family. CI/CD registers new revisions under this family."
  value       = module.ecs.task_definition_family
}

output "ecs_container_name" {
  description = "Name of the application container, used by aws-actions/amazon-ecs-render-task-definition."
  value       = module.ecs.container_name
}

output "alb_dns_name" {
  description = "Public DNS name of the ALB. Hit http://<alb_dns_name>/ to reach the app."
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Hosted zone ID of the ALB (for Route 53 alias records)."
  value       = module.alb.alb_zone_id
}

output "vpc_id" {
  description = "ID of the VPC."
  value       = module.vpc.vpc_id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets ECS runs in."
  value       = module.vpc.private_subnet_ids
}

output "public_subnet_ids" {
  description = "IDs of the public subnets the ALB lives in."
  value       = module.vpc.public_subnet_ids
}

output "cloudwatch_log_group" {
  description = "Name of the CloudWatch log group for application logs."
  value       = module.cloudwatch.log_group_name
}

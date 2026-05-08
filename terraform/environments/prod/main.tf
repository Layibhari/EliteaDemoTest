
# Cloud Clinic - Production environment composition

locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    Owner       = var.owner
    ManagedBy   = "terraform"
  }
}


# VPC + Networking


module "vpc" {
  source = "../../modules/vpc"

  name_prefix          = local.name_prefix
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  tags                 = local.common_tags
}

module "networking" {
  source = "../../modules/networking"

  name_prefix        = local.name_prefix
  vpc_id             = module.vpc.vpc_id
  public_subnet_ids  = module.vpc.public_subnet_ids
  private_subnet_ids = module.vpc.private_subnet_ids
  tags               = local.common_tags
}


# Security Groups

module "security_groups" {
  source = "../../modules/security-groups"

  name_prefix    = local.name_prefix
  vpc_id         = module.vpc.vpc_id
  container_port = var.container_port
  tags           = local.common_tags
}


# ECR (image registry)

module "ecr" {
  source = "../../modules/ecr"

  repository_name      = local.name_prefix
  image_tag_mutability = "MUTABLE"
  max_image_count      = var.ecr_max_image_count
  tags                 = local.common_tags
}


# IAM roles

module "iam" {
  source = "../../modules/iam"

  name_prefix = local.name_prefix
  tags        = local.common_tags
}


# CloudWatch log group


module "cloudwatch" {
  source = "../../modules/cloudwatch"

  log_group_name    = "/ecs/${local.name_prefix}"
  retention_in_days = var.log_retention_in_days
  tags              = local.common_tags
}

# Application Load Balancer

module "alb" {
  source = "../../modules/alb"

  name_prefix                = local.name_prefix
  vpc_id                     = module.vpc.vpc_id
  public_subnet_ids          = module.vpc.public_subnet_ids
  alb_security_group_id      = module.security_groups.alb_security_group_id
  container_port             = var.container_port
  health_check_path          = var.health_check_path
  enable_deletion_protection = var.alb_deletion_protection
  tags                       = local.common_tags
}

# ECS Fargate


module "ecs" {
  source = "../../modules/ecs"

  name_prefix             = local.name_prefix
  aws_region              = var.aws_region
  private_subnet_ids      = module.vpc.private_subnet_ids
  ecs_security_group_id   = module.security_groups.ecs_security_group_id
  task_execution_role_arn = module.iam.task_execution_role_arn
  task_role_arn           = module.iam.task_role_arn
  target_group_arn        = module.alb.target_group_arn
  alb_listener_arn        = module.alb.listener_arn
  log_group_name          = module.cloudwatch.log_group_name

  container_name        = "app"
  container_image       = "public.ecr.aws/docker/library/nginx:alpine"
  container_port        = var.container_port
  task_cpu              = var.task_cpu
  task_memory           = var.task_memory
  desired_count         = var.desired_count
  health_check_path     = var.health_check_path
  environment_variables = var.environment_variables

  tags = local.common_tags
}

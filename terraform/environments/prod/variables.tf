# Top-level inputs for the prod environment


variable "aws_region" {
  description = "AWS region for all resources."
  type        = string
  default     = "us-west-2"
}

variable "project_name" {
  description = "Short project name used in resource names and tags."
  type        = string
  default     = "cloudclinic"
}

variable "environment" {
  description = "Logical environment name (used in tags and resource names)."
  type        = string
  default     = "prod"
}

variable "owner" {
  description = "Tag identifying the team / owner of the resources."
  type        = string
  default     = "platform-team"
}


# Network sizing


variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "availability_zones" {
  description = "AZs to spread subnets across. Must be at least two."
  type        = list(string)
  default     = ["us-west-2a", "us-west-2b"]
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets, one per AZ."
  type        = list(string)
  default     = ["10.20.0.0/24", "10.20.1.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets, one per AZ."
  type        = list(string)
  default     = ["10.20.10.0/24", "10.20.11.0/24"]
}


# Application


variable "container_port" {
  description = "Port the Spring Boot app listens on."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "HTTP health check path."
  type        = string
  default     = "/actuator/health"
}

variable "task_cpu" {
  description = "Fargate task CPU units."
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate task memory (MiB)."
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of running ECS task replicas."
  type        = number
  default     = 2
}

variable "log_retention_in_days" {
  description = "CloudWatch log retention."
  type        = number
  default     = 30
}

variable "ecr_max_image_count" {
  description = "Maximum number of tagged images retained in ECR."
  type        = number
  default     = 20
}

variable "alb_deletion_protection" {
  description = "Whether to enable ALB deletion protection."
  type        = bool
  default     = true
}

variable "environment_variables" {
  description = "Environment variables passed to the application container."
  type        = map(string)
  default = {
    SPRING_PROFILES_ACTIVE = "prod"
    SERVER_PORT            = "8080"
  }
}

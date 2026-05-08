variable "name_prefix" {
  description = "Prefix used for ECS resource names."
  type        = string
}

variable "aws_region" {
  description = "AWS region (used in awslogs configuration)."
  type        = string
}

###############################################################################
# Networking
###############################################################################

variable "private_subnet_ids" {
  description = "Subnets (private) the ECS tasks run in."
  type        = list(string)
}

variable "ecs_security_group_id" {
  description = "Security group ID assigned to ECS tasks."
  type        = string
}

###############################################################################
# IAM
###############################################################################

variable "task_execution_role_arn" {
  description = "ARN of the ECS task execution role."
  type        = string
}

variable "task_role_arn" {
  description = "ARN of the ECS task role."
  type        = string
}

###############################################################################
# Load balancer integration
###############################################################################

variable "target_group_arn" {
  description = "ARN of the ALB target group the service registers into."
  type        = string
}

variable "alb_listener_arn" {
  description = "ARN of the ALB listener (used as a dependency to avoid races)."
  type        = string
}

###############################################################################
# Logging
###############################################################################

variable "log_group_name" {
  description = "CloudWatch log group used by the awslogs log driver."
  type        = string
}

###############################################################################
# Container / task sizing
###############################################################################

variable "container_name" {
  description = "Name of the application container in the task definition."
  type        = string
  default     = "app"
}

variable "container_image" {
  description = "Initial container image used for bootstrapping. CI/CD will replace this."
  type        = string
}

variable "container_port" {
  description = "Port the container listens on."
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "CPU units for the Fargate task (e.g. 512, 1024)."
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Memory (MiB) for the Fargate task."
  type        = number
  default     = 1024
}

variable "cpu_architecture" {
  description = "CPU architecture for the Fargate task."
  type        = string
  default     = "X86_64"

  validation {
    condition     = contains(["X86_64", "ARM64"], var.cpu_architecture)
    error_message = "cpu_architecture must be X86_64 or ARM64."
  }
}

variable "desired_count" {
  description = "Number of running task replicas at bootstrap time."
  type        = number
  default     = 2
}

variable "health_check_path" {
  description = "HTTP path used by the container-level health check."
  type        = string
  default     = "/actuator/health"
}

variable "environment_variables" {
  description = "Environment variables passed to the container."
  type        = map(string)
  default     = {}
}

variable "enable_execute_command" {
  description = "Whether to allow ECS Exec into running tasks."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Common tags applied to all resources."
  type        = map(string)
  default     = {}
}

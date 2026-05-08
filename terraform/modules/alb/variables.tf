variable "name_prefix" {
  description = "Prefix used for resource names."
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC where the target group lives."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs the ALB attaches to."
  type        = list(string)
}

variable "alb_security_group_id" {
  description = "Security group ID assigned to the ALB."
  type        = string
}

variable "container_port" {
  description = "Port the application listens on."
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "HTTP path the target group uses for health checks."
  type        = string
  default     = "/actuator/health"
}

variable "enable_deletion_protection" {
  description = "Whether to enable deletion protection on the ALB."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Common tags applied to all resources."
  type        = map(string)
  default     = {}
}

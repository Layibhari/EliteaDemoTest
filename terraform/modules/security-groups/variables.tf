variable "name_prefix" {
  description = "Prefix used for resource names."
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC where security groups are created."
  type        = string
}

variable "container_port" {
  description = "Port the application container listens on."
  type        = number
  default     = 8080
}

variable "tags" {
  description = "Common tags applied to all resources."
  type        = map(string)
  default     = {}
}

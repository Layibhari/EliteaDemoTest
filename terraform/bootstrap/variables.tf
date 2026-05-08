variable "aws_region" {
  description = "AWS region the state bucket lives in."
  type        = string
  default     = "us-west-2"
}

variable "project_name" {
  description = "Project name used in tags."
  type        = string
  default     = "cloudclinic"
}

variable "state_bucket_name" {
  description = "Globally unique S3 bucket name for Terraform remote state."
  type        = string
}

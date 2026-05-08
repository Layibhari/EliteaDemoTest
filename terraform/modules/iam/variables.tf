variable "name_prefix" {
  description = "Prefix used for IAM role names."
  type        = string
}

variable "secret_arns" {
  description = "Optional ARNs of Secrets Manager / SSM Parameter Store entries the task execution role should be allowed to read."
  type        = list(string)
  default     = []
}

variable "tags" {
  description = "Common tags applied to all resources."
  type        = map(string)
  default     = {}
}

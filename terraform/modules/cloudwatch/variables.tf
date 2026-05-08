variable "log_group_name" {
  description = "Name of the CloudWatch log group."
  type        = string
}

variable "retention_in_days" {
  description = "Number of days log events are retained."
  type        = number
  default     = 30

  validation {
    condition = contains(
      [1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, 3653],
      var.retention_in_days
    )
    error_message = "retention_in_days must be one of the values supported by CloudWatch Logs."
  }
}

variable "kms_key_arn" {
  description = "Optional KMS key ARN for log encryption. Defaults to AWS-managed encryption when null."
  type        = string
  default     = null
}

variable "tags" {
  description = "Common tags applied to all resources."
  type        = map(string)
  default     = {}
}

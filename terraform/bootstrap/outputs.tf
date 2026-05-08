output "state_bucket_name" {
  description = "S3 bucket name to put in environment backend.tf files."
  value       = aws_s3_bucket.state.bucket
}

output "state_bucket_arn" {
  description = "ARN of the state bucket."
  value       = aws_s3_bucket.state.arn
}

###############################################################################
# Remote state bootstrap
#
# Provisions the S3 bucket that the per-environment stacks reference in
# their `backend "s3"` blocks.
#
# State locking via DynamoDB is intentionally NOT provisioned to avoid
# the table cost; we rely on solo-operator workflows + S3 versioning to
# recover from concurrent-write mistakes.
#
# Apply this stack ONCE per AWS account, with a local backend, BEFORE any
# environment stack:
#
#   cd terraform/bootstrap
#   terraform init
#   terraform apply
#
# Then update terraform/environments/prod/backend.tf with the bucket name
# this stack outputs and run `terraform init` in the prod directory.
###############################################################################

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

###############################################################################
# State bucket
###############################################################################

resource "aws_s3_bucket" "state" {
  bucket = var.state_bucket_name

  tags = {
    Name      = var.state_bucket_name
    Project   = var.project_name
    Purpose   = "terraform-remote-state"
    ManagedBy = "terraform"
  }
}

resource "aws_s3_bucket_versioning" "state" {
  bucket = aws_s3_bucket.state.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "state" {
  bucket = aws_s3_bucket.state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "state" {
  bucket = aws_s3_bucket.state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "state" {
  bucket = aws_s3_bucket.state.id

  rule {
    id     = "expire-noncurrent"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = 90
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}


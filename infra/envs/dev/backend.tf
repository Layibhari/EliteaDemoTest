terraform {
  backend "s3" {
    bucket         = "petclinic-terraform-state-1"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "petclinic-terraform-locks"
  }
}

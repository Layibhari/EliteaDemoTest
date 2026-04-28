terraform {
  backend "s3" {
    bucket         = "petclinic-terraform-backend"
    key            = "dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-lock"
  }
}

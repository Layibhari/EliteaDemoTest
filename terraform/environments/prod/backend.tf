terraform {
  backend "s3" {
    bucket  = "cloudclinic-tfstate-1234"   
    key     = "prod/terraform.tfstate"             # path inside the bucket; fine as-is
    region  = "us-west-2"                          # must match the bucket's region
    encrypt = true                                 # SSE on the state object
  }
}
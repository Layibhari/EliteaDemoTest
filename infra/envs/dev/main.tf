provider "aws" {
  region = "us-east-1"
}

module "vpc" {
  source = "../../modules/vpc"
}

module "ecr" {
  source = "../../modules/ecr"
}

module "rds" {
  source     = "../../modules/rds"
  subnet_ids = module.vpc.private_subnets
  vpc_id     = module.vpc.vpc_id
}

module "eks" {
  source     = "../../modules/eks"
  subnet_ids = module.vpc.private_subnets
}


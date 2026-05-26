terraform {
  required_version = ">= 1.5.0"

  required_providers {
    # azurerm = {
    #   source  = "hashicorp/azurerm"
    #   version = "~> 3.100"
    # }

    flux = {
      source  = "fluxcd/flux"
      version = "~> 1.7"
    }
  }
}

provider "azurerm" {
  features {}
  subscription_id = var.subscription_id
}

# provider "flux" {
#   kubernetes = {
#     config_path = "~/.kube/config"
#   }

#   git = {
#     url = var.flux_git_repository_url

#     branch = var.flux_git_branch

#     http = {
#       username = var.flux_git_username
#       password = var.flux_git_token
#     }
#   }
# }

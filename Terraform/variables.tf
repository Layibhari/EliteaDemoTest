variable "subscription_id" {
  description = "Azure subscription ID"
  type        = string
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "westeurope"
}

variable "resource_group_name" {
  description = "Resource group name"
  type        = string
  default     = "rg-aks-demo"
}

variable "vnet_name" {
  description = "Virtual network name"
  type        = string
  default     = "vnet-aks-demo"
}

variable "vnet_address_space" {
  description = "VNet address space"
  type        = string
  default     = "10.10.0.0/16"
}

variable "aks_subnet_name" {
  description = "AKS subnet name"
  type        = string
  default     = "snet-aks"
}

variable "aks_subnet_address_prefix" {
  description = "AKS subnet address prefix"
  type        = string
  default     = "10.10.1.0/24"
}

variable "aks_cluster_name" {
  description = "AKS cluster name"
  type        = string
  default     = "aks-demo"
}

variable "aks_dns_prefix" {
  description = "AKS DNS prefix"
  type        = string
  default     = "aks-demo"
}

variable "pod_cidr" {
  description = "Pod CIDR for Azure CNI Overlay"
  type        = string
  default     = "192.168.0.0/16"
}

variable "service_cidr" {
  description = "Kubernetes service CIDR"
  type        = string
  default     = "10.20.0.0/16"
}

variable "dns_service_ip" {
  description = "Kubernetes DNS service IP"
  type        = string
  default     = "10.20.0.10"
}

variable "environment" {
  description = "Environment tag"
  type        = string
  default     = "dev"
}
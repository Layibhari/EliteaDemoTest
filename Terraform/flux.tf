# resource "flux_bootstrap_git" "this" {
#   path = "clusters/aks-demo"

#   depends_on = [
#     azurerm_kubernetes_cluster.main
#   ]
# }
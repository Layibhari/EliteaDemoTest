resource "aws_eks_cluster" "cluster" {
  name     = "petclinic-cluster"
  role_arn = aws_iam_role.eks_role.arn

  vpc_config {
    subnet_ids = var.subnet_ids
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy
  ]
}

resource "aws_eks_node_group" "nodes" {
  cluster_name    = aws_eks_cluster.cluster.name
  node_group_name = "petclinic-nodes"
  node_role_arn   = aws_iam_role.node_role.arn
  subnet_ids      = var.subnet_ids
  instance_types  = ["t3.small"]

  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 1
  }
}

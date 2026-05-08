###############################################################################
# IAM Module - ECS roles
#
#   - task_execution_role : used by the ECS agent itself to pull images from
#                           ECR and write logs to CloudWatch.
#                           Receives the AWS-managed
#                           AmazonECSTaskExecutionRolePolicy.
#
#   - task_role           : used by the running container to call AWS APIs.
#                           Starts empty (least privilege); attach inline
#                           policies as the application needs them.
###############################################################################

data "aws_iam_policy_document" "ecs_tasks_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

###############################################################################
# Task execution role
###############################################################################

resource "aws_iam_role" "task_execution" {
  name               = "${var.name_prefix}-ecs-task-exec"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "task_execution_managed" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Optional: grant the execution role access to read application secrets.
# Wired in only if `secret_arns` is non-empty.
data "aws_iam_policy_document" "task_execution_secrets" {
  count = length(var.secret_arns) > 0 ? 1 : 0

  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "ssm:GetParameters",
      "kms:Decrypt"
    ]
    resources = var.secret_arns
  }
}

resource "aws_iam_role_policy" "task_execution_secrets" {
  count = length(var.secret_arns) > 0 ? 1 : 0

  name   = "${var.name_prefix}-ecs-task-exec-secrets"
  role   = aws_iam_role.task_execution.id
  policy = data.aws_iam_policy_document.task_execution_secrets[0].json
}

###############################################################################
# Task role - empty by default, runtime permissions go here
###############################################################################

resource "aws_iam_role" "task" {
  name               = "${var.name_prefix}-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = var.tags
}

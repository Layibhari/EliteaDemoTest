###############################################################################
# ECS Module - Cluster, Task Definition, Service (Fargate)
#
#   - aws_ecs_cluster      : Container Insights enabled.
#   - aws_ecs_task_definition : Single container, awslogs driver, runs as
#                            non-root via the image's USER directive.
#   - aws_ecs_service      : Fargate, deployed in private subnets, registered
#                            against the ALB target group, rolling deployments.
#
# A bootstrap container image is referenced in the task definition so that
# `terraform apply` can succeed before any application image has been
# published. GitHub Actions will later push a real image to ECR and update
# the service.
#
# `lifecycle.ignore_changes` on `task_definition` and `desired_count`
# prevents Terraform from fighting CI/CD updates after the first deploy.
###############################################################################

locals {
  container_name = var.container_name
}

resource "aws_ecs_cluster" "this" {
  name = "${var.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-cluster"
    }
  )
}

resource "aws_ecs_cluster_capacity_providers" "this" {
  cluster_name = aws_ecs_cluster.this.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
    base              = 1
  }
}

###############################################################################
# Task Definition
###############################################################################

resource "aws_ecs_task_definition" "this" {
  family                   = "${var.name_prefix}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = var.task_execution_role_arn
  task_role_arn            = var.task_role_arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = var.cpu_architecture
  }

  container_definitions = jsonencode([
    {
      name      = local.container_name
      image     = var.container_image
      essential = true

      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]

      environment = [
        for k, v in var.environment_variables : {
          name  = k
          value = v
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = var.log_group_name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = "ecs"
        }
      }

      healthCheck = {
        command = [
          "CMD-SHELL",
          "wget -q -O - http://localhost:${var.container_port}${var.health_check_path} || exit 1"
        ]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-task"
    }
  )
}

###############################################################################
# Service
###############################################################################

resource "aws_ecs_service" "this" {
  name            = "${var.name_prefix}-service"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.this.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  enable_execute_command = var.enable_execute_command

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.ecs_security_group_id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = local.container_name
    container_port   = var.container_port
  }

  # Avoid races where the service comes up before the ALB listener is ready
  # to register targets.
  depends_on = [var.alb_listener_arn]

  # Once CI/CD takes over, image rollouts update the task definition and
  # desired_count out-of-band. Ignore those drifts so terraform apply
  # doesn't roll the service back to the bootstrap image.
  lifecycle {
    ignore_changes = [
      task_definition,
      desired_count,
    ]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-service"
    }
  )
}

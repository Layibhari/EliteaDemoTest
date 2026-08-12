#resource "aws_iam_role" "ec2_cloudwatch_role" {
#  name = "petclinic-ec2-cloudwatch-role"
#
#  assume_role_policy = jsonencode({
#    Version = "2012-10-17"
#    Statement = [
#      {
#        Action = "sts:AssumeRole"
#        Effect = "Allow"
#        Principal = {
#          Service = "ec2.amazonaws.com"
#        }
#      }
#    ]
#  })
#}
#
#resource "aws_iam_role_policy_attachment" "cloudwatch_agent_policy" {
#  role       = aws_iam_role.ec2_cloudwatch_role.name
#  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
#}

#resource "aws_iam_instance_profile" "ec2_cloudwatch_profile" {
#  name = "petclinic-ec2-cloudwatch-profile"
#  role = aws_iam_role.ec2_cloudwatch_role.name
#}

#resource "aws_cloudwatch_log_group" "petclinic_logs" {
#  name              = "/petclinic/app-logs"
#  retention_in_days = 7
#}

resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  alarm_name          = "petclinic-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 120
  statistic           = "Average"
  threshold           = 80

  dimensions = {
    InstanceId = aws_instance.petclinic.id
  }

  alarm_description = "Triggers when CPU exceeds 80% for two consecutive periods"
}

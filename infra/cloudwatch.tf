resource "aws_cloudwatch_log_group" "petclinic_logs" {
  name              = "/petclinic/app-logs"
}

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

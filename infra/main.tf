data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_instance" "petclinic" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = "t3.medium"
  key_name               = aws_key_pair.petclinic_key.key_name
  vpc_security_group_ids = [aws_security_group.petclinic_sg.id]
  iam_instance_profile  = "Team4-SSM-CWlogs-role"
  user_data = file("${path.module}/install.sh")

  tags = {
    Name = "petclinic-instance"
  }
}

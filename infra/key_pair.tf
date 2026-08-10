resource "aws_key_pair" "petclinic_key" {
  key_name   = "petclinic-key"
  public_key = file("~/.ssh/petclinic-key.pub")

  tags = {
    Name = "petclinic-key"
  }
}

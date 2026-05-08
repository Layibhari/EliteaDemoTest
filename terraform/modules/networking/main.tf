###############################################################################
# Networking Module - IGW, NAT Gateway, Route Tables, Associations
#
# Adds the connectivity layer on top of the VPC + subnets created by the
# `vpc` module:
#   - Internet Gateway for public subnets
#   - Single NAT Gateway in the first public subnet (cost-optimized)
#   - Public route table -> IGW
#   - Private route table -> NAT
###############################################################################

resource "aws_internet_gateway" "this" {
  vpc_id = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-igw"
    }
  )
}

# Elastic IP for the NAT Gateway. Single NAT for cost optimization in
# this prod environment; for stricter HA, deploy one NAT per AZ.
resource "aws_eip" "nat" {
  domain = "vpc"

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-nat-eip"
    }
  )

  depends_on = [aws_internet_gateway.this]
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = var.public_subnet_ids[0]

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-nat"
    }
  )

  depends_on = [aws_internet_gateway.this]
}

###############################################################################
# Public route table -> IGW
###############################################################################

resource "aws_route_table" "public" {
  vpc_id = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-public-rt"
    }
  )
}

resource "aws_route" "public_internet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public" {
  count = length(var.public_subnet_ids)

  subnet_id      = var.public_subnet_ids[count.index]
  route_table_id = aws_route_table.public.id
}

###############################################################################
# Private route table -> NAT
###############################################################################

resource "aws_route_table" "private" {
  vpc_id = var.vpc_id

  tags = merge(
    var.tags,
    {
      Name = "${var.name_prefix}-private-rt"
    }
  )
}

resource "aws_route" "private_nat" {
  route_table_id         = aws_route_table.private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id         = aws_nat_gateway.this.id
}

resource "aws_route_table_association" "private" {
  count = length(var.private_subnet_ids)

  subnet_id      = var.private_subnet_ids[count.index]
  route_table_id = aws_route_table.private.id
}

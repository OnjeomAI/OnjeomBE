# =============================================
# Provider 설정
# =============================================
provider "aws" {
  region = "ap-northeast-2"
}

# =============================================
# VPC
# =============================================
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = { Name = "onjeom-vpc" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "onjeom-igw" }
}

resource "aws_subnet" "public_1" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = true
  tags                    = { Name = "onjeom-public-1" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = { Name = "onjeom-public-rt" }
}

resource "aws_route_table_association" "public_1" {
  subnet_id      = aws_subnet.public_1.id
  route_table_id = aws_route_table.public.id
}

# =============================================
# 보안 그룹
# =============================================
resource "aws_security_group" "ec2" {
  name        = "onjeom-ec2-sg"
  description = "Security group for EC2"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Spring Boot"
  }

  # SSH는 보안상 본인 IP만 허용 권장
  # cidr_blocks = ["본인IP/32"] 로 변경 추천
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "onjeom-ec2-sg" }
}

# =============================================
# IAM - EC2가 ECR pull 할 수 있도록
# =============================================
resource "aws_iam_role" "ec2_role" {
  name = "onjeom-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ssm_policy" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "cloudwatch_agent" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

resource "aws_iam_role_policy" "ecr_policy" {
  name = "ecr-pull-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ]
      Resource = "*"
    }]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "onjeom-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# =============================================
# 키 페어
# 터미널: ssh-keygen -t rsa -b 4096 -f onjeom-key -N ""
# =============================================
resource "aws_key_pair" "onjeom" {
  key_name   = "onjeom-key"
  public_key = file("${path.module}/onjeom-key.pub")
}

# =============================================
# EC2 (프리티어: t2.micro / gp2 30GB)
# =============================================
resource "aws_instance" "app" {
  ami           = "ami-0e9bfdb247cc8de84" # Ubuntu 22.04 LTS (서울)
  instance_type = "t2.micro"              # 프리티어
  subnet_id     = aws_subnet.public_1.id
  monitoring    = false                   # 세부 모니터링 OFF (과금 주의)

  vpc_security_group_ids = [aws_security_group.ec2.id]
  key_name               = aws_key_pair.onjeom.key_name
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size = 30    # 프리티어 최대
    volume_type = "gp2" # gp3는 과금 주의
  }

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # 패키지 업데이트
    apt-get update -y

    # SSM Agent (AWS 콘솔에서 SSH 없이 접속 가능)
    snap install amazon-ssm-agent --classic
    systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service
    systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service

    # AWS CLI
    apt-get install -y unzip curl
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    ./aws/install

    # Docker 설치
    apt-get install -y ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    usermod -aG docker ubuntu
    systemctl enable docker
    systemctl start docker

    # Nginx 설치
    apt-get install -y nginx
    systemctl enable nginx
    systemctl start nginx

    # 앱 디렉토리 생성
    mkdir -p /home/ubuntu/app/logs
    chown -R ubuntu:ubuntu /home/ubuntu/app

    echo "EC2 초기 세팅 완료!"
  EOF

  tags = { Name = "onjeom-app" }
}

# =============================================
# 탄력적 IP (인스턴스 연결 상태면 무료)
# =============================================
resource "aws_eip" "app" {
  instance   = aws_instance.app.id
  domain     = "vpc"
  depends_on = [aws_internet_gateway.main]
  tags       = { Name = "onjeom-app-eip" }
}

# =============================================
# ECR (월 500MB 무료)
# =============================================
resource "aws_ecr_repository" "backend" {
  name         = "onjeom-backend"
  force_delete = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = { Name = "onjeom-backend" }
}

# 최근 3개 이미지만 유지
resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "최근 3개 이미지만 유지"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 3
      }
      action = { type = "expire" }
    }]
  })
}

# =============================================
# 출력값 → GitHub Secrets에 등록할 값들
# =============================================
output "public_ip" {
  value       = aws_eip.app.public_ip
  description = "EC2 공인 IP → GitHub Secret: EC2_HOST"
}

output "ssh_command" {
  value       = "ssh -i onjeom-key.pem ubuntu@${aws_eip.app.public_ip}"
  description = "SSH 접속 명령어"
}

output "instance_id" {
  value       = aws_instance.app.id
  description = "EC2 인스턴스 ID"
}

output "ecr_repository_url" {
  value       = aws_ecr_repository.backend.repository_url
  description = "ECR URL → GitHub Secret: ECR_REGISTRY"
}



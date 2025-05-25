variable "region" {
  description = "Região AWS onde os recursos serão criados"
  type        = string
}

variable "ecr_repository_names" {
  description = "Nome dos repositórios ECR"
  type        = list(string)
}

variable "micro_services" {
  description = "Micro serviços da autorização de débito"
  type        = list(string)
}

variable "poc_metrics_ecr_repository" {
  description = "Nome do repositório ECR para o serviço"
  type        = string
}

variable "vpc_id" {
  description = "ID da VPC onde o NLB será criado"
  type        = string
}

variable "public_subnets" {
  description = "Lista de subnets públicas para o NLB"
  type        = list(string)
}

variable "datadog_api_key" {
  description = "Api key datadog"
  type        = string
}


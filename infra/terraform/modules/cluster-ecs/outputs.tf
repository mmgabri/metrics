output "ecs_cluster_poc_metrics" {
  description = "Nome do cluster ECS"
  value       = aws_ecs_cluster.fargate_cluster.name
}
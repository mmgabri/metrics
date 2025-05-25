#------------------------------------------------------------------------------
# AWS Task Definition
#------------------------------------------------------------------------------
resource "aws_ecs_task_definition" "service_task" {
  family             = "${var.micro_service_name}-task"
  requires_compatibilities = ["FARGATE"]
  network_mode       = "awsvpc"
  cpu                = var.cpu
  memory             = var.memory
  execution_role_arn = var.execution_role_arn
  task_role_arn      = var.task_role_arn

  container_definitions = jsonencode([
    # -------------------------------
    # Aplicação Spring Boot
    # -------------------------------
    {
      name  = var.micro_service_name
      image = "${var.ecr_repository}:latest"
      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.host_port
        }
      ]
      environment = [
        { name = "DD_AGENT_HOST", value = "localhost" },
        { name = "DD_TRACE_AGENT_PORT", value = "8126" },
        { name = "DD_SERVICE", value = var.micro_service_name },
        { name = "DD_ENV", value = "producao" },
        { name = "DD_VERSION", value = "1.0.0" },
        { name = "DD_JMXFETCH_ENABLED", value = "true" },  # Habilitar coleta de métricas JMX
        { name = "DD_JMXFETCH_STATSD_HOST", value = "localhost" },
        { name = "DD_JMXFETCH_STATSD_PORT", value = "8125" },
        { name = "DD_API_KEY", value = var.datadog_api_key },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/${var.micro_service_name}-logs"
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = var.micro_service_name
        }
      }
      healthCheck = {
        command = [
          "CMD-SHELL",
          "curl --silent --fail http://localhost:${var.container_port}/actuator/health || exit 1"
        ]
        interval    = 30
        retries     = 3
        startPeriod = 60
        timeout     = 10
      }
    },
    # -------------------------------
    # Datadog Agent Sidecar
    # -------------------------------
    {
      name  = "datadog-agent"
      image = "public.ecr.aws/datadog/agent:latest"
      essential = true
      environment = [
        { name = "ECS_FARGATE", value = "true" },
        { name = "DD_API_KEY", value = var.datadog_api_key },
        { name = "DD_SITE", value = "us5.datadoghq.com" },
            { name = "DD_APM_ENABLED", value = "true" },
        { name = "DD_LOGS_ENABLED", value = "true" },
        { name = "DD_DOGSTATSD_NON_LOCAL_TRAFFIC", value = "true" },
        { name = "DD_JMXFETCH_ENABLED", value = "true" },  # Habilitar coleta de métricas JMX
        { name = "DD_JMXFETCH_STATSD_HOST", value = "localhost" },
        { name = "DD_JMXFETCH_STATSD_PORT", value = "8125" },
        { name = "DD_HISTOGRAM_PERCENTILES", value = "0.99 0.95 0.90 0.50" }
      ]
      requiresCompatibilities = ["FARGATE"]
      portMappings = [
        {
          containerPort = 8126
          hostPort      = 8126
          protocol      = "tcp"
        },
        {
          containerPort = 8125
          hostPort      = 8125
          protocol      = "udp"
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/datadog-agent-logs"
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "datadog-agent"
        }
      }
      # healthCheck = {
      #   command = [
      #     "CMD-SHELL",
      #     "curl --silent --fail http://localhost:5555/health || exit 1"
      #   ]
      #   interval    = 30
      #   retries     = 3
      #   startPeriod = 60
      #   timeout     = 5
      # }
    }
  ])
}

#------------------------------------------------------------------------------
# AWS Service
#------------------------------------------------------------------------------
resource "aws_ecs_service" "service_task" {
  name            = "${var.micro_service_name}-service"
  cluster         = var.ecs_cluster_name
  task_definition = aws_ecs_task_definition.service_task.arn
  launch_type     = "FARGATE"
  desired_count   = 7

  network_configuration {
    subnets          = var.public_subnets
    security_groups  = var.security_groups
    assign_public_ip = true
  }

  dynamic "load_balancer" {
    for_each = length(var.target_group_arn) > 0 ? [var.target_group_arn[0]] : []
    content {
      target_group_arn = var.target_group_arn[0]
      container_name   = var.micro_service_name
      container_port   = var.container_port
    }
  }
}
region = "us-east-1"

vpc_id = "vpc-08b07adee0cfe9aaf"

public_subnets = [
  "subnet-085894be0866e805e",
  "subnet-074c30a196b1faad7",
  "subnet-09c05369194b4d927"
]

micro_services = [
  "poc-metrics", "datadog-agent"
]

ecr_repository_names = [
  "140023369634.dkr.ecr.us-east-1.amazonaws.com/poc-metrics"
]

poc_metrics_ecr_repository = "140023369634.dkr.ecr.us-east-1.amazonaws.com/poc-metrics"

datadog_api_key = "b194cdeecc9e1e62318497747e54eb61"
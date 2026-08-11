# Infrastructure (placeholder)

Not yet started. Per TDD Section 9.2, all AWS infrastructure (VPC, subnets,
security groups, ECS, RDS, S3, IAM roles, etc.) should be defined as
Infrastructure as Code \u2013 Terraform or AWS CDK, tool choice to be confirmed
with the Cloud/DevOps Engineer at Sprint 0 (see SDD Section 8.3).

`infra/local/` currently only contains a Postgres init script used by the root
`docker-compose.yml` for local development \u2013 that's local-dev tooling, not
IaC for AWS.

## What needs to be built here (see TDD for full detail)

| Area | TDD Reference |
|---|---|
| VPC, subnets (public/private-app/private-data), security groups | Section 3 |
| ECS Fargate cluster + 4 services | Section 4.1 |
| RDS PostgreSQL (Multi-AZ) + ElastiCache Redis | Section 4.2 |
| S3 buckets (reports, static assets) | Section 4.3 |
| API Gateway, EventBridge, SQS, Lambda (scheduled batch) | Section 4.4 |
| Cognito user pools | Section 4.5 |
| CloudFront + WAF + Route 53 + ACM | Section 4.6 |
| IAM roles (least privilege per service) | Section 6.1 |
| CI/CD pipeline definition | Section 9.1 |

## Suggested next step
Stand up the VPC/networking layer first (Section 3), since every other AWS
resource depends on it, then the ECS cluster + one service end-to-end (e.g.
vendor-offering-service) as a walking skeleton before replicating for the
other three.

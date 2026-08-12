#!/bin/sh
# Bootstraps the local EventBridge bus, SQS queue, and the rule routing
# BundleSuperseded events between them – mirrors the TDD's target AWS wiring
# (EventBridge -> SQS -> partner-subscription-service) for local dev/testing.
# Runs automatically once LocalStack is ready (mounted into /etc/localstack/init/ready.d/).
set -e

BUS_NAME="partnership-pillar-local"
QUEUE_NAME="bundle-superseded-queue"
RULE_NAME="bundle-superseded-to-sqs"

awslocal events create-event-bus --name "$BUS_NAME" 2>/dev/null || true

QUEUE_URL=$(awslocal sqs create-queue --queue-name "$QUEUE_NAME" --query 'QueueUrl' --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

awslocal events put-rule \
  --name "$RULE_NAME" \
  --event-bus-name "$BUS_NAME" \
  --event-pattern "{\"source\":[\"ecosystem-bundle-service\"],\"detail-type\":[\"BundleSuperseded\"]}"

POLICY="{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"Service\":\"events.amazonaws.com\"},\"Action\":\"sqs:SendMessage\",\"Resource\":\"${QUEUE_ARN}\"}]}"
awslocal sqs set-queue-attributes --queue-url "$QUEUE_URL" --attributes "{\"Policy\":\"$(echo "$POLICY" | sed 's/"/\\"/g')\"}"

# InputPath strips the EventBridge envelope so the SQS message body is just the
# BundleSuperseded detail payload (matching /api-contracts/bundle-superseded-event.schema.json).
TARGETS="Id=1,Arn=${QUEUE_ARN},InputPath=\$.detail"
awslocal events put-targets \
  --event-bus-name "$BUS_NAME" \
  --rule "$RULE_NAME" \
  --targets "$TARGETS"

echo "LocalStack bootstrap complete: bus=$BUS_NAME rule=$RULE_NAME queue=$QUEUE_NAME ($QUEUE_URL)"

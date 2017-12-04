variable "id" {
  default = "random"
  description = "Id to namespace the resources"
}
variable "account_id" {}
variable "profile" {
  default = "default"
}
variable "region" {
  default = "us-east-1"
}

provider "aws" {
  region = "${var.region}"
  profile = "${var.profile}"
}

# IAM role for Lambda functions w/ full DynamoDB access

resource "aws_iam_role" "lambda_role" {
  name = "lambda-role-${var.id}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

# API gateway role
resource "aws_iam_role" "api_gateway_role" {
  name = "api-gateway-lambda-invoke-role-${var.id}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "apigateway.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

# Policy to allow invocation of Lambda functions
resource "aws_iam_policy" "lambda_invoke_policy" {
  name = "lambda-invoke-policy-${var.id}"
  description = "Allows invocation of Lambda functions"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Resource": [
        "*"
      ],
      "Action": [
        "lambda:InvokeFunction"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_policy_attachment" "lambda_execution_attach" {
  name = "lambda-execution-attach-${var.id}"
  roles = [
    "${aws_iam_role.lambda_role.id}"
  ]
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Attaches Lambda invocation policy to the provides roles (primarily the API gateway role)
resource "aws_iam_policy_attachment" "lambda_invoke_policy_attach" {
  name = "lamba-invoke-policy-${var.id}"
  roles = [
    "${aws_iam_role.api_gateway_role.id}"]
  policy_arn = "${aws_iam_policy.lambda_invoke_policy.arn}"
}

resource "aws_lambda_function" "test_lambda" {
  function_name = "test-${var.id}"
  description = "Test proxy lambda"
  filename = "mock.zip"
  runtime = "java8"
  timeout = "60"
  memory_size = "256"
  handler = "handler.handler"
  role = "${aws_iam_role.lambda_role.arn}"
}

module "api_gateway" {
  source = "github.com/frankfarrell/terraform-aws-recipes//api_gateway_proxy"

  account_id = "${var.account_id}"
  lambda_function_name = "${aws_lambda_function.test_lambda.function_name}"
  gateway_lambda_execution_role_arn = "${aws_iam_role.api_gateway_role.arn}"
  create_api_key = true
}
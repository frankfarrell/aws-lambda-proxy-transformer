# AWS Lambda Proxy Transformer

## Creating the infrastructure with Terraform 

This relies on the terraform api gateway module [link]
 1. Create a vars.tfvars file with the region & account id in the terraform dir 
 2. Run `terraform plan --var-file=vars.tfvars`, to see what will be created. It ought to be all free-tier
 3. Run `terraform apply --var-file=vars.tfvars`
 
 ## Deploying the lambda
 
 Run `gradle clean build deploy`
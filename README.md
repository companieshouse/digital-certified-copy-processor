# digital-certified-copy-processor

* Consumes messages from the `item-ordered-certified-copy` Kafka topic.
* Produces messages to the `sign-digital-document` Kafka topic.

## Environment variables

| Name                                      | Description                                                                                                                  | Mandatory | Location                                |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|-----------|-----------------------------------------|
| API_URL                                   | URL to CHS API                                                                                                               | ✓         | chs-configs repo environment global_env |
| CHS_API_KEY                               | API Access Key for CHS                                                                                                       | ✓         | chs-configs repo environment global_env |
| BACKOFF_DELAY                             | The delay in milliseconds between message republish attempts.                                                                | ✓         | chs-configs repo environment global_env |
| GROUP_ID                                  | The group ID of the main consumer                                                                                            | ✓         | chs-configs repo environment global_env |
| INVALID_ITEM_ORDERED_CERTIFIED_COPY_TOPIC | The topic to which consumers will republish messages if any unchecked exception other than RetryableException is thrown.     | ✓         | chs-configs repo environment global_env |
| MAX_ATTEMPTS                              | The maximum number of times messages will be processed before they are sent to the dead letter topic.                        | ✓         | chs-configs repo environment global_env |
| CONCURRENT_LISTENER_INSTANCES             | The number of consumers that should participate in the consumer group. Must be equal to the number of main topic partitions. | ✓         | chs-configs repo environment global_env |
| ITEM_ORDERED_CERTIFIED_COPY_TOPIC         | The topic from which the main consumer will consume `item-ordered-certified-copy` messages.                                  | ✓         | chs-configs repo environment global_env |
| BOOTSTRAP_SERVER_URL                      | The URLs of the Kafka brokers that the consumers will connect to.                                                            | ✓         | chs-configs repo environment global_env |
| PAYMENTS_API_URL            | Payments API URL                        | ✓         | chs-configs repo environment global_env |
| SIGN_DIGITAL_DOCUMENT_TOPIC | The topic this app produces messages to | ✓         | chs-configs repo environment env        |
## Endpoints

| Path                                              | Method | Description                                                         |
|---------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/digital-certified-copy-processor/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |order-service                                     | ECS cluster (stack) the service belongs to
**Load balancer**      |N/A - processor service                                            | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/digital-certified-copy-processor) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/digital-certified-copy-processor)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
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


# digital-certified-copy-processor

* Consumes messages from the `item-ordered-certified-copy` Kafka topic.
* Produces messages to the `sign-digital-document` Kafka topic.

## Environment variables

| Name                        | Description                             | Mandatory | Location                                |
|-----------------------------|-----------------------------------------|-----------|-----------------------------------------|
| API_URL                     | URL to CHS API                          | ✓         | chs-configs repo environment global_env |
| BOOTSTRAP_SERVER_URL        | URL(s) of Kafka cluster(s)              | ✓         | chs-configs repo environment env        |
| CHS_API_KEY                 | API Access Key for CHS                  | ✓         | chs-configs repo environment global_env |
| PAYMENTS_API_URL            | Payments API URL                        | ✓         | chs-configs repo environment global_env |
| SIGN_DIGITAL_DOCUMENT_TOPIC | The topic this app produces messages to | ✓         | chs-configs repo environment env        |

## Endpoints

| Path                                              | Method | Description                                                         |
|---------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/digital-certified-copy-processor/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

# digital-certified-copy-processor
Consumes messages from the `item-ordered-certified-copy` Kafka topic.

## Environment variables

| Name                      | Description                                  | Mandatory | Location                                |
|---------------------------|----------------------------------------------|-----------|-----------------------------------------|
| API_URL                   | URL to CHS API                               | ✓         | chs-configs repo environment global_env |
| CHS_API_KEY               | API Access Key for CHS                       | ✓         | chs-configs repo environment global_env |
| PAYMENTS_API_URL          | Payments API URL                             | ✓         | chs-configs repo environment global_env |

## Endpoints

| Path                                              | Method | Description                                                         |
|---------------------------------------------------|--------|---------------------------------------------------------------------|
| *`/digital-certified-copy-processor/healthcheck`* | GET    | Returns HTTP OK (`200`) to indicate a healthy application instance. |

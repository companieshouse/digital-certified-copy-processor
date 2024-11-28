package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.kafka.support.SendResult;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

public class KafkaProducerCallback implements BiConsumer<SendResult<String, SignDigitalDocument>, Throwable> {

    private final Logger logger;

    private final String signDigitalDocumentTopic;

    private final SignDigitalDocument message;

    public KafkaProducerCallback(Logger logger, String signDigitalDocumentTopic, SignDigitalDocument message) {
        this.logger = logger;
        this.signDigitalDocumentTopic = signDigitalDocumentTopic;
        this.message = message;
    }

    public void onSuccess(SendResult<String, SignDigitalDocument> result) {
        final var metadata =  result.getRecordMetadata();
        final var partition = metadata.partition();
        final var offset = metadata.offset();
        logger.info("Message " + message + " delivered to topic " + signDigitalDocumentTopic
                        + " on partition " + partition + " with offset " + offset + ".",
                getLogMap(message.getGroupItem(),
                        message.getOrderNumber(),
                        signDigitalDocumentTopic,
                        partition,
                        offset));
    }

    public void onFailure(Throwable ex) {
        logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".",
                getLogMap(ex.getMessage()));
    }

    @Override
    public void accept(SendResult<String, SignDigitalDocument> sendResult, Throwable throwable) {
        if(throwable != null) {
            onFailure(throwable);
        } else {
            onSuccess(sendResult);
        }
    }

    private static Map<String, Object> getLogMap(final String itemId, final String orderNumber) {
        return new DataMap.Builder()
                .itemId(itemId)
                .orderId(orderNumber)
                .build()
                .getLogMap();
    }

    private static Map<String, Object> getLogMap(final String groupItem,
                                                 final String orderNumber,
                                                 final String topic,
                                                 final int partition,
                                                 final long offset) {
        return new DataMap.Builder()
                .groupItem(groupItem)
                .orderId(orderNumber)
                .topic(topic)
                .partition(partition)
                .offset(offset)
                .build()
                .getLogMap();
    }

    private static Map<String, Object> getLogMap(final String error) {
        return new DataMap.Builder()
                .errors(Collections.singletonList(error))
                .build()
                .getLogMap();
    }
}

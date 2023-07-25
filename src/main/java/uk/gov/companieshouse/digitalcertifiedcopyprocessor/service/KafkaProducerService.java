package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentFactory;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, SignDigitalDocument> kafkaTemplate;
    private final Logger logger;
    private final SignDigitalDocumentFactory signDigitalDocumentFactory;

    private final String signDigitalDocumentTopic;

    public KafkaProducerService(KafkaTemplate<String, SignDigitalDocument> kafkaTemplate,
                                Logger logger,
                                SignDigitalDocumentFactory signDigitalDocumentFactory,
                                @Value("${kafka.topics.sign-digital-document}")
                                String signDigitalDocumentTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger;
        this.signDigitalDocumentFactory = signDigitalDocumentFactory;
        this.signDigitalDocumentTopic = signDigitalDocumentTopic;
    }

    public void sendMessage(final ItemOrderedCertifiedCopy certifiedCopy, final URI privateUri, final String filingHistoryDescription) {

        final var itemId = certifiedCopy.getItemId();
        final var orderNumber = certifiedCopy.getOrderNumber();
        logger.info("Sending a message for certified copy ID " + itemId + " from order " + orderNumber + ".",
                getLogMap(itemId, orderNumber));

        final var message = signDigitalDocumentFactory.buildMessage(certifiedCopy, privateUri, filingHistoryDescription);
        final var future = kafkaTemplate.send(signDigitalDocumentTopic, message);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, SignDigitalDocument> result) {
                final var metadata =  result.getRecordMetadata();
                final var partition = metadata.partition();
                final var offset = metadata.offset();
                logger.info("Message " + message + " delivered to topic " + signDigitalDocumentTopic
                                + " on partition " + partition + " with offset " + offset + ".",
                        getLogMap(message.getItemGroup(),
                                  message.getOrderNumber(),
                                  signDigitalDocumentTopic,
                                  partition,
                                  offset));
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".",
                        getLogMap(ex.getMessage()));
            }

        });
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

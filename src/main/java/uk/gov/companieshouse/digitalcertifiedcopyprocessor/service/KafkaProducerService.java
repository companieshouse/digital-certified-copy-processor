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

import java.net.URI;

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

    public void sendMessage(final ItemOrderedCertifiedCopy certifiedCopy, final URI privateUri) {

        // TODO DCAC-73: Structured logging
        logger.info("Sending a message for certified copy ID " + certifiedCopy.getItemId() +
                        " from order " + certifiedCopy.getOrderNumber() + ".");
        final var message = signDigitalDocumentFactory.buildMessage(certifiedCopy, privateUri);
        final var future = kafkaTemplate.send(signDigitalDocumentTopic, message);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, SignDigitalDocument> result) {
                final var metadata =  result.getRecordMetadata();
                final var partition = metadata.partition();
                final var offset = metadata.offset();
                // TODO DCAC-73: Structured logging
                logger.info("Message " + message + " delivered to topic " + signDigitalDocumentTopic
                                + " on partition " + partition + " with offset " + offset + ".");
            }

            @Override
            public void onFailure(Throwable ex) {
                // TODO DCAC-73: Structured logging
                logger.error("Unable to deliver message " + message + ". Error: " + ex.getMessage() + ".");
            }

        });
    }

}

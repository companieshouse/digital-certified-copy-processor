package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentFactory;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaProducerCallback.getLogMap;

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

        future.whenComplete(new KafkaProducerCallback(logger, signDigitalDocumentTopic, message));
    }

}

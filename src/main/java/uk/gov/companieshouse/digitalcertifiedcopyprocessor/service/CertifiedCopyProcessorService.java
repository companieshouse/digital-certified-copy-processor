package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;

/**
 * Consumes a {@link uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy} message
 * to obtain further info from the Filing History and Document APIs, and finally produce a
 * {@link uk.gov.companieshouse.documentsigning.SignDigitalDocument} message.
 */
@Service
class CertifiedCopyProcessorService implements KafkaService {

    private final Logger logger;
    private final FilingHistoryDocumentService filingHistoryDocumentService;
    private final DocumentService documentService;
    private final KafkaProducerService kafkaProducerService;

    CertifiedCopyProcessorService(Logger logger, FilingHistoryDocumentService filingHistoryDocumentService, DocumentService documentService, KafkaProducerService kafkaProducerService) {
        this.logger = logger;
        this.filingHistoryDocumentService = filingHistoryDocumentService;
        this.documentService = documentService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public void processMessage(KafkaServiceParameters parameters) {

        // Note MessageLoggingAspect has already done a good job of logging incoming message.

        final var certifiedCopy = parameters.getData();
        final var documentMetadata = filingHistoryDocumentService.getDocumentMetadata(
                certifiedCopy.getCompanyNumber(),
                certifiedCopy.getFilingHistoryId());

        // TODO DCAC-260 Structure or remove
        logger.info("Document metadata: " + documentMetadata);

        // TODO DCAC-260 The code below this point has not even been manually tested
        // in this integration context as yet.

        final var privateUri = documentService.getPrivateUri(documentMetadata);

        // TODO DCAC-260 Structure or remove
        logger.info("Private URI: " + privateUri);

        kafkaProducerService.sendMessage(certifiedCopy, privateUri);

    }
}

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

    CertifiedCopyProcessorService(Logger logger, FilingHistoryDocumentService filingHistoryDocumentService, DocumentService documentService) {
        this.logger = logger;
        this.filingHistoryDocumentService = filingHistoryDocumentService;
        this.documentService = documentService;
    }

    @Override
    public void processMessage(KafkaServiceParameters parameters) {

        // Note MessageLoggingAspect has already done a good job of logging incoming message.

        final var message = parameters.getData();
        final var documentMetadata = filingHistoryDocumentService.getDocumentMetadata(
                message.getCompanyNumber(),
                message.getFilingHistoryId());

        // TODO DCAC-260 Structure or remove
        logger.info("Document metadata: " + documentMetadata);

        final var privateUri = documentService.getPrivateUri(documentMetadata);

        // TODO DCAC-260 Structure or remove
        logger.info("Private URI: " + privateUri);

    }
}

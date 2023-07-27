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
    private final FilingHistoryDescriptionService filingHistoryDescriptionService;

    CertifiedCopyProcessorService(Logger logger, FilingHistoryDocumentService filingHistoryDocumentService,
                                  DocumentService documentService, KafkaProducerService kafkaProducerService,
                                  FilingHistoryDescriptionService filingHistoryDescriptionService) {
        this.logger = logger;
        this.filingHistoryDocumentService = filingHistoryDocumentService;
        this.documentService = documentService;
        this.kafkaProducerService = kafkaProducerService;
        this.filingHistoryDescriptionService = filingHistoryDescriptionService;
    }

    @Override
    public void processMessage(KafkaServiceParameters parameters) {

        final var certifiedCopy = parameters.getData();

        final var documentMetadata = filingHistoryDocumentService.getDocumentMetadata(
                certifiedCopy.getCompanyNumber(),
                certifiedCopy.getFilingHistoryId());

        //convert the current filing history value to the correct description using api-enumerations
        final var filingHistoryDescription =
                filingHistoryDescriptionService.getDescription(certifiedCopy.getFilingHistoryDescription());

        final var privateUri = documentService.getPrivateUri(documentMetadata);

        kafkaProducerService.sendMessage(certifiedCopy, privateUri, filingHistoryDescription);
    }
}

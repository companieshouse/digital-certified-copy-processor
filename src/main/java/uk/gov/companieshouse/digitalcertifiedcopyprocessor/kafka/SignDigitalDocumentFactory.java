package uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.net.URI;

@Component
public class SignDigitalDocumentFactory {

    private static final String CERTIFIED_COPY_DOCUMENT_TYPE = "certified-copy";

    public SignDigitalDocument buildMessage(final ItemOrderedCertifiedCopy certifiedCopy, final URI privateUri,
                                            final String filingHistoryDescription) {
        return SignDigitalDocument.newBuilder()
                .setPrivateS3Location(privateUri.toString())
                .setDocumentType(CERTIFIED_COPY_DOCUMENT_TYPE)
                .setItemGroup(certifiedCopy.getGroupItem())
                .setOrderNumber(certifiedCopy.getOrderNumber())
                .setCompanyName(certifiedCopy.getCompanyName())
                .setCompanyNumber(certifiedCopy.getCompanyNumber())
                .setFilingHistoryDescription(filingHistoryDescription)
                .setFilingHistoryType(certifiedCopy.getFilingHistoryType())
                .build();
    }
}

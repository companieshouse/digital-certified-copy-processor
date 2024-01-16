package uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigning.CoverSheetDataRecord;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.net.URI;

@Component
public class SignDigitalDocumentFactory {

    private static final String CERTIFIED_COPY_DOCUMENT_TYPE = "certified-copy";

    public SignDigitalDocument buildMessage(final ItemOrderedCertifiedCopy certifiedCopy, final URI privateUri,
                                            final String filingHistoryDescription) {
        CoverSheetDataRecord coverSheetDataRecord = CoverSheetDataRecord.newBuilder()
                .setCompanyName(certifiedCopy.getCompanyName())
                .setCompanyNumber(certifiedCopy.getCompanyNumber())
                .setDescription(filingHistoryDescription)
                .setType(certifiedCopy.getFilingHistoryType())
                .build();

        return SignDigitalDocument.newBuilder()
                .setCoverSheetData(coverSheetDataRecord)
                .setPrivateS3Location(privateUri.toString())
                .setDocumentType(CERTIFIED_COPY_DOCUMENT_TYPE)
                .setGroupItem(certifiedCopy.getGroupItem())
                .setItemId(certifiedCopy.getItemId())
                .setOrderNumber(certifiedCopy.getOrderNumber())
                .setFilingHistoryDescriptionValues(certifiedCopy.getFilingHistoryDescriptionValues())
                .build();
    }
}

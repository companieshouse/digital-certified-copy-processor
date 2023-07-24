package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.CERTIFIED_COPY;

/**
 * Unit tests the {@link CertifiedCopyProcessorService} class.
 */
@ExtendWith(MockitoExtension.class)
public class CertifiedCopyProcessorServiceTest {

    @Mock
    private FilingHistoryDocumentService filingHistoryDocumentService;

    @Mock
    private DocumentService documentService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private CertifiedCopyProcessorService certifiedCopyProcessorService;

    @Test
    public void testProcessMessage() throws URISyntaxException {
        //Prepare test data
        ItemOrderedCertifiedCopy certifiedCopy = CERTIFIED_COPY;
        String documentMetadata = "egg";
        URI privateUri = new URI("private_uri");

        //Mock behaviour of the dependencies
        when(filingHistoryDocumentService.getDocumentMetadata(anyString(), anyString()))
                .thenReturn(documentMetadata);
        when(documentService.getPrivateUri(documentMetadata)).thenReturn(privateUri);

        //call the processor service
        certifiedCopyProcessorService.processMessage(new KafkaServiceParameters(certifiedCopy));

        //Verify that the methods of the dependencies were called with the correct parameters
        verify(filingHistoryDocumentService).getDocumentMetadata(certifiedCopy.getCompanyNumber(), certifiedCopy.getFilingHistoryId());
        verify(documentService).getPrivateUri(documentMetadata);
        verify(kafkaProducerService).sendMessage(certifiedCopy, privateUri);
    }
}

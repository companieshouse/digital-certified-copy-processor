package uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PRIVATE_DOCUMENT_URI;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryDescriptionConverterTest {

    @Mock
    private Logger logger;

    @Test
    @DisplayName("FilingHistoryDescriptionConverter converts successfully")
    void filingHistoryDescriptionConverterSuccess() {
        final FilingHistoryDescriptionConverter provider = new FilingHistoryDescriptionConverter(logger);
        // Given
        String inputDescriptionCode = "liquidation-receiver-administrative-receivers-report";
        String expectedOutput = "**Administrative Receiver's report**";
        //When, Then
        assertThat(provider.convertDescriptionCode(inputDescriptionCode), is(expectedOutput));
    }

    @Test
    @DisplayName("FilingHistoryDescriptionConverter returns null when no matching FilingHistoryDescription")
    void filingHistoryDescriptionConvertsAsExpected() {
        final FilingHistoryDescriptionConverter provider = new FilingHistoryDescriptionConverter(logger);
        // Given
        String inputDescriptionCode = "an-incorrect-string";
        String expectedOutput = null;
        //When, Then
        assertThat(provider.convertDescriptionCode(inputDescriptionCode), is(expectedOutput));
    }
}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.UriConversionException;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PRIVATE_DOCUMENT_URI;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.PUBLIC_DOCUMENT_URI;

@ExtendWith(MockitoExtension.class)
class PublicToPrivateUriConverterTest {

    private static final String EXPECTED_INVALID_PRIVATE_URI_MESSAGE =
    "Caught URISyntaxException creating private URI from `s3://document-api-images-cidev' and " +
            "'/docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf', derived from public URI '"
            + PUBLIC_DOCUMENT_URI + "`, error message is 'Invalid URI: s3://blah'";

    @InjectMocks
    private PublicToPrivateUriConverter converterUnderTest;

    @Mock
    private Logger logger;

    @Test
    @DisplayName("convertToPrivateUri converts public URI (location) to private URI successfully")
    void convertToPrivateUriConvertsAsExpected() throws URISyntaxException {

        // Given and when
        final URI privateUri = converterUnderTest.convertToPrivateUri(new URI(PUBLIC_DOCUMENT_URI));

        // Then
        assertThat(privateUri, is(EXPECTED_PRIVATE_DOCUMENT_URI));
    }

    @Test
    @DisplayName("convertToPrivateUri rejects an invalid public URI")
    void convertToPrivateUriRejectsAnInvalidUri() throws URISyntaxException {

        // Given
        final URI invalidPublicUri = new URI("localhost");

        // Given, when and then
        final UriConversionException conversionException =
                assertThrows(UriConversionException.class,
                            () -> converterUnderTest.convertToPrivateUri(invalidPublicUri));

        // And then
        assertThat(conversionException.getMessage(), is("Invalid public URI: localhost"));

    }

    @Test
    @DisplayName("convertToPrivateUri rejects a public URI that leads to an invalid private URI")
    void convertToPrivateUriRejectsUriLeadingToInvalidPrivateUri() throws URISyntaxException {

        // Given
        final URI validPublicUri = new URI(PUBLIC_DOCUMENT_URI);
        final var localConverter = new PublicToPrivateUriConverter(logger) {
            @Override
            protected URI createURI(String bucketUri, String documentKey) throws URISyntaxException {
                throw new URISyntaxException("s3://blah", "Invalid URI");
            }
        };

        // When and then
        final UriConversionException conversionException =
                assertThrows(UriConversionException.class,
                        () -> localConverter.convertToPrivateUri(validPublicUri));

        // And then
        assertThat(conversionException.getMessage(), is(EXPECTED_INVALID_PRIVATE_URI_MESSAGE));

    }

}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.LOCATION;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils.givenSdkIsConfigured;

/**
 * Integration tests the {@link DocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes={DocumentServiceIntegrationTest.Config.class, TestConfig.class})
@AutoConfigureWireMock(port = 0)
class DocumentServiceIntegrationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final String DOCUMENT_METADATA = "/document/specimen";

    private static final URI EXPECTED_PRIVATE_DOCUMENT_URI;
    static {
        try {
            EXPECTED_PRIVATE_DOCUMENT_URI = new URI(
                "s3://document-api-images-cidev/docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf");
        } catch (URISyntaxException e) {
            // This will not happen.
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private DocumentService serviceUnderTest;

    @Autowired
    private Environment environment;

    @Configuration
    @ComponentScan(basePackageClasses = DocumentServiceIntegrationTest.class)
    static class Config { }

    @Test
    @DisplayName("getPrivateUri() gets the document private URI successfully")
    void getPrivateUriGetsUriSuccessfully() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", LOCATION)));

        // When
        final URI privateUri =
                serviceUnderTest.getPrivateUri(DOCUMENT_METADATA);

        // Then
        assertThat(privateUri, is(EXPECTED_PRIVATE_DOCUMENT_URI));
    }

    @Test
    @DisplayName("getPrivateUri() throws 500 Internal Server Error for unknown document")
    void getPrivateUriThrowsInternalServerErrorForUnknownDocument() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(notFound()));

        // When and then
        assertDocumentApiRequestIssuePropagated(NOT_FOUND);
    }

    @Test
    @DisplayName("getPrivateUri() throws 500 Internal Server Error for service unavailable")
    void getPrivateUriThrowsInternalServerErrorForServiceUnavailable() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(serviceUnavailable()));

        // When and then
        assertDocumentApiRequestIssuePropagated(SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("getPrivateUri() throws 500 Internal Server Error for connection reset")
    void getPrivateUriThrowsInternalServerErrorForConnectionReset() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getPrivateUri(DOCUMENT_METADATA));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        final String expectedReason =
                "Caught ApiErrorResponseException with status 500, and message 'null' getting public URI using " +
                        "document content request " + DOCUMENT_METADATA + "/content.";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    private void assertDocumentApiRequestIssuePropagated(final HttpStatus underlyingStatus) {
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getPrivateUri(DOCUMENT_METADATA));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        final String expectedReason =
                "Received unexpected response status code " +
                        underlyingStatus.value() + " getting public URI using document content request "
                        + DOCUMENT_METADATA + "/content.";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
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
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.KafkaConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.RetryableException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentFactory;

import java.net.URI;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.DOCUMENT_METADATA;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PRIVATE_DOCUMENT_URI;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.PUBLIC_DOCUMENT_URI;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils.givenSdkIsConfigured;

/**
 * Integration tests the {@link DocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes={DocumentServiceIntegrationTest.Config.class,
                            TestConfig.class,
                            KafkaConfig.class,
                            SignDigitalDocumentFactory.class})
@AutoConfigureWireMock(port = 0)
class DocumentServiceIntegrationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Autowired
    private DocumentService serviceUnderTest;

    @Autowired
    private Environment environment;

    @Configuration
    @ComponentScan(basePackageClasses = DocumentServiceIntegrationTest.class)
    static class Config { }

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @Test
    @DisplayName("getPrivateUri() gets the document private URI successfully")
    void getPrivateUriGetsUriSuccessfully() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", PUBLIC_DOCUMENT_URI)));

        // When
        final URI privateUri =
                serviceUnderTest.getPrivateUri(DOCUMENT_METADATA);

        // Then
        assertThat(privateUri, is(EXPECTED_PRIVATE_DOCUMENT_URI));
    }

    @Test
    @DisplayName("getPrivateUri() throws RetryableException for unknown document")
    void getPrivateUriThrowsRetryableExceptionForUnknownDocument() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(notFound()));

        // When and then
        assertDocumentApiRequestIssuePropagatedAsRetryableException(NOT_FOUND);
    }

    @Test
    @DisplayName("getPrivateUri() throws RetryableException for service unavailable")
    void getPrivateUriThrowsRetryableExceptionForServiceUnavailable() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(serviceUnavailable()));

        // When and then
        assertDocumentApiRequestIssuePropagatedAsRetryableException(SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("getPrivateUri() throws RetryableException for connection reset")
    void getPrivateUriThrowsRetryableExceptionForConnectionReset() {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo( DOCUMENT_METADATA + "/content"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final RetryableException exception =
                assertThrows(RetryableException.class,
                        () -> serviceUnderTest.getPrivateUri(DOCUMENT_METADATA));
        final String expectedReason =
                "Caught ApiErrorResponseException with status code 500, and status message 'Connection reset' " +
                        "getting public URI using document content request " + DOCUMENT_METADATA + "/content.";
        assertThat(exception.getMessage(), Is.is(expectedReason));
    }

    private void assertDocumentApiRequestIssuePropagatedAsRetryableException(final HttpStatus underlyingStatus) {
        final RetryableException exception =
                assertThrows(RetryableException.class,
                        () -> serviceUnderTest.getPrivateUri(DOCUMENT_METADATA));
        final String expectedReason =
                "Received unexpected response status code " +
                        underlyingStatus.value() + " getting public URI using document content request "
                        + DOCUMENT_METADATA + "/content.";
        assertThat(exception.getMessage(), Is.is(expectedReason));
    }

}

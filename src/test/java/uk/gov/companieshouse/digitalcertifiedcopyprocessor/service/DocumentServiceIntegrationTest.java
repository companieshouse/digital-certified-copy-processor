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
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.ApplicationConfiguration;
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
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils.givenSdkIsConfigured;

/**
 * Integration tests the {@link DocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes={
        ApplicationConfiguration.class,
        DocumentServiceIntegrationTest.Config.class,
        TestConfig.class})
@AutoConfigureWireMock(port = 0)
class DocumentServiceIntegrationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final String DOCUMENT_METADATA = "/document/specimen";

    private static final String LOCATION =
        "https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/" +
        "docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf" +
        "?X-Amz-Algorithm=AWS4-HMAC-SHA256" +
        "&X-Amz-Credential=AWS_Access_Key_ID%2F20230523%2Feu-west-2%2Fs3%2Faws4_request" +
        "&X-Amz-Date=20230523T071124Z" +
        "&X-Amz-Expires=60&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEKf%2F%2F%2F%2F%2F%2F%2F%2F%2F%2" +
        "FwEaCWV1LXdlc3QtMiJIMEYCIQCkH0CgdgxtZUHzWSGqGZfvhArMBVhjUipfyzC7HqxybwIhAO3%2FACQh8hYR" +
        "P9tBbyeKNThXR5x4t%2B4kWBWFM9o4pYhjKsUFCND%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEQABoMMTY5OTQy" +
        "MDIwNTIxIgy%2FRjW4UMixpIgXLyoqmQUlJi%2FBNQsLmcvzHsmKLGAkTSa%2B07rB3JADObNlS1F8ajKNef%2" +
        "BHlGPmGtWr4TDVo%2BmfASgChgdwaZfZQ5%2BedFmO1O5mZIaaTXKpnpnggPEVv8EzFF4%2BuASmFhhD5GVEXi" +
        "MHdMqXaYzeKpry2wjJFvvcUhLpwOq9MdGEOd9rJOWp3EwX35u6geuFlR36yHPqXZD8gEmOltWJXz64G8otbmxo" +
        "imy9ZMjUe%2FT8KbAEg7WUfpHo23022myZjeUJYfk2X32mwQ%2F8FsUoU2pMQpTUHO0QmBlscMKzqlJMHalBzE" +
        "ut%2Fuiw0jl%2BbbXmRcz6piW4DTHuK%2BQsRUNGAjItwaFxVjNemJ14m1yDSNEaRE1JdqWutAsdKQ87XgV%2F" +
        "s9Td8KeFLJH%2FaC86ZXsgEjB2esnjp0q56ATBCs7yp9Im1966%2BoBOZaxqNAbtLwr%2F11YKn5V01R9uQpPi" +
        "ieEKp7nipRbxajBB6bJcTen%2By6dvxGm%2FladwvUIH7BRWYPDCx1DA9s1bUEKpLg%2F2FSVEmhq6yjZV%2Bu" +
        "eNkOBwqKwcLYKWgB3nfVYEu1ORzx6IMbVksnMJ%2BAlHrfj9HJF3XYDxca8yw%2F%2BIvi4oSK3j2Bc2bRyHim" +
        "q9UCdkiQFFnyPzplZ8OVkZ0%2FfOnv0LkPYhkdiMUoPWIqPcOyjmSNjF%2FbqOs2O7cN8xUUy4Nv%2FhzUYaYZ" +
        "svV6cy3Gk7DEq5Uj8eT5fEd3zlwLg0YKaG%2BEs2pHdoEIrDx6SgRDmZF5%2FlKwIEG8HrvkFLBvXxEjSnr%2F" +
        "u0naENzYXIg9fgtc%2BtctfbNtD1jloLe0RelNMGWhP9huPN1kotv3AnJQ%2B4TCxhIbDQeJBox%2BpR7sSfso" +
        "UfCAAgZr8ZzBlkhqAxYbAOCz3jhCPlLRdaGzD8v7GjBjqwAev5qyBqHCbUewXjxQs0JEOegwMO4gQ7IJt7OU0m" +
        "l1%2F3gogILJ2hpX6SvPCIT5ECm0NR9gaO2Ej%2B1RSflO%2FwBMpo2p48rwUVnUZ2LwNnvGxf79BwbyMJ%2Bd" +
        "cgaHQks4z5suSYJ4fabfQnYV32CwzEc%2FdNxaPKM7TFzaqaU%2FiVB91yVncr9sMp%2B8zKVMSyFBB32bQPYL" +
        "7qZT33foMHOrQP3BCQjovrlGe67hOKURrh5lkf" +
        "&X-Amz-SignedHeaders=host" +
        "&response-content-disposition=inline%3Bfilename%3D%22companies_house_document.pdf%22" +
        "&X-Amz-Signature=e18b2345566e0340bfc6ca7633939039df9fb71c1a44265701bb7102e42094c0";

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
    void getPrivateUriGetsUriSuccessfully() throws  URISyntaxException {

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

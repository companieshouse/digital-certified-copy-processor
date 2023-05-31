package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.document.DocumentResourceHandler;
import uk.gov.companieshouse.api.handler.document.request.DocumentGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.NonRetryableException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.RetryableException;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.DOCUMENT_METADATA;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PRIVATE_DOCUMENT_URI;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PUBLIC_DOCUMENT_URI;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.PUBLIC_DOCUMENT_URI;

/**
 * Unit tests the {@link DocumentService} class.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @InjectMocks
    private DocumentService serviceUnderTest;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private Logger logger;

    @Mock
    private PublicToPrivateUriConverter converter;

    @Mock
    private ApiClient apiClient;

    @Mock
    private DocumentResourceHandler documentResourceHandler;

    @Mock
    private DocumentGet documentGet;

    @Mock
    private ApiResponse<Void> response;

    @Mock
    private Map<String, Object> headers;


    @Test
    @DisplayName("getPrivateUri() gets URI successfully")
    void getPrivateUriGetsUriSuccessfully() throws ApiErrorResponseException, URIValidationException {

        // Given
        givenResponseWithStatus(FOUND);
        givenResponseContainsPublicUri(PUBLIC_DOCUMENT_URI);
        when(converter.convertToPrivateUri(EXPECTED_PUBLIC_DOCUMENT_URI)).thenReturn(EXPECTED_PRIVATE_DOCUMENT_URI);

        // When
        final URI privateUri = serviceUnderTest.getPrivateUri(DOCUMENT_METADATA);

        // Then
        assertThat(privateUri, is(EXPECTED_PRIVATE_DOCUMENT_URI));
    }

    @Test
    @DisplayName("getPublicUri() gets URI successfully")
    void getPublicUriGetsUriSuccessfully() throws ApiErrorResponseException, URIValidationException {

        // Given
        givenResponseWithStatus(FOUND);
        givenResponseContainsPublicUri(PUBLIC_DOCUMENT_URI);

        // When
        final URI publicUri = serviceUnderTest.getPublicUri(DOCUMENT_METADATA);

        // Then
        assertThat(publicUri, is(EXPECTED_PUBLIC_DOCUMENT_URI));
    }

    @Test
    @DisplayName(
            "getPublicUri() propagates a RuntimeException from the ApiClientService wrapped as a NonRetryableException")
    void getPublicUriErrorsRetryablyForApiClientServiceRuntimeException()  {

        // Given
        var underlyingError = "Environment variable missing: DOCUMENT_API_LOCAL_URL";
        when(apiClientService.getApiClient()).thenThrow(new RuntimeException(underlyingError));

        // When
        final NonRetryableException exception =
                assertThrows(NonRetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        assertThat(exception.getMessage(), is("Caught RuntimeException getting API client: " + underlyingError));
    }

    @Test
    @DisplayName("getPublicUri() throws a RetryableException for a non-302 response from the document API")
    void getPublicUriErrorsRetryablyForNon302Response() throws ApiErrorResponseException, URIValidationException {

        // Given
        givenResponseWithStatus(NOT_FOUND);

        // When
        final RetryableException exception =
                assertThrows(RetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "Received unexpected response status code " +
                NOT_FOUND.value() + " getting public URI using document content request " +
                DOCUMENT_METADATA + "/content.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    @Test
    @DisplayName("getPublicUri() propagates an ApiErrorResponseException wrapped in a RetryableException")
    void getPublicUriErrorsRetryablyForApiErrorResponseException()
            throws ApiErrorResponseException, URIValidationException {

        // Given
        final var underlyingErrorMessage = "Unknown IO error.";
        givenRequestExecutionException(
                ApiErrorResponseException.fromIOException(new IOException(underlyingErrorMessage)));

        // When and then
        final RetryableException exception =
                assertThrows(RetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "Caught ApiErrorResponseException with status code " + INTERNAL_SERVER_ERROR.value() +
                ", and status message '" + underlyingErrorMessage +
                "' getting public URI using document content request " + DOCUMENT_METADATA + "/content.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    @Test
    @DisplayName("getPublicUri() propagates a URIValidationException wrapped in a NonRetryableException")
    void getPublicUriErrorsNonRetryablyForURIValidationException()
            throws ApiErrorResponseException, URIValidationException {

        // Given
        final var underlyingErrorMessage = "URI pattern does not match expected URI pattern for this resource.";
        givenRequestExecutionException(new URIValidationException(underlyingErrorMessage));

        // When and then
        final NonRetryableException exception =
                assertThrows(NonRetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "Invalid URI " + DOCUMENT_METADATA +
                "/content to get document public URI.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    @Test
    @DisplayName("getPublicUri() propagates a URISyntaxException wrapped in a NonRetryableException")
    void getPublicUriErrorsNonRetryablyForURISyntaxException()
            throws ApiErrorResponseException, URIValidationException {

        // Given
        final var corruptedLocationHeaderValue = "corrupted location?";
        givenResponseWithStatus(FOUND);
        givenResponseContainsPublicUri(corruptedLocationHeaderValue);

        // When and then
        final NonRetryableException exception =
                assertThrows(NonRetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "Invalid URI `" + corruptedLocationHeaderValue +
                "` obtained from Location header.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    @Test
    @DisplayName("getPublicUri() throws a NonRetryableException if location is null")
    void getPublicUriErrorsNonRetryablyForNullLocation() throws ApiErrorResponseException, URIValidationException {

        // Given
        givenResponseWithStatus(FOUND);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(LOCATION.toLowerCase())).thenReturn(null);

        // When and then
        final NonRetryableException exception =
                assertThrows(NonRetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "No locations found in response from document API.";
        assertThat(exception.getMessage(), is(expectedError));
    }



    @Test
    @DisplayName("getPublicUri() throws a NonRetryableException if location empty")
    void getPublicUriErrorsNonRetryablyForEmptyLocation() throws ApiErrorResponseException, URIValidationException {

        // Given
        givenResponseWithStatus(FOUND);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(LOCATION.toLowerCase())).thenReturn(new ArrayList<>());

        // When and then
        final NonRetryableException exception =
                assertThrows(NonRetryableException.class,
                        () -> serviceUnderTest.getPublicUri(DOCUMENT_METADATA));
        final var expectedError = "No locations found in response from document API.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    private void givenRequestExecutionException(final Exception exception)
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClient.document()).thenReturn(documentResourceHandler);
        when(documentResourceHandler.getDocument(DOCUMENT_METADATA + "/content")).thenReturn(documentGet);
        when(documentGet.execute()).thenThrow(exception);
    }

    private void givenResponseWithStatus(final HttpStatus status)
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClient.document()).thenReturn(documentResourceHandler);
        when(documentResourceHandler.getDocument(DOCUMENT_METADATA + "/content")).thenReturn(documentGet);
        when(documentGet.execute()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(status.value());
    }

    private void givenResponseContainsPublicUri(final String publicUri) {
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(LOCATION.toLowerCase())).thenReturn(List.of(publicUri));
    }

}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.NonRetryableException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.RetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.FOUND;

@Service
public class DocumentService {

    private static final UriTemplate GET_DOCUMENT_CONTENT_URL = new UriTemplate("{documentMetadata}/content");

    private final ApiClientService apiClientService;

    private final Logger logger;

    private final PublicToPrivateUriConverter converter;

    public DocumentService(ApiClientService apiClientService, Logger logger, PublicToPrivateUriConverter converter) {
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.converter = converter;
    }

    public URI getPrivateUri(final String documentMetadata) {
        logger.info("Getting private URI for document metadata " + documentMetadata + ".",
                getLogMap(documentMetadata));
        final var publicUri = getPublicUri(documentMetadata);
        logger.info("Got public URI " + publicUri + " for document metadata " + documentMetadata + ".",
                getLogMap(documentMetadata, publicUri));
        final var privateUri = converter.convertToPrivateUri(publicUri);
        logger.info("Got private URI " + privateUri + " for document metadata " + documentMetadata + ".",
                getLogMap(documentMetadata, publicUri, privateUri));
        return privateUri;
    }

    public URI getPublicUri(final String documentMetadata) {
        final String uri = GET_DOCUMENT_CONTENT_URL.expand(documentMetadata).toString();
        try {
            final var response = getDocumentContent(uri, documentMetadata);
            return getFirstLocationAsUri(response, documentMetadata);
        } catch (ApiErrorResponseException ex) {
            final var error = "Caught ApiErrorResponseException with status code " +
                    ex.getStatusCode() + ", and status message '" + ex.getStatusMessage() +
                    "' getting public URI using document content request " + uri + ".";
            logger.error(error, ex, getLogMap(documentMetadata));
            throw new RetryableException(error, ex);
        } catch (URIValidationException ex) {
            // Should this happen (unlikely), it is a programmatic error, hence not recoverable.
            final var error = "Invalid URI " + uri + " to get document public URI.";
            logger.error(error, ex, getLogMap(documentMetadata));
            throw new NonRetryableException(error, ex);
        }
    }

    private ApiResponse<Void> getDocumentContent(final String uri, final String documentMetadata)
            throws ApiErrorResponseException, URIValidationException {
        final var response = getApiClient(documentMetadata).document().getDocument(uri).execute();
        if (response.getStatusCode() != FOUND.value()) {
            final var error = "Received unexpected response status code " +
                    response.getStatusCode() +
                    " getting public URI using document content request " +
                    uri + ".";
            logger.error(error, getLogMap(documentMetadata));
            throw new RetryableException(error);
        }
        return response;
    }

    private URI getFirstLocationAsUri(final ApiResponse<Void> response, final String documentMetadata) {
        final var headers = response.getHeaders();
        var locations = (List<String>) headers.get(LOCATION.toLowerCase());
        if (isEmpty(locations)) {
            // Should this happen (unlikely), it would likely not be a recoverable issue.
            final var error = "No locations found in response from document API.";
            logger.error(error, getLogMap(documentMetadata));
            throw new NonRetryableException(error);
        }
        try {
            return new URI(locations.get(0));
        } catch (URISyntaxException ex) {
            // Should this happen (unlikely), it would likely not be a recoverable issue.
            final var error = "Invalid URI `" + locations.get(0) + "` obtained from Location header.";
            logger.error(error, ex, getLogMap(documentMetadata));
            throw new NonRetryableException(error, ex);
        }
    }

    private ApiClient getApiClient(final String documentMetadata) {
        try {
            return apiClientService.getApiClient();
        } catch (RuntimeException re) {
            // Should this happen (unlikely), it would likely not be a recoverable issue.
            final var error = "Caught RuntimeException getting API client: " + re.getMessage();
            logger.error(error, getLogMap(documentMetadata));
            throw new NonRetryableException(error, re);
        }
    }

    private Map<String, Object> getLogMap(final String documentMetadata) {
        return new DataMap.Builder()
                .filingHistoryDocumentMetadata(documentMetadata)
                .build()
                .getLogMap();
    }

    private Map<String, Object> getLogMap(final String documentMetadata, final URI publicUri) {
        return new DataMap.Builder()
                .filingHistoryDocumentMetadata(documentMetadata)
                .documentPublicUri(publicUri.toString())
                .build()
                .getLogMap();
    }

    private Map<String, Object> getLogMap(final String documentMetadata, final URI publicUri, final URI privateUri) {
        return new DataMap.Builder()
                .filingHistoryDocumentMetadata(documentMetadata)
                .documentPublicUri(publicUri.toString())
                .documentPrivateUri(privateUri.toString())
                .build()
                .getLogMap();
    }

}

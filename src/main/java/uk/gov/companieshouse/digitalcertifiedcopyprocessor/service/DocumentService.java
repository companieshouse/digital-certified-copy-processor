package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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
        final var publicUri = getPublicUri(documentMetadata);
        return converter.convertToPrivateUri(publicUri);
    }

    public URI getPublicUri(final String documentMetadata) {
        final String uri = GET_DOCUMENT_CONTENT_URL.expand(documentMetadata).toString();
        try {
            final var response = getDocumentContent(uri);
            return getFirstLocationAsUri(response);
        } catch (ApiErrorResponseException ex) {
            final var error = "Caught ApiErrorResponseException with status code " +
                    ex.getStatusCode() + ", and status message '" + ex.getStatusMessage() +
                    "' getting public URI using document content request " + uri + ".";
            // TODO DCAC-71 Structured logging
            // throw getResponseStatusException(ex, apiClient, companyNumber, filingHistoryDocumentId, uri);
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        } catch (URIValidationException ex) {
            // Should this happen (unlikely), it is a broken contract, hence 500.
            final var error = "Invalid URI " + uri + " to get document public URI.";
            // TODO DCAC-71 Structured logging
            // logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
    }

    private ApiResponse<Void> getDocumentContent(final String uri)
            throws ApiErrorResponseException, URIValidationException {
        final var response = getApiClient().document().getDocument(uri).execute();
        if (response.getStatusCode() != FOUND.value()) {
            // TODO DCAC-71 Structured logging
            final var error = "Received unexpected response status code " +
                    response.getStatusCode() +
                    " getting public URI using document content request " +
                    uri + ".";
            logger.error(error);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
        return response;
    }

    private URI getFirstLocationAsUri(final ApiResponse<Void> response) {
        final var headers = response.getHeaders();
        var locations = (List<String>) headers.get(LOCATION.toLowerCase());
        if (isEmpty(locations)) {
            // Should this happen (unlikely), it would likely not be a recoverable issue?
            final var error = "No locations found in response from document API.";
            // TODO DCAC-71 Structured logging
            // logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            logger.error(error);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
        try {
            return new URI(locations.get(0));
        } catch (URISyntaxException ex) {
            // Should this happen (unlikely), it would likely not be a recoverable issue?
            final var error = "Invalid URI `" + locations.get(0) + "` obtained from Location header.";
            // TODO DCAC-71 Structured logging
            // logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
    }

    private ApiClient getApiClient() {
        try {
            return apiClientService.getApiClient();
        } catch (RuntimeException re) {
            logger.error("Caught RuntimeException getting API client: " + re.getMessage());
            throw re;
        }
    }

}

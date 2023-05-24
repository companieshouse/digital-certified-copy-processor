package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.http.HttpHeaders;
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

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.MOVED_TEMPORARILY;

@Service
public class DocumentService {

    private static final UriTemplate GET_SHORT_LIVED_DOCUMENT_URL = new UriTemplate("{documentMetadata}/content");

    private final ApiClientService apiClientService;

    private final Logger logger;

    private final PublicToPrivateUriConverter converter;

    public DocumentService(ApiClientService apiClientService, Logger logger, PublicToPrivateUriConverter converter) {
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.converter = converter;
    }

    public URI getPrivateUri(final String documentMetadata) throws URISyntaxException {
        final URI publicUri = getPublicUri(documentMetadata);
        return converter.convertToPrivateUri(publicUri);
    }

    public URI getPublicUri(final String documentMetadata) {
        final String uri = GET_SHORT_LIVED_DOCUMENT_URL.expand(documentMetadata).toString();
        try {
            final ApiResponse<Void> response = getDocumentContent(uri);
            final var headers = response.getHeaders();
            // TODO DCAC-71 Error handling
            final var locations = (List<String>) headers.get(HttpHeaders.LOCATION.toLowerCase());
            return new URI(locations.get(0));
        } catch (ApiErrorResponseException ex) {
            final String error = "Caught ApiErrorResponseException with status " +
                    ex.getStatusCode() + ", and message '" + ex.getMessage() +
                    "' getting public URI using document content request " + uri + ".";
            // TODO DCAC-71 Structured logging
            // throw getResponseStatusException(ex, apiClient, companyNumber, filingHistoryDocumentId, uri);
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        } catch (URIValidationException | URISyntaxException ex) {
            // Should this happen (unlikely), it is a broken contract, hence 500.
            final String error = "Invalid URI " + uri + " to get document public URI.";
            // TODO DCAC-71 Structured logging
            // logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
    }

    private ApiResponse<Void> getDocumentContent(final String uri)
            throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<Void> response = getApiClient().document().getDocument(uri).execute();
        if (response.getStatusCode() != MOVED_TEMPORARILY.value()) {
            // TODO DCAC-71 Structured logging
            final String error = "Received unexpected response status code " +
                    response.getStatusCode() +
                    " getting public URI using document content request " +
                    uri + ".";
            logger.error(error);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
        return response;
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

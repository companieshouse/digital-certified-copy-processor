package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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
        final InternalApiClient apiClient = getInternalApiClient();
        try {
             final var headers = apiClient.privateDocumentResourceHandler().getDocument(uri).execute().getHeaders();
            // TODO DCAC-71 Error handling
            final var locations = (List<String>) headers.get(HttpHeaders.LOCATION.toLowerCase());
            return new URI(locations.get(0));
        } catch (ApiErrorResponseException ex) {
            // TODO DCAC-71 Structured logging, error handling
            // throw getResponseStatusException(ex, apiClient, companyNumber, filingHistoryDocumentId, uri);
            logger.error("Caught ApiErrorResponseException getting public URI.", ex);
            throw new RuntimeException(ex);
        } catch (URIValidationException | URISyntaxException ex) {
            // Should this happen (unlikely), it is a broken contract, hence 500.
            final String error = "Invalid URI " + uri + " to get document public URI.";
            // TODO DCAC-71 Structured logging
            // logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            logger.error(error, ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }
    }

    private InternalApiClient getInternalApiClient() {
        try {
            return apiClientService.getInternalApiClient();
        } catch (RuntimeException re) {
            logger.error("Caught RuntimeException getting API client: " + re.getMessage());
            throw re;
        }
    }

}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class FilingHistoryDocumentService {

    private static final UriTemplate
            GET_FILING_HISTORY_DOCUMENT =
            new UriTemplate("/company/{companyNumber}/filing-history/{filingHistoryId}");

    private final ApiClientService apiClientService;

    private final Logger logger;

    public FilingHistoryDocumentService(final ApiClientService apiClientService,
                                        final Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Gets the filing history document metadata for the company number and filing history document ID provided.
     * @param companyNumber the company number
     * @param filingHistoryDocumentId the filing history document ID
     * @return the document metadata link
     */
    public String getDocumentMetadata(
            final String companyNumber,
            final String filingHistoryDocumentId) {
        logger.info("Getting filing history document " + filingHistoryDocumentId + " for company number "
                + companyNumber + ".", getLogMap(companyNumber, filingHistoryDocumentId));
        final ApiClient apiClient = apiClientService.getInternalApiClient();
        final String uri = GET_FILING_HISTORY_DOCUMENT.expand(companyNumber, filingHistoryDocumentId).toString();
        try {
            final FilingApi filing = apiClient.filing().get(uri).execute().getData();
            final String metadata = filing.getLinks().getDocumentMetaData();
            logger.info("Got document metadata " + metadata + ".",
                    getLogMap(companyNumber, filingHistoryDocumentId, metadata));
            return metadata;
        } catch (ApiErrorResponseException ex) {
            throw getResponseStatusException(ex, apiClient, companyNumber, filingHistoryDocumentId, uri);
        } catch (URIValidationException ex) {
            // Should this happen (unlikely), it is a broken contract, hence 500.
            final String error = "Invalid URI " + uri + " for filing";
            logger.error(error, ex, getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        }

    }

    /**
     * Creates an appropriate exception to report the underlying problem.
     * @param apiException the API exception caught
     * @param client the API client
     * @param companyNumber the number of the company for which the filing history is looked up
     * @param filingHistoryDocumentId the filing history document ID
     * @param uri the URI used to communicate with the company filing history API
     * @return the {@link ResponseStatusException} exception to report the problem
     */
    private ResponseStatusException getResponseStatusException(final ApiErrorResponseException apiException,
                                                               final ApiClient client,
                                                               final String companyNumber,
                                                               final String filingHistoryDocumentId,
                                                               final String uri) {
        final ResponseStatusException propagatedException;
        if (apiException.getStatusCode() >= INTERNAL_SERVER_ERROR.value()) {
            final String error = "Error sending request to "
                    + client.getBasePath() + uri + ": " + apiException.getStatusMessage();
            logger.error(error, apiException,
                    getLogMap(companyNumber, filingHistoryDocumentId, INTERNAL_SERVER_ERROR, error));
            propagatedException = new ResponseStatusException(INTERNAL_SERVER_ERROR, error);
        } else {
            final String error = "Error getting filing history document " + filingHistoryDocumentId +
                    " for company number " + companyNumber + ".";
            logger.error(error, apiException, getLogMap(companyNumber, filingHistoryDocumentId, BAD_REQUEST, error));
            propagatedException =  new ResponseStatusException(BAD_REQUEST, error);
        }
        return propagatedException;
    }

    private Map<String, Object> getLogMap(final String companyNumber, final String filingHistoryDocumentId) {
        return new DataMap.Builder()
                .companyNumber(companyNumber)
                .filingHistoryDocumentId(filingHistoryDocumentId)
                .build()
                .getLogMap();
    }

    private Map<String, Object> getLogMap(final String companyNumber,
                                          final String filingHistoryDocumentId,
                                          final String metadata) {
        return new DataMap.Builder()
                .companyNumber(companyNumber)
                .filingHistoryDocumentId(filingHistoryDocumentId)
                .filingHistoryDocumentMetadata(metadata)
                .build()
                .getLogMap();
    }

    private Map<String, Object> getLogMap(final String companyNumber,
                                          final String filingHistoryDocumentId,
                                          final HttpStatus status,
                                          final String error) {
        return new DataMap.Builder()
                .companyNumber(companyNumber)
                .filingHistoryDocumentId(filingHistoryDocumentId)
                .status(status.toString())
                .errors(List.of(error))
                .build()
                .getLogMap();
    }

}

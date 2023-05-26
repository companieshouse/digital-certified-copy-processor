package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.logging.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.lang.System.getenv;

/**
 * This provides a way of testing the sending of a document content request to the cidev document API without going
 * via the Java SDKs only. It might be useful for troubleshooting.
 * @deprecated Use {@link DocumentService}.
 */
@Service
public class RestTemplateDocumentService {

    private static final UriTemplate GET_SHORT_LIVED_DOCUMENT_URL = new UriTemplate("{documentMetadata}/content");

    private final RestTemplate restTemplate;

    private final Logger logger;

    private final PublicToPrivateUriConverter converter;

    public RestTemplateDocumentService(RestTemplate restTemplate, Logger logger, PublicToPrivateUriConverter converter) {
        this.restTemplate = restTemplate;
        this.logger = logger;
        this.converter = converter;
    }

    public URI getPrivateUri(final String documentMetadata) {
        final URI publicUri = getPublicUri(documentMetadata);
        return converter.convertToPrivateUri(publicUri);
    }

    public URI getPublicUri(final String documentMetadata) {
        final String uri = GET_SHORT_LIVED_DOCUMENT_URL.expand(documentMetadata).toString();
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(getBasicAuthCredentials());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
        final HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        final var message =
                restTemplate.exchange(
                        "http://document-api-cidev.aws.chdev.org" + uri,
                        HttpMethod.GET,
                        httpEntity,
                        HttpMessage.class);
        return message.getHeaders().getLocation();
    }

    private static String getBasicAuthCredentials() {
        return getenv("BASIC_AUTH_CREDENTIALS");
    }

}

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

@Service
public class DocumentService {

    private static final UriTemplate GET_SHORT_LIVED_DOCUMENT_URL = new UriTemplate("/{documentMetadata}/content");

    private final RestTemplate restTemplate;

    private final Logger logger;

    private final PublicToPrivateUriConverter converter;

    public DocumentService(RestTemplate restTemplate, Logger logger, PublicToPrivateUriConverter converter) {
        this.restTemplate = restTemplate;
        this.logger = logger;
        this.converter = converter;
    }

    public URI getPrivateUri(final String documentMetadata) throws URISyntaxException {
        final URI publicUri = getPublicUri(documentMetadata);
        return converter.convertToPrivateUri(publicUri);
    }

    // TODO DCAC-71 Use SDK rather than Spring.
    public URI getPublicUri(final String documentMetadata) {
        final String uri = GET_SHORT_LIVED_DOCUMENT_URL.expand(documentMetadata).toString();
        final HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(getBasicAuthCredentials());
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_PDF));
        final HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        // TODO DCAC-71 Configure doc api domain
        // TODO DCAC-71 Structured logging
        final var message =
                restTemplate.exchange(
                        "http://document-api-cidev.aws.chdev.org" + uri,
                        HttpMethod.GET,
                        httpEntity,
                        HttpMessage.class);
        return message.getHeaders().getLocation();
    }

    private static String getBasicAuthCredentials() {
        // TODO DCAC-71 Constants, required env vars etc
        return getenv("BASIC_AUTH_CREDENTIALS");
    }

}

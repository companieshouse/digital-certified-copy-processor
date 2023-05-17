package uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PublicToPrivateUriConverter {

    private static final String S3_SCHEME_PREFIX = "s3://";

    public URI convertToPrivateUri(final URI publicUri) throws URISyntaxException {
        // TODO DCAC-71 Error handling
        final var hostName = publicUri.getHost().split("\\.")[0];
        final var documentKey = publicUri.getPath();
        final var bucketUri = S3_SCHEME_PREFIX + hostName;

        return new URI(bucketUri + documentKey);
    }
}

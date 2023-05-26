package uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter;

import com.google.re2j.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.UriConversionException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Component
public class PublicToPrivateUriConverter {

    private final Logger logger;

    private static final Pattern PUBLIC_URI_PATTERN =
            Pattern.compile("^https://.*\\.s3\\.eu-west-2\\.amazonaws\\.com/(.*?pdf)");
    private static final String S3_SCHEME_PREFIX = "s3://";

    public PublicToPrivateUriConverter(Logger logger) {
        this.logger = logger;
    }

    public URI convertToPrivateUri(final URI publicUri) {
        if (!isValidPublicUri(publicUri)) {
            final String error = "Invalid public URI: " + publicUri;
            logger.error(error, getLogMap(publicUri));
            throw new UriConversionException(error);
        }

        final var hostName = publicUri.getHost().split("\\.")[0];
        final var bucketUri = S3_SCHEME_PREFIX + hostName;
        final var documentKey = publicUri.getPath();

        try {
            return createURI(bucketUri, documentKey);
        } catch (URISyntaxException ex) {
            final String error = "Caught URISyntaxException creating private URI from `" +
                    bucketUri  + "' and '" + documentKey + "', derived from public URI '" + publicUri +
                    "`, error message is '" + ex.getMessage() + "'";
            logger.error(error, getLogMap(publicUri));
            throw new UriConversionException(error, ex);
        }
    }

    protected URI createURI(final String bucketUri, final String documentKey) throws URISyntaxException {
        return new URI(bucketUri + documentKey);
    }

    private boolean isValidPublicUri(final URI publicUri) {
        final var matcher = PUBLIC_URI_PATTERN.matcher(publicUri.toString());
        return matcher.find();
    }

    private Map<String, Object> getLogMap(final URI publicUri) {
        return new DataMap.Builder()
                .documentPublicUri(publicUri.toString())
                .build()
                .getLogMap();
    }
}

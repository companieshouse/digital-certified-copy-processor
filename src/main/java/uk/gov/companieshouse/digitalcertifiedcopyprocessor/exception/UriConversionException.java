package uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception;

/**
 * Wraps and propagates exceptions originating in the
 * {@link uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter}.
 */
public class UriConversionException extends NonRetryableException {
    public UriConversionException(String message) {
        super(message);
    }

    public UriConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

public class Error {
    private final String type;
    private final String error;

    public Error(String type, String error) {
        this.type = type;
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }
}

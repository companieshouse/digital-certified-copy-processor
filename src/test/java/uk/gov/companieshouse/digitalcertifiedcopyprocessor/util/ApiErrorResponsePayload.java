package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

import java.util.List;

public class ApiErrorResponsePayload {

    private final List<Error> errors;

    public ApiErrorResponsePayload(List<Error> errors) {
        this.errors = errors;
    }

    public List<Error> getErrors() {
        return errors;
    }
}
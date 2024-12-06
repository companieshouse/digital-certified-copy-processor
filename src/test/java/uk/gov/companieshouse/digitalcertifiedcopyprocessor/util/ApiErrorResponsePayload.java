package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

import java.util.List;

public record ApiErrorResponsePayload(List<Error> errors) {

}
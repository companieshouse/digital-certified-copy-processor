package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.NonRetryableException;

/**
 * The default service.
 */
@Component
class NullService implements KafkaService {

    @Override
    public void processMessage(KafkaServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}

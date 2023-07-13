package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.exception.NonRetryableException;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaService;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaServiceParameters;

@Component
public class NonRetryableExceptionService implements KafkaService {

    @Override
    public void processMessage(KafkaServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}

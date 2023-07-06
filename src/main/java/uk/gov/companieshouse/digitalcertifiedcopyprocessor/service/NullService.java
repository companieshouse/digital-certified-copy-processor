package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.stereotype.Component;

/**
 * The default service is a placeholder for actually hooking up to the consuming service.
 */
@Component
public class NullService implements KafkaService {

    @Override
    public void processMessage(KafkaServiceParameters parameters) {
        System.out.println("DATA: "+parameters.getData());
    }
}
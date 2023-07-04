package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.springframework.stereotype.Component;

/**
 * The default service.
 */
@Component
class NullService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        System.out.println("DATA: "+parameters.getData());
    }
}
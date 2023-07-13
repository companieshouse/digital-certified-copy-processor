package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

/**
 * Processes an incoming message.
 */
public interface KafkaService {

    /**
     * Processes an incoming message.
     *
     * @param parameters Any parameters required when processing the message.
     */
    void processMessage(KafkaServiceParameters parameters);
}
package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;

/**
 * The default service is a placeholder for actually hooking up to the consuming service.
 */
@Component
public class NullService implements KafkaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    // Kafka Message Keys
    private static final String COMPANY_NAME = "company_name";
    private static final String COMPANY_NUMBER = "company_number";
    private static final String ITEM_ID = "item_id";
    private static final String FILING_HISTORY_DESCRIPTION = "filing_history_description";
    private static final String FILING_HISTORY_DESCRIPTION_VALUES = "filing_history_description_values";
    private static final String FILING_HISTORY_TYPE = "filing_history_type";
    private static final String GROUP_ITEM = "group_item";
    private static final String ORDER_NUMBER = "order_number";

    @Override
    public void processMessage(KafkaServiceParameters parameters) {

        final String companyName = parameters.getData().get(COMPANY_NAME).toString();
        final String companyNumber = parameters.getData().get(COMPANY_NUMBER).toString();
        final String itemId = parameters.getData().get(ITEM_ID).toString();
        final String filingHistoryDescription = parameters.getData().get(FILING_HISTORY_DESCRIPTION).toString();
        final String filingHistoryDescriptionValues = parameters.getData().get(FILING_HISTORY_DESCRIPTION_VALUES).toString();
        final String filingHistoryType = parameters.getData().get(FILING_HISTORY_TYPE).toString();
        final String groupItem = parameters.getData().get(GROUP_ITEM).toString();
        final String orderNumber = parameters.getData().get(ORDER_NUMBER).toString();

        LOGGER.info("Processed Kafka message for: ", getLogMap(companyName, companyNumber));
    }

    private Map<String, Object> getLogMap(final String companyName, final String companyNumber) {
        return new DataMap.Builder()
                .companyName(companyName)
                .companyNumber(companyNumber)
                .build()
                .getLogMap();
    }

}
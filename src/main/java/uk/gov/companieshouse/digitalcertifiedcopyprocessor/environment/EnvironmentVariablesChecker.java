package uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication.APPLICATION_NAME_SPACE;

public class EnvironmentVariablesChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public enum RequiredEnvironmentVariables {
        API_URL("API_URL"),
        PAYMENTS_API_URL("PAYMENTS_API_URL"),
        CHS_API_KEY("CHS_API_KEY"),
        DOCUMENT_API_LOCAL_URL("DOCUMENT_API_LOCAL_URL"),
        BACKOFF_DELAY("BACKOFF_DELAY"),
        GROUP_ID("GROUP_ID"),
        INVALID_ITEM_ORDERED_CERTIFIED_COPY_TOPIC("INVALID_ITEM_ORDERED_CERTIFIED_COPY_TOPIC"),
        MAX_ATTEMPTS("MAX_ATTEMPTS"),
        CONCURRENT_LISTENER_INSTANCES("CONCURRENT_LISTENER_INSTANCES"),
        ITEM_ORDERED_CERTIFIED_COPY_TOPIC("ITEM_ORDERED_CERTIFIED_COPY_TOPIC"),
        BOOTSTRAP_SERVER_URL("BOOTSTRAP_SERVER_URL"),
        SIGN_DIGITAL_DOCUMENT_TOPIC("SIGN_DIGITAL_DOCUMENT_TOPIC");

        private final String name;

        RequiredEnvironmentVariables(String name) { this.name = name; }

        public String getName() { return this.name; }
    }

    /**
     * Method to check if all of the required configuration variables
     * defined in the RequiredEnvironmentVariables enum have been set to a value
     * @return <code>true</code> if all required environment variables have been set, <code>false</code> otherwise
     */
    public static boolean allRequiredEnvironmentVariablesPresent() {
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        var allVariablesPresent = true;
        LOGGER.info("Checking all environment variables present");
        for(RequiredEnvironmentVariables param : RequiredEnvironmentVariables.values()) {
            try{
                environmentReader.getMandatoryString(param.getName());
            } catch (EnvironmentVariableException eve) {
                allVariablesPresent = false;
                LOGGER.error(String.format("Required config item %s missing", param.getName()));
            }
        }

        return allVariablesPresent;
    }
}

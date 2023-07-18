package uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.util.DataMap;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class FilingHistoryDescriptionConverter {
    private final Logger logger;

    private static final String FILING_HISTORY_DESCRIPTIONS_FILEPATH = "api-enumerations/filing_history_descriptions.yml";
    private static final String FILING_HISTORY_KEY = "description";
    final File ordersDescriptionsFile;
    public FilingHistoryDescriptionConverter(Logger logger) {
        this.ordersDescriptionsFile = new File(FILING_HISTORY_DESCRIPTIONS_FILEPATH);
        this.logger = logger;
    }

    public String convertDescriptionCode(String filingHistoryDescriptionCode){
        try(final InputStream inputStream = new FileInputStream(ordersDescriptionsFile)) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> filingHistoryDescriptions = yaml.load(inputStream);
            final Map<String, String> certificateDescriptions =
                    (Map<String, String>) filingHistoryDescriptions.get(FILING_HISTORY_KEY);

            if (certificateDescriptions == null) {
                final String error = "No matching filingHistoryDescriptionCode for value of: " + filingHistoryDescriptionCode;
                logger.error(error, getLogMap(filingHistoryDescriptionCode));
                return null;
            }

            return certificateDescriptions.get(filingHistoryDescriptionCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getLogMap(String filingHistoryDescriptionCode) {
        return new DataMap.Builder()
                .filingHistoryDocumentMetadata(filingHistoryDescriptionCode)
                .build()
                .getLogMap();
    }

}

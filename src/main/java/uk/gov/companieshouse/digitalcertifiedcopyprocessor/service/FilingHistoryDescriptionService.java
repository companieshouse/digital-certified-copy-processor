package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class FilingHistoryDescriptionService {

    private final Logger logger;
    private Map<String, String> filingHistoryDescriptions;
    private final String FILING_HISTORY_DESCRIPTION_KEY = "description";

    public FilingHistoryDescriptionService(
            @Value("api-enumerations/filing_history_descriptions.yml") File fileResource,
            Logger logger) {
        this.logger = logger;
        loadDescriptions(fileResource);
    }

    private void loadDescriptions(final File fileResource) {
        try (final InputStream inputStream = new FileInputStream(fileResource)){
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);
            if(data != null && data.containsKey("description") && data.get(FILING_HISTORY_DESCRIPTION_KEY) instanceof Map)
                filingHistoryDescriptions = (Map<String, String>) data.get(FILING_HISTORY_DESCRIPTION_KEY);
        } catch (IOException ex) {
            String errorMessage = "Failed to load filing_history_descriptions from file," +
                    " all descriptions will default to blank strings";
            logger.error(errorMessage, ex);
        }
    }

    public String getDescription(String descriptionCode) {
        //if the Filing History Descriptions failed to load from the file, return an empty string
        if (filingHistoryDescriptions == null || filingHistoryDescriptions.isEmpty()){
            logger.info("filingHistoryDescriptions map loaded from api-enumerations is null or empty, returning empty string");
            return "";
        }
         String convertedDescription = filingHistoryDescriptions.get(descriptionCode);
        //if we can't match the description, just pass along an empty string
         if (convertedDescription == null) {
             String errorMessage = String.format("Description not found for FilingHistoryDescription code: " +
                             "'%s' Defaulting to an empty string", descriptionCode);
             logger.error(errorMessage);
             return "";
         }
        return convertedDescription;
    }
}

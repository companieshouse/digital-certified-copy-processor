package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FilingHistoryDescriptionService {

    private final Logger logger;
    private Map<String, String> filingHistoryDescriptions;
    private final String FILING_HISTORY_DESCRIPTION_KEY = "description";

    public FilingHistoryDescriptionService(
            @Value("classpath:api-enumerations/filing_history_descriptions.yml") Resource fileResource,
            Logger logger) {
        this.logger = logger;
        loadDescriptions(fileResource);
    }

    private void loadDescriptions(Resource fileResource) {
        try (InputStream inputStream = fileResource.getInputStream()){
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

    public String getDescription(String DescriptionCode) {
        //if we can't match the description, just pass along an empty string
         String convertedDescription = filingHistoryDescriptions.get(DescriptionCode);
         if (convertedDescription == null) {
             String errorMessage = String.format("Description not found for FilingHistoryDescription code: " +
                             "'%s' Defaulting to an empty string", DescriptionCode);
             logger.error(errorMessage);
             return "";
         }
        return convertedDescription;
    }
}

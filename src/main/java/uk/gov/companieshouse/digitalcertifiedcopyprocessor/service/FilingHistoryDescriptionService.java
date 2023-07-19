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
            if(data != null && data.containsKey("description") && data.get("description") instanceof Map)
                filingHistoryDescriptions = (Map<String, String>) data.get("description");
        } catch (IOException e) {
            logger.error("Failed to load filing_history_descriptions from file");
        }
    }

    public String getDescription(String name) {
        //if we can't match the description, just pass along an empty string
        return filingHistoryDescriptions.getOrDefault(name, "");
    }
}

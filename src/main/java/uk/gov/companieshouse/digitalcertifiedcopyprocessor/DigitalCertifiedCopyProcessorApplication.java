package uk.gov.companieshouse.digitalcertifiedcopyprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

@SpringBootApplication
public class DigitalCertifiedCopyProcessorApplication {

    public static final String NAMESPACE = "digital-certified-copy-processor";

    public static void main(String[] args) {
        if (allRequiredEnvironmentVariablesPresent()) {
            SpringApplication.run(DigitalCertifiedCopyProcessorApplication.class, args);
        }
    }

}

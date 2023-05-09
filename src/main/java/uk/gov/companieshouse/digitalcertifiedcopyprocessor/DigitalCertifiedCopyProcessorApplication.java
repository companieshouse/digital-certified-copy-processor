package uk.gov.companieshouse.digitalcertifiedcopyprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DigitalCertifiedCopyProcessorApplication {

    public static final String NAMESPACE = "digital-certified-copy-processor";

    public static void main(String[] args) {
        SpringApplication.run(DigitalCertifiedCopyProcessorApplication.class, args);
    }

}

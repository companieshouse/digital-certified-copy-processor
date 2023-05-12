package uk.gov.companieshouse.digitalcertifiedcopyprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication.NAMESPACE;

@Configuration
public class ApplicationConfiguration {

    @Bean
    Logger getLogger() {
        return LoggerFactory.getLogger(NAMESPACE);
    }

}

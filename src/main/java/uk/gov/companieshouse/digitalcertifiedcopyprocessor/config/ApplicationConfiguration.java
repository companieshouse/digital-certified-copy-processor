package uk.gov.companieshouse.digitalcertifiedcopyprocessor.config;

import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication.NAMESPACE;

@Configuration
public class ApplicationConfiguration {

    @Bean
    Logger getLogger() {
        return LoggerFactory.getLogger(NAMESPACE);
    }

    // TODO DCAC-71 Use SDK
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        final var httpClient = HttpClients.custom().disableRedirectHandling().build();
        final var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return builder.requestFactory(() -> requestFactory).build();
    }

}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.model.filinghistory.FilingApi;
import uk.gov.companieshouse.api.model.filinghistory.FilingLinks;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.ApplicationConfiguration;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.converter.PublicToPrivateUriConverter;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.ApiErrorResponsePayload;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Error;

import java.util.Arrays;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils.givenSdkIsConfigured;


/**
 * Integration tests the {@link FilingHistoryDocumentService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes= {ApplicationConfiguration.class, FilingHistoryDocumentServiceIntegrationTest.Config.class})
@AutoConfigureWireMock(port = 0)
class FilingHistoryDocumentServiceIntegrationTest {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final String COMPANY_NUMBER = "00006400";
    private static final String UNKNOWN_COMPANY_NUMBER = "00000000";
    private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
    private static final String UNKNOWN_ID = "000000000000000000000000";
    private static final String DOCUMENT_METADATA = "/document/specimen";
    public static final ApiErrorResponsePayload FILING_NOT_FOUND =
            new ApiErrorResponsePayload(
                    singletonList(new Error("ch:service", "filing-history-item-not-found")));

    @Configuration
    @ComponentScan(basePackageClasses = FilingHistoryDocumentServiceIntegrationTest.class)
    static class Config {

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setPropertyNamingStrategy(SNAKE_CASE)
                    .findAndRegisterModules();
        }

        @Bean
        public RestTemplateBuilder getRestTemplateBuilder() {
            return new RestTemplateBuilder();
        }

        @Bean
        public PublicToPrivateUriConverter getPublicToPrivateUriConverter() {
            return new PublicToPrivateUriConverter();
        }
    }

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @Test
    @DisplayName("getDocumentMetadata() gets the expected filing history document metadata successfully")
    void getDocumentMetadataGetsMetadataSuccessfully() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getFilingHistoryResponsePayload())));

        // When
        final String metadata =
                serviceUnderTest.getDocumentMetadata(COMPANY_NUMBER, ID_1);

        // Then
        assertThat(metadata, is(DOCUMENT_METADATA));
    }

    @Test
    @DisplayName("getDocumentMetadata() throws 400 Bad Request for an unknown company")
    void getDocumentMetadataThrowsBadRequestForUnknownCompany() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + UNKNOWN_COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getDocumentMetadata(UNKNOWN_COMPANY_NUMBER, ID_1));
        assertThat(exception.getStatus(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + ID_1 +
                " for company number " + UNKNOWN_COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getDocumentMetadata() throws 400 Bad Request for an unknown filing history document")
    void getDocumentMetadataThrowsBadRequestForUnknownFilingHistoryDocument() throws JsonProcessingException {

        // Given
        givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + UNKNOWN_ID))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(FILING_NOT_FOUND))));

        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getDocumentMetadata(COMPANY_NUMBER, UNKNOWN_ID));
        assertThat(exception.getStatus(), Is.is(BAD_REQUEST));
        final String expectedReason = "Error getting filing history document " + UNKNOWN_ID +
                " for company number " + COMPANY_NUMBER + ".";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    @Test
    @DisplayName("getDocumentMetadata() throws 500 Internal Server Error for connection failure")
    void getDocumentMetadataThrowsInternalServerErrorForForConnectionFailure() {

        // Given
        final String wireMockPort = givenSdkIsConfigured(environment, environmentVariables);
        givenThat(get(urlEqualTo("/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        // When and then
        final ResponseStatusException exception =
                Assertions.assertThrows(ResponseStatusException.class,
                        () -> serviceUnderTest.getDocumentMetadata(COMPANY_NUMBER, ID_1));
        assertThat(exception.getStatus(), Is.is(INTERNAL_SERVER_ERROR));
        final String expectedReason = "Error sending request to http://localhost:"
                + wireMockPort + "/company/" + COMPANY_NUMBER + "/filing-history/" + ID_1 + ": Connection reset";
        assertThat(exception.getReason(), Is.is(expectedReason));
    }

    private String getFilingHistoryResponsePayload() throws JsonProcessingException {
        final FilingApi filing = new FilingApi();
        final FilingLinks links = new FilingLinks();
        links.setDocumentMetaData(DOCUMENT_METADATA);
        filing.setLinks(links);
        return objectMapper.writeValueAsString(filing);
    }

}



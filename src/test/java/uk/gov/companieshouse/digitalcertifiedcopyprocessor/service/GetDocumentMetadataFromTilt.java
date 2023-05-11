package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker;
import uk.gov.companieshouse.logging.Logger;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils.givenSdkIsConfiguredForTilt;


/**
 *  "Test" class re-purposed to execute {@link FilingHistoryDocumentService#getDocumentMetadata(String, String)}
 *  to retrieve document metadata from the filing history API in Tilt. This is NOT to be run as part of
 *  an automated test suite. It is for manual testing only.
 */
@SpringBootTest
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class GetDocumentMetadataFromTilt {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private static final String COMPANY_NUMBER = "00006400";
    private static final String ID_1 = "MDAxMTEyNzExOGFkaXF6a2N4";
    private static final String EXPECTED_DOCUMENT_METADATA = "/document/specimen";

    @Autowired
    private FilingHistoryDocumentService serviceUnderTest;

    @Autowired
    private Logger logger;

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @Test
    @DisplayName("get filing history document metadata from Tilt")
    void getDocumentMetadataFromTilt() {

        // Given
        givenSdkIsConfiguredForTilt(environmentVariables);

        // When
        final String metadata =
                serviceUnderTest.getDocumentMetadata(COMPANY_NUMBER, ID_1);

        logger.info("Document metadata returned = " + metadata);

        // Then
        assertThat(metadata, is(EXPECTED_DOCUMENT_METADATA));
    }

}



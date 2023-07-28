package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;


import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilingHistoryDescriptionServiceTest {

    @Autowired
    private FilingHistoryDescriptionService filingHistoryDescriptionService;

    @Mock
    Logger logger;

    @BeforeEach
    public void setUp(){
        Resource testFileResource = new ClassPathResource("test_filing_history_descriptions.yml");
        filingHistoryDescriptionService = new FilingHistoryDescriptionService(testFileResource, logger);
    }

    @Test
    public void testGetDescriptionReturnsCorrectValue() {
        String descriptionKey = "test1";
        String expectedResult = "value1";
        String result = filingHistoryDescriptionService.getDescription(descriptionKey);
        assertEquals(expectedResult, result);
    }

    @Test
    @Description("When the api-enumerations does not c, we should just return an empty string")
    public void testGetDescriptionReturnsEmptyStringForMissingKey() {
        String descriptionKey = "wrong";
        String result = filingHistoryDescriptionService.getDescription(descriptionKey);
        assertEquals("", result);
    }

    @Test
    @Description("When the api-enumerations file is failed to load, we should just return an empty string")
    public void testFileNotFound() throws IOException {
        Resource mockFileResource = mock(Resource.class);
        when(mockFileResource.getInputStream()).thenThrow(new IOException());
        FilingHistoryDescriptionService filingHistoryDescriptionServiceNotFound = new FilingHistoryDescriptionService(mockFileResource, logger);
        String result = filingHistoryDescriptionServiceNotFound.getDescription("corn on the cob");
        assertEquals("", result);
    }
}

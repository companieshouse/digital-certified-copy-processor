package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;


import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
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
        File testFileResource = new File("src/test/resources/test_filing_history_descriptions.yml");
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
        File fakeFileResource = new File("this/file/is/not/real");
        String descriptionKey = "test1";

        FilingHistoryDescriptionService filingHistoryDescriptionServiceNotFound =
                new FilingHistoryDescriptionService(fakeFileResource, logger);
        String result = filingHistoryDescriptionServiceNotFound.getDescription(descriptionKey);
        assertEquals("", result);
    }
}

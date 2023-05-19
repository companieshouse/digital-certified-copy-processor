package uk.gov.companieshouse.digitalcertifiedcopyprocessor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;

@SpringBootTest
@SpringJUnitConfig(TestConfig.class)
class DigitalCertifiedCopyProcessorApplicationTests {

	@SuppressWarnings("squid:S2699") // at least one assertion
	@DisplayName("context loads")
	@Test
	void contextLoads() {
	}

}

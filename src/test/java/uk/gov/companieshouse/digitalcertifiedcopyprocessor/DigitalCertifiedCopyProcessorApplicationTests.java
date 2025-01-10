package uk.gov.companieshouse.digitalcertifiedcopyprocessor;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.Consumer;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

@SpringBootTest
@EmbeddedKafka
@SpringJUnitConfig(TestConfig.class)
class DigitalCertifiedCopyProcessorApplicationTests {

	@MockitoBean
	KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;
	@MockitoBean
	KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;
	@MockitoBean
	Consumer consumer;

	@SuppressWarnings("squid:S2699") // at least one assertion
	@DisplayName("context loads")
	@Test
	void contextLoads() {
	}

}

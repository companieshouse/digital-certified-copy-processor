package uk.gov.companieshouse.digitalcertifiedcopyprocessor;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.Consumer;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

@SpringBootTest
@SpringJUnitConfig(TestConfig.class)
class DigitalCertifiedCopyProcessorApplicationTests {

	@MockBean
	KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;
	@MockBean
	KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;
	@MockBean
	Consumer consumer;

	@SuppressWarnings("squid:S2699") // at least one assertion
	@DisplayName("context loads")
	@Test
	void contextLoads() {
	}

}

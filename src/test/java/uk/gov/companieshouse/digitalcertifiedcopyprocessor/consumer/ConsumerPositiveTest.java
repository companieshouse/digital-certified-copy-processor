package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaService;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaServiceParameters;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.CERTIFIED_COPY;

@SpringBootTest(classes = DigitalCertifiedCopyProcessorApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        topics = {"echo", "echo-retry", "echo-error", "echo-invalid"},
        controlledShutdown = true,
        partitions = 1
)
@TestPropertySource(locations = "classpath:application-test_main_positive.yml")
@Import(TestConfig.class)
@ActiveProfiles("test_main_positive")
class ConsumerPositiveTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;

    @Autowired
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;

    @Autowired
    private CountDownLatch latch;

    @MockBean
    private KafkaService service;

    @Test
    void testConsumeFromMainTopic() throws InterruptedException {
        //given
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(testConsumer);

        //when
        testProducer.send(new ProducerRecord<>(
                "echo", 0, System.currentTimeMillis(), "Key", CERTIFIED_COPY));
        if (!latch.await(30L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, 10000L, 1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-retry"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-error"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-invalid"), is(0));
        verify(service).processMessage(new KafkaServiceParameters(CERTIFIED_COPY));
    }
}

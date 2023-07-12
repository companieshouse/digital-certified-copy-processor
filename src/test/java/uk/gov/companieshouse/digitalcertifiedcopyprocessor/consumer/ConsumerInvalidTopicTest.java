package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.TestUtils;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.CERTIFIED_COPY;

@SpringBootTest(classes = DigitalCertifiedCopyProcessorApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        topics = {"echo", "echo-retry", "echo-error", "echo-invalid"},
        controlledShutdown = true,
        partitions = 1
)
@TestPropertySource(locations = "classpath:application-test_main_nonretryable.yml")
@Import(TestConfig.class)
@ActiveProfiles("test_main_nonretryable")
class ConsumerInvalidTopicTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;

    @Autowired
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;

    @Test
    void testPublishToInvalidMessageTopicIfInvalidDataDeserialised() throws InterruptedException, ExecutionException {
        //given
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(testConsumer);

        //when
        Future<RecordMetadata> future =
                testProducer.send(new ProducerRecord<>(
                        "echo",
                        0,
                        System.currentTimeMillis(),
                        "key",
                        CERTIFIED_COPY));
        future.get();
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, 10000L, 2);

        //then
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-retry"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-error"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-invalid"), is(1));
    }
}
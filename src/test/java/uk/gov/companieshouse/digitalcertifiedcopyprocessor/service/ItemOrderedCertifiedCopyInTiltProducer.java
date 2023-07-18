package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.CERTIFIED_COPY_2;

/**
 * "Test" class re-purposed to produce {@link ItemOrderedCertifiedCopy} messages to the
 * <code>item-ordered-certified-copy</code> topic in Tilt. This is NOT to be run as part
 * of an automated test suite. It is for manual testing only.
 */
@SpringBootTest
@TestPropertySource(locations="classpath:item-ordered-certified-copy-in-tilt.properties")
@Tag("manual")
@Import(TestConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class ItemOrderedCertifiedCopyInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemOrderedCertifiedCopyInTiltProducer");

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    private static final String SAME_PARTITION_KEY = "key";

    @Value("${consumer.topic}")
    private String itemOrderedCertifiedCopyTopic;

    @Autowired
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
                itemOrderedCertifiedCopyTopic, 0, System.currentTimeMillis(), SAME_PARTITION_KEY, CERTIFIED_COPY_2));
        final var result = future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var partition = result.partition();
        final var offset = result.offset();
        LOGGER.info("Message " + CERTIFIED_COPY_2 + " delivered to topic " + itemOrderedCertifiedCopyTopic
                + " on partition " + partition + " with offset " + offset + ".");
    }

}

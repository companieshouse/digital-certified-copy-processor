package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.core.env.Environment;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {

    public static final String MAIN_TOPIC = "echo";
    public static final String RETRY_TOPIC = "echo-retry";
    public static final String ERROR_TOPIC = "echo-error";
    public static final String INVALID_TOPIC = "echo-invalid";

    /**
     * Configures the CH Java SDKs used in WireMock based integration tests to interact with WireMock
     * stubbed/mocked API endpoints.
     * @param environment the Spring {@link Environment} the tests are run in
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @return the WireMock port value used for the tests
     */
    public static String givenSdkIsConfigured(final Environment environment, final EnvironmentVariables variables) {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://localhost:" + wireMockPort);
        variables.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        variables.set("DOCUMENT_API_LOCAL_URL", "http://localhost:" + wireMockPort);
        return wireMockPort;
    }

    /**
     * Configures the CH Java SDKs used in manual tests to interact with Tilt
     * hosted API endpoints.
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @see #givenSdkIsConfigured(EnvironmentVariables, Map)
     */
    public static void givenSdkIsConfiguredForTilt(final EnvironmentVariables variables) {
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://api.chs.local:4001");
        variables.set("PAYMENTS_API_URL", "http://api.chs.local:4001");
        variables.set("DOCUMENT_API_LOCAL_URL", "http://document-api-cidev.aws.chdev.org");
    }

    /**
     * Configures the CH Java SDKs used in manual tests to interact with API endpoints.
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @param variableValues map of key-value pairs to override settings which otherwise default to acceptable
     *                       values for testing with Tilt hosted API endpoints
     */
    public static void givenSdkIsConfigured(final EnvironmentVariables variables,
                                            final Map<String, String> variableValues) {
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://api.chs.local:4001");
        variables.set("PAYMENTS_API_URL", "http://api.chs.local:4001");
        variables.set("DOCUMENT_API_LOCAL_URL", "http://document-api-cidev.aws.chdev.org");
        variableValues.forEach(variables::set);
    }

    public static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    /**public static ConsumerRecords<?, ?> setUpSendAndWaitForMessage(final Environment environment,
                                                                   final KafkaConsumer<String, ItemGroupProcessed> testConsumer,
                                                                   final KafkaProducer<String, ItemGroupProcessed> testProducer,
                                                                   final CountDownLatch latch) throws Exception {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
                .and("CHS_API_KEY", "Token value")
                .and("PAYMENTS_API_URL", "NOT-USED")
                .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
                .execute(() -> sendAndWaitForMessage(testProducer, latch));
        return KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10L), 6);
    }

    private static void sendAndWaitForMessage(
            final KafkaProducer<String, ItemGroupProcessed> testProducer, final CountDownLatch latch)
            throws InterruptedException {
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
                TestConstants.ITEM_GROUP_PROCESSED));
        if (!latch.await(30L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
    }**/

}

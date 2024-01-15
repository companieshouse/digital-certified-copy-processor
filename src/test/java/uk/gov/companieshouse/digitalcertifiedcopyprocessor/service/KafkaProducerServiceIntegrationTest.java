package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import consumer.deserialization.AvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.KafkaConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.MessageFlags;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentFactory;
import uk.gov.companieshouse.documentsigning.CoverSheetDataRecord;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.CERTIFIED_COPY;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.PRIVATE_DOCUMENT_URI;

/**
 * Integration tests the {@link KafkaProducerService}.
 */
@SpringBootTest
@SpringJUnitConfig(classes={
        KafkaConfig.class,
        KafkaProducerService.class,
        SignDigitalDocumentFactory.class,
        KafkaProducerServiceIntegrationTest.Config.class
})
@EmbeddedKafka
class KafkaProducerServiceIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("KafkaProducerServiceIntegrationTest");
    private static final String SIGN_DIGITAL_DOCUMENT_TOPIC = "sign-digital-document";
    private static final String FILING_HISTORY_DESCRIPTION = "**Resignation of a liquidator**";
    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;
    
    private static final Map<String, String> FilingHistoryDescriptions =
            Map.of("appointment_date", "2023-05-01",
            "officer_name", "Mr Tom Sunburn");

    public static final CoverSheetDataRecord coverSheetData = CoverSheetDataRecord.newBuilder()
            .setCompanyName(CERTIFIED_COPY.getCompanyName())
            .setCompanyNumber(CERTIFIED_COPY.getCompanyNumber())
            .setDescription(FILING_HISTORY_DESCRIPTION)
            .setType(CERTIFIED_COPY.getFilingHistoryType())
            .build();

    private static final SignDigitalDocument EXPECTED_SIGN_DIGITAL_DOCUMENT_MESSAGE = SignDigitalDocument.newBuilder()
            .setCoverSheetData(coverSheetData)
            .setOrderNumber(CERTIFIED_COPY.getOrderNumber())
            .setPrivateS3Location(PRIVATE_DOCUMENT_URI.toString())
            .setDocumentType("certified-copy")
            .setGroupItem(CERTIFIED_COPY.getGroupItem())
            .setItemId(CERTIFIED_COPY.getItemId())
            .setFilingHistoryDescriptionValues(FilingHistoryDescriptions)
            .build();

    @Autowired
    private KafkaProducerService serviceUnderTest;

    @MockBean
    private Logger logger;

    @MockBean
    MessageFlags messageFlags;

    private final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
    private SignDigitalDocument messageReceived;

    @Configuration
    @EnableKafka
    static class Config {

        @Bean
        public ConsumerFactory<String, SignDigitalDocument> consumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            return new DefaultKafkaConsumerFactory<>(
                    Map.of(
                            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                    new StringDeserializer(),
                    new ErrorHandlingDeserializer<>(new AvroDeserializer<>(SignDigitalDocument.class)));
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, SignDigitalDocument> kafkaListenerContainerFactory(
                ConsumerFactory<String, SignDigitalDocument> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, SignDigitalDocument> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
            return factory;
        }

    }

    @Test
    @DisplayName("KafkaProducerService produces a message to sign-digital-document successfully")
    void producesMessageSuccessfully() throws InterruptedException {

        serviceUnderTest.sendMessage(CERTIFIED_COPY, PRIVATE_DOCUMENT_URI, FILING_HISTORY_DESCRIPTION);

        verifyExpectedMessageIsReceived();
    }

    @KafkaListener(topics = SIGN_DIGITAL_DOCUMENT_TOPIC, groupId = "test-group")
    public void receiveMessage(final @Payload SignDigitalDocument message) {
        LOGGER.info("Received message: " + message);
        messageReceived = message;
        messageReceivedLatch.countDown();
    }

    private void verifyExpectedMessageIsReceived() throws InterruptedException {
        verifyWhetherMessageIsReceived(true);
        assertThat(messageReceived, is(notNullValue()));
        assertThat(Objects.deepEquals(messageReceived, EXPECTED_SIGN_DIGITAL_DOCUMENT_MESSAGE), is(true));
    }

    private void verifyWhetherMessageIsReceived(final boolean messageIsReceived) throws InterruptedException {
        LOGGER.info("Waiting to receive message for up to " + MESSAGE_WAIT_TIMEOUT_SECONDS + " seconds.");
        final var received = messageReceivedLatch.await(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(received, is(messageIsReceived));
    }

}

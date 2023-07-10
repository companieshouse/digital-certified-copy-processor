package uk.gov.companieshouse.digitalcertifiedcopyprocessor.config;

import consumer.deserialization.AvroDeserializer;
import consumer.serialization.AvroSerializer;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.NonRetryableExceptionService;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.service.KafkaService;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@TestConfiguration
public class TestConfig {

    @Bean
    public RestTemplateBuilder getRestTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        final var httpClient = HttpClients.custom().disableRedirectHandling().build();
        final var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return builder.requestFactory(() -> requestFactory).build();
    }

    @Bean
    CountDownLatch latch(@Value("${steps}") int steps) {
        return new CountDownLatch(steps);
    }

    @Bean
    KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaConsumer<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroSerializer.class,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()),
                new StringDeserializer(), new AvroDeserializer<>(ItemOrderedCertifiedCopy.class));
    }

    @Bean
    KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaProducer<>(
                Map.of(
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new StringSerializer(),
                (topic, data) -> {
                    try {
                        return new SerializerFactory().getSpecificRecordSerializer(
                                ItemOrderedCertifiedCopy.class).toBinary(data); //creates a leading space
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Bean
    @Primary
    public KafkaService getService() {
        return new NonRetryableExceptionService();
    }
}

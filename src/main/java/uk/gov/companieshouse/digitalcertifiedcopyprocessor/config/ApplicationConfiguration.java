package uk.gov.companieshouse.digitalcertifiedcopyprocessor.config;

import consumer.deserialization.AvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.codehaus.jackson.map.deser.std.ClassDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.InvalidMessageRouter;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.MessageFlags;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.Map;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.DigitalCertifiedCopyProcessorApplication.NAMESPACE;

@Configuration
@EnableKafka
public class ApplicationConfiguration {

    @Bean
    public ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new AvroDeserializer<>(ItemOrderedCertifiedCopy.class)));
    }

    @Bean
    public ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            MessageFlags messageFlags,
            @Value("${invalid_message_topic}") String invalidMessageTopic) {
        return new DefaultKafkaProducerFactory<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, InvalidMessageRouter.class.getName(),
                        "message.flags", messageFlags,
                        "invalid.message.topic", invalidMessageTopic),
                new StringSerializer(),
                (topic, data) -> {
                    try {
                        return new SerializerFactory().getSpecificRecordSerializer(ItemOrderedCertifiedCopy.class)
                                .toBinary(data); //creates a leading space
                    } catch (SerializationException e) {
                        var dataMap = new DataMap.Builder()
                                .topic(topic)
                                .kafkaMessage(data.toString())
                                .build();
                        getLogger().error("Caught SerializationException serializing kafka message.",
                                dataMap.getLogMap());
                        throw new RuntimeException(e);
                    }
                });
    }

    @Bean
    public KafkaTemplate<String, ItemOrderedCertifiedCopy> kafkaTemplate(ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemOrderedCertifiedCopy> kafkaListenerContainerFactory(ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory,
                                                                                                 @Value("${consumer.concurrency}") Integer concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, ItemOrderedCertifiedCopy> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    Logger getLogger() {
        return LoggerFactory.getLogger(NAMESPACE);
    }
}

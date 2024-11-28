package uk.gov.companieshouse.digitalcertifiedcopyprocessor.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentAvroSerializer;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, SignDigitalDocument> signProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        final Map<String, Object> config = new HashMap<>();
        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SignDigitalDocumentAvroSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, SignDigitalDocument> signKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}" ) final String bootstrapServers) {
        return new KafkaTemplate<>(signProducerFactory(bootstrapServers));
    }

    /**@Bean
    public KafkaTemplate<String, ItemOrderedCertifiedCopy> defaultRetryTopicKafkaTemplate(
            ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }**/

    @Bean
    public SignDigitalDocumentAvroSerializer avroSerializer() {
        return new SignDigitalDocumentAvroSerializer();
    }

}

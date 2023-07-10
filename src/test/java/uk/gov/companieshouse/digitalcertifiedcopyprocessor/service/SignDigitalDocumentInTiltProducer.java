package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.DOCUMENT;

/**
 * "Test" class re-purposed to produce {@link SignDigitalDocument} messages to the <code>sign-digital-document</code>
 * topic in Tilt. This is NOT to be run as part of an automated test suite. It is for manual testing only.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL}")
@ActiveProfiles("manual")
@Tag("manual")
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class SignDigitalDocumentInTiltProducer {

    private static final String KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL = "localhost:29092";
    private static final String TOPIC_NAME_IN_TILT = "sign-digital-document";

    public static class KafkaTemplateAvroSerializer implements Serializer<SignDigitalDocument> {

        @Override
        public byte[] serialize(String topic, SignDigitalDocument data) {
            DatumWriter<SignDigitalDocument> datumWriter = new SpecificDatumWriter<>();

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                datumWriter.setSchema(data.getSchema());
                datumWriter.write(data, encoder);
                encoder.flush();

                byte[] serializedData = out.toByteArray();
                encoder.flush();

                return serializedData;
            } catch (IOException e) {
                throw new SerializationException("Error when serializing ItemOrderedCertifiedCopy to byte[]");
            }
        }
    }

    @Configuration
    @Profile("manual")
    static class KafkaTemplateConfig {

        @Bean
        public ProducerFactory<String, SignDigitalDocument> producerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaTemplateAvroSerializer.class);
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL);
            return new DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        public KafkaTemplate<String, SignDigitalDocument> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }

        @Bean
        public KafkaTemplateAvroSerializer avroSerializer() {
            return new KafkaTemplateAvroSerializer();
        }

    }

    @Autowired
    private KafkaTemplate<String, SignDigitalDocument> testTemplate;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() {
        testTemplate.send(new ProducerRecord<>(TOPIC_NAME_IN_TILT, DOCUMENT));
    }
}

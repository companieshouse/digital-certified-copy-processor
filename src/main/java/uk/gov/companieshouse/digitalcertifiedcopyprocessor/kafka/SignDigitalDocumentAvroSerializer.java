package uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignDigitalDocumentAvroSerializer implements Serializer<SignDigitalDocument> {

    @Override
    public byte[] serialize(String topic, SignDigitalDocument data) {
        final DatumWriter<SignDigitalDocument> datumWriter = new SpecificDatumWriter<>();

        try (final var out = new ByteArrayOutputStream()) {
            final var encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.setSchema(data.getSchema());
            datumWriter.write(data, encoder);
            encoder.flush();

            byte[] serializedData = out.toByteArray();
            encoder.flush();

            return serializedData;
        } catch (IOException e) {
            throw new SerializationException("Error when serializing SignDigitalDocument to byte[]");
        }
    }
}

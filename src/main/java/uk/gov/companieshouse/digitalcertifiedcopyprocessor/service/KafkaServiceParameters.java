package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Objects;

/**
 * Contains all parameters required by {@link KafkaService service implementations}.
 */
public class KafkaServiceParameters {

    private final ItemOrderedCertifiedCopy data;

    public KafkaServiceParameters(ItemOrderedCertifiedCopy data) {
        this.data = data;
    }

    /**
     * Get data attached to the ServiceParameters object.
     *
     * @return A string representing data that has been attached to the ServiceParameters object.
     */
    public ItemOrderedCertifiedCopy getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KafkaServiceParameters)) {
            return false;
        }
        KafkaServiceParameters that = (KafkaServiceParameters) o;
        return Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }
}

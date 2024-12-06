package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Objects;

/**
 * Contains all parameters required by {@link KafkaService service implementations}.
 */
public record KafkaServiceParameters(ItemOrderedCertifiedCopy data) {

    /**
     * Get data attached to the ServiceParameters object.
     *
     * @return A string representing data that has been attached to the ServiceParameters object.
     */
    @Override
    public ItemOrderedCertifiedCopy data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KafkaServiceParameters that)) {
            return false;
        }
        return Objects.equals(data(), that.data());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data());
    }
}

package uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.Consumer;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.API_URL;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CHS_API_KEY;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.DOCUMENT_API_LOCAL_URL;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.PAYMENTS_API_URL;

@SpringBootTest
@EmbeddedKafka
@SpringJUnitConfig(TestConfig.class)
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @MockBean
    KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;
    @MockBean
    KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;
    @MockBean
    ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory;
    @MockBean
    ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory;

    @MockBean
    Consumer consumer;

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(this::accept);
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertThat(allPresent, is(true));
    }

    @DisplayName("returns false if API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(API_URL);
    }

    @DisplayName("returns false if PAYMENTS_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPaymentsApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PAYMENTS_API_URL);
    }

    @DisplayName("returns false if CHS_API_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsApiKeyMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_API_KEY);
    }

    @DisplayName("returns false if DOCUMENT_API_LOCAL_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfDocumentApiLocalUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(DOCUMENT_API_LOCAL_URL);
    }

    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
            final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            if (variable != excludedVariable) {
                environmentVariables.set(variable.getName(), TOKEN_VALUE);
            }
        });
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertFalse(allPresent);
    }

    private void accept(EnvironmentVariablesChecker.RequiredEnvironmentVariables variable) {
        environmentVariables.set(variable.getName(), TOKEN_VALUE);
    }
}
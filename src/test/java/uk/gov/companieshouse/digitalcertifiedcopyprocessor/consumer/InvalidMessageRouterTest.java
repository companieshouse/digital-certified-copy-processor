package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.ITEM_ORDERED_CERTIFIED_COPY;

@ExtendWith(MockitoExtension.class)
class InvalidMessageRouterTest {

    private InvalidMessageRouter invalidMessageRouter;

    @Mock
    private MessageFlags flags;

    @BeforeEach
    void setup() {
        invalidMessageRouter = new InvalidMessageRouter();
        invalidMessageRouter.configure(Map.of("message.flags", flags, "invalid.message.topic", "invalid"));
    }

    @Test
    void testOnSendRoutesMessageToInvalidMessageTopicIfNonRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ItemOrderedCertifiedCopy> message =
                new ProducerRecord<>("main", "key", ITEM_ORDERED_CERTIFIED_COPY);

        // when
        ProducerRecord<String, ItemOrderedCertifiedCopy> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(equalTo(new ProducerRecord<>("invalid", "key", ITEM_ORDERED_CERTIFIED_COPY))));
    }

    @Test
    void testOnSendRoutesMessageToTargetTopicIfRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ItemOrderedCertifiedCopy> message =
                new ProducerRecord<>("main", "key", ITEM_ORDERED_CERTIFIED_COPY);
        when(flags.isRetryable()).thenReturn(true);

        // when
        ProducerRecord<String, ItemOrderedCertifiedCopy> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(sameInstance(message)));
    }

}

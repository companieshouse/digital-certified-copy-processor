package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Flags related to the state of an individual message that has been processed.
 */
@Component
public class MessageFlags {

    private static final ThreadLocal<Boolean> retryableFlagContainer = new ThreadLocal<>();

    /**
     * Set the retryable flag.
     *
     * @param retryable True if a retryable error has occurred, otherwise false.
     */
    public void setRetryable(boolean retryable) {
        retryableFlagContainer.set(retryable);
    }

    /**
     * Get the retryable flag.
     *
     * @return True if a retryable error has occurred, otherwise false.
     */
    public boolean isRetryable() {
        Boolean retryable = retryableFlagContainer.get();
        return retryable != null && retryable;
    }

    /**
     * Clear all flags.
     */
    @PreDestroy
    public void destroy() {
        retryableFlagContainer.remove();
    }
}

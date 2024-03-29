package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Aspect
@Component
public class ConsumerAspect {

    private final CountDownLatch latch;

    public ConsumerAspect(CountDownLatch latch) {
        this.latch = latch;
    }

    @After("execution(* uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer.Consumer.consume(..))")
    void afterConsume(JoinPoint joinPoint) {
        latch.countDown();
    }
}

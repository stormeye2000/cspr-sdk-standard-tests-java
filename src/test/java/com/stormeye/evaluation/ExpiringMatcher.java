package com.stormeye.evaluation;

import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A matcher that expires if a match has not occurred within a given time in seconds.
 *
 * @author ian@meywood.com
 */
public class ExpiringMatcher<T> {

    private final Logger logger = LoggerFactory.getLogger(ExpiringMatcher.class);

    /** The matcher to use against the result */
    private final Matcher<T> matcher;
    private final Semaphore semaphore;
    private boolean passed = false;
    private Exception error;

    public ExpiringMatcher(final Matcher<T> matcher) {
        this.matcher = matcher;
        this.semaphore = new Semaphore(1);

        AtomicInteger counter = new AtomicInteger(0);

        // Acquire the semaphore in new thread to lock it
        new Thread(() -> {
            acquireSemaphore();
            counter.incrementAndGet();

        }).start();

        do {
            try {
                //noinspection BusyWait
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (counter.get() == 0);
    }


    public void match(final Object actual) {
        try {
            if (matcher.matches(actual)) {
                passed = true;
                semaphore.release();
            }
        } catch (final Exception e) {
            logger.error("Error in ExpiringMatcher", e);
            this.error = e;
            semaphore.release();
            throw new RuntimeException(e);
        }
    }

    public boolean waitForMatch(final long timeoutSeconds) throws Exception {

        //noinspection ResultOfMethodCallIgnored
        semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);

        if (error != null) {
            throw error;
        }
        return passed;
    }

    private void acquireSemaphore() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.stormeye.evaluation;

import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * A matcher that expires if a match has not occurred within a given time in seconds.
 *
 * @author ian@meywood.com
 */
public class ExpiringMatcher<T> {

    private final Logger logger = LoggerFactory.getLogger(ExpiringMatcher.class);

    /** The matcher to use against the result */
    private final Matcher matcher;
    /** The time in seconds that the matcher must be invoked before timing out */
    private final long timeoutSeconds;

    private final Semaphore semaphore;

    private final Timer timer;

    private boolean passed = false;
    private Exception error;

    public ExpiringMatcher(final Matcher<T> matcher, final long timeoutSeconds) {
        this.matcher = matcher;
        this.timeoutSeconds = timeoutSeconds;
        this.semaphore = new Semaphore(1);

        acquireSemaphore();

        timer = new Timer();

        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        semaphore.release();
                    }
                },
                timeoutSeconds * 1000L
        );
    }


    public void match(final Object actual) {
        try {
            if (matcher.matches(actual)) {
                passed = true;
                timer.cancel();
                semaphore.release();
            }
        } catch (Exception e) {
            logger.error("Error in ExpiringMatcher",e);
            this.error = e;
            timer.cancel();
            semaphore.release();
            throw new RuntimeException(e);
        }
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean waitForMatch() throws Exception{
        acquireSemaphore();

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

    private Throwable getError() {
        return this.error;
    }
}

package com.stormeye.evaluation;

import com.casper.sdk.model.event.Event;
import com.casper.sdk.model.event.EventTarget;
import com.casper.sdk.model.event.EventType;
import com.stormeye.utils.CasperClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ian@meywood.com
 */
public class EventHandler {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    private final MatcherMap matcherMap = new MatcherMap();

    private final List<AutoCloseable> sseSources = new ArrayList<>();

    public EventHandler() throws InterruptedException {

        consume(EventType.DEPLOYS);
        consume(EventType.MAIN);
        consume(EventType.SIGS);

        // Give the threads as chance to start
        Thread.sleep(2000L);
    }

    public void close() {

        for (AutoCloseable sseSource : sseSources) {
            try {
                sseSource.close();
            } catch (Exception e) {
                logger.error("Error closing SSE Source", e);
            }
        }
    }


    private void consume(final EventType eventType) {

        sseSources.add(
                CasperClientProvider.getInstance().getEventService().consumeEvents(eventType, EventTarget.POJO, null, event -> {
                    logger.info("Got {} event {}", eventType, event);
                    handleMatchers(event);
                }, throwable -> {
                    logger.error("Error processing SSE event", throwable);
                })
        );
    }


    public void addEventMatcher(final EventType eventType, ExpiringMatcher<?> matcher) {
        matcherMap.addEventMatcher(eventType, matcher);
    }

    private void handleMatchers(Event<?> event) {
        matcherMap.handleEvent(event);
    }
}

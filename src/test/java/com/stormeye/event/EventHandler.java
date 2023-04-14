package com.stormeye.event;

import com.casper.sdk.model.event.Event;
import com.casper.sdk.model.event.EventTarget;
import com.casper.sdk.model.event.EventType;
import com.stormeye.matcher.MatcherMap;
import com.stormeye.utils.CasperClientProvider;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper class for consuming and matching events.
 *
 * @author ian@meywood.com
 */
public class EventHandler {

    private final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private final MatcherMap matcherMap = new MatcherMap();
    private final List<AutoCloseable> sseSources = new ArrayList<>();

    public EventHandler() {

        consume(EventType.DEPLOYS);
        consume(EventType.MAIN);
        consume(EventType.SIGS);
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
                }, throwable -> logger.error("Error processing SSE event", throwable))
        );
    }

    public <T> Matcher<T> addEventMatcher(final EventType eventType, final Matcher<T> matcher) {
        matcherMap.addEventMatcher(eventType, matcher);
        return matcher;
    }

    private void handleMatchers(Event<?> event) {
        matcherMap.handleEvent(event);
    }

    public <T> void removeEventMatcher(final EventType eventType, final Matcher<T> matcher) {
        matcherMap.removeEventMatcher(eventType, matcher);
    }
}

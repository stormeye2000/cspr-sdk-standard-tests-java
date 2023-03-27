package com.stormeye.evaluation;

import com.casper.sdk.model.event.Event;
import com.casper.sdk.model.event.EventType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ian@meywood.com
 */
public class MatcherMap {

    private final Map<EventType, List<ExpiringMatcher>> eventTypeListMap = new LinkedHashMap<>();

    public void addEventMatcher(final EventType eventType, ExpiringMatcher<?> matcher) {
        getMatchers(eventType).add(matcher);
    }


    private List<ExpiringMatcher> getMatchers(final EventType eventType) {
        return eventTypeListMap.computeIfAbsent(eventType, k -> new ArrayList<>());
    }


    public void handleEvent(Event<?> event) {
        List<ExpiringMatcher> matchers = getMatchers(event.getEventType());
        matchers.forEach(matcher -> matcher.match(event));
    }
}

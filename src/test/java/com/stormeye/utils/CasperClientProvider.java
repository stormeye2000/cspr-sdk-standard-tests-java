package com.stormeye.utils;

import com.casper.sdk.service.CasperService;
import com.casper.sdk.service.EventService;
import com.stormeye.execption.TestException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ian@meywood.com
 */
public class CasperClientProvider {

    private static CasperClientProvider instance;
    private final CasperService casperService;
    private final EventService eventService;

    public static CasperClientProvider getInstance() {
        if (instance == null) {
            instance = new CasperClientProvider();
        }
        return instance;
    }

    private CasperClientProvider() {

        try {
            final TestProperties properties = new TestProperties();
            casperService = CasperService.usingPeer(properties.getHostname(), properties.getRcpPort());
            //noinspection HttpUrlsUsage
            eventService = EventService.usingPeer(new URI("http://" + properties.getHostname() + ":" + properties.getSsePort()));
        } catch (MalformedURLException | URISyntaxException e) {
            throw new TestException(e);
        }
    }

    public CasperService getCasperService() {
        return casperService;
    }

    public EventService getEventService() {
        return eventService;
    }
}

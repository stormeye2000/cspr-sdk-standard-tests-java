package com.stormeye.utils;

/**
 * @author ian@meywood.com
 */
public class TestProperties {

    private final String hostname;
    private final String dockerName;
    private final int rcpPort;
    private final int restPort;
    private final int ssePort;

    public TestProperties() {

        this.hostname = getProperty("cspr.hostname", "localhost");
        this.dockerName = getProperty("cspr.docker.name", "storm-nctl");
        this.rcpPort = getIntProperty("cspr.port.rcp", 11101);
        this.restPort = getIntProperty("cspr.port.rest", 14101);
        this.ssePort = getIntProperty("cspr.port.sse", 18101);
    }

    public String getHostname() {
        return hostname;
    }

    public int getRcpPort() {
        return rcpPort;
    }

    public int getRestPort() {
        return restPort;
    }

    public int getSsePort() {
        return ssePort;
    }

    public String getDockerName() { return dockerName; }

    private String getProperty(final String name, final String defaultValue) {
        final String property = System.getProperty(name);
        return property != null ? property : defaultValue;
    }

    private int getIntProperty(final String name, final int defaultValue) {
        final String property = getProperty(name, null);
        return property != null ? Integer.parseInt(property) : defaultValue;
    }
}

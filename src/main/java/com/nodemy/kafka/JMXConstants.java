package com.nodemy.kafka;

public final class JMXConstants {
    public static final String PRE_URL = "service:jmx:rmi:///jndi/rmi://";
    public static final String POST_URL = "/jmxrmi";

    public static String buildJMXUrl(String host, String port) {
        return PRE_URL + host + ":" + port + POST_URL;
    }

    protected JMXConstants() {
    }
}

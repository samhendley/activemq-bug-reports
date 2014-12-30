package com.sensus.activemqtests;

import java.io.Serializable;

class TraceEvent implements Serializable {
    private final long time;
    private final String host;
    private final String event;

    public TraceEvent(String host, String event) {
        this(System.currentTimeMillis(), host, event);
    }

    public TraceEvent(long time, String host, String event) {
        this.time = time;
        this.host = host;
        this.event = event;
    }

    public long getTime() {
        return time;
    }

    public String getHost() {
        return host;
    }

    public String getEvent() {
        return event;
    }
}

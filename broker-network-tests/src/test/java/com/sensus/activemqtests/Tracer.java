package com.sensus.activemqtests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Tracer implements Serializable {
    private final byte[] buffer;
    private final int id;
    private final List<TraceEvent> events = new ArrayList<TraceEvent>();

    public Tracer(int id, int bufferSize) {
        this.id = id;
        if (bufferSize > 0) {
            buffer = new byte[bufferSize];
        } else {
            buffer = null;
        }
    }

    public int getId() {
        return id;
    }

    public void addEvent(TraceEvent event) {
        events.add(event);
    }

    public List<TraceEvent> getEvents() {
        return events;
    }
}

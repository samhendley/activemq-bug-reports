package com.sensus.activemqtests;

public class Stopwatch {
    private long startTimeNanos;

    public Stopwatch() {
        this.startTimeNanos = System.nanoTime();
    }

    public long elapsed() {
        return convertNanosToMillis(System.nanoTime() - startTimeNanos);
    }

    public long startTime() {
        return convertNanosToMillis(startTimeNanos);
    }

    private static long convertNanosToMillis(long timeInMillis) {
        return timeInMillis / 1000000L;
    }
}

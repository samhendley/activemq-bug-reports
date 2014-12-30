package com.sensus.activemqtests;

/**
 * simple class to encapsulate a wait for some delay
 */
public class Timeout {

    private final long expirationTime;

    /**
     * a delay of less than 0 will never expire
     */
    public Timeout(long timeoutDelay) {
        long now = System.currentTimeMillis();
        if (timeoutDelay >= 0) {
            this.expirationTime = now + timeoutDelay;
        } else {
            this.expirationTime = Long.MAX_VALUE;
        }
    }

    public boolean isExpired() {
        return expirationTime <= System.currentTimeMillis();
    }

    /**
     * @return how many milliseconds until the timeout is exhausted, or 0 if it is already expired
     */
    public long getRemainingTime() {
        return Math.max(expirationTime - System.currentTimeMillis(), 0);
    }
}

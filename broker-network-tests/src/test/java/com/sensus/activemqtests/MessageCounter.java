package com.sensus.activemqtests;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.atomic.AtomicLong;

class MessageCounter implements MessageListener {

    protected final String hostName;
    protected JmsTemplate responseTemplate;
    private final int consumers;
    private final AtomicLong messages = new AtomicLong();
    private final Destination destination;

    MessageCounter(String hostName, Destination destination, int consumers) {
        this.hostName = hostName;
        this.consumers = consumers;
        this.destination = destination;
    }

    public synchronized void onMessage(Message message) {
        messages.incrementAndGet();
        notifyAll();
    }

    public synchronized void await(long count, Timeout timeout) {
        try {
            while (messages.longValue() != count && !timeout.isExpired()) {
                long remainingTime = timeout.getRemainingTime();
                if (remainingTime > 0) {
                    wait(remainingTime);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (timeout.isExpired() || messages.longValue() != count) {
            throw new IllegalArgumentException("Didn't get " + count + " instead: " + messages.longValue());
        }
    }

    public String getHostName() {
        return hostName;
    }

    public void setResponseTemplate(JmsTemplate responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    public int getConsumers() {
        return consumers;
    }

    public Destination getDestination() {
        return destination;
    }
}

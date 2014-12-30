package com.sensus.activemqtests;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helper class that performs a number of jobs to make testing multiple JMS listeners straightforward.
 * 1. Provides a fast (and mostly reliable way) to wait for listeners to be ready to accept messages to avoid sleeps
 * 2. Provides an easy way to shutdown all listeners easily.
 * 3. provides simpler listener interface ExceptionThrowingMessageListener that keeps exception catching boiler plate
 * out of the test code. Any thrown exceptions are noticed when stop is called.
 */
public final class MultiListenerContainer {

    private static final int START_STOP_TIMEOUT = 10000;
    private static final int START_WAIT_TIME_INCREMENT = 10;

    private final ConnectionFactory factory;
    private final List<DefaultMessageListenerContainer> listeners = new ArrayList<DefaultMessageListenerContainer>();
    private final List<Exception> caughtExceptions = new ArrayList<Exception>();

    public MultiListenerContainer(ConnectionFactory factory) {
        this.factory = factory;
    }

    /**
     * adds and starts a listener on a particular destination
     *
     * @param listener    MessageListener class we want to actually receive the messages
     * @param destination needs to be of format queue://dest or topic://dest
     */
    public void addListener(MessageListener listener, Destination destination) {
        addListener(listener, destination, 1);
    }

    /**
     * adds and starts a listener on a particular destination
     *
     * @param listener            MessageListener class we want to actually receive the messages
     * @param destination         needs to be of format queue://dest or topic://dest
     * @param concurrentConsumers
     */
    public void addListener(MessageListener listener, Destination destination, int concurrentConsumers) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setDestination(destination);
        container.setConnectionFactory(factory);
        container.setMessageListener(listener);
        container.setBeanName("Listener-" + destination.toString());
        container.setConcurrentConsumers(concurrentConsumers);
        container.setMaxConcurrentConsumers(concurrentConsumers);
        container.afterPropertiesSet();
        container.start();
        listeners.add(container);
    }

    /**
     * wait for all of the listeners to be started and ready for incoming messages
     */
    public void start() throws Exception {
        // TODO: when we upgrade spring we can poll isRegisteredWithDestination()
        Timeout timeout = new Timeout(START_STOP_TIMEOUT);
        while (!allListenersStarted() && !timeout.isExpired()) {
            Thread.sleep(START_WAIT_TIME_INCREMENT);
        }
        // add a little extra sleeping since the flag actually gets set before
        // listener is 100% ready
        Thread.sleep(START_WAIT_TIME_INCREMENT);
        if (timeout.isExpired()) {
            throw new IllegalArgumentException("Message Listener Container not started after 10 seconds");
        }
    }

    /**
     * wait for the all of the listeners to be stopped and check that no exceptions were thrown by any of the listeners
     */
    public void stop() throws Exception {
        final CountDownLatch latch = new CountDownLatch(listeners.size());
        Runnable stopper = new Runnable() {
            public void run() {
                latch.countDown();
            }
        };
        for (DefaultMessageListenerContainer listener : listeners) {
            listener.stop(stopper);
        }
        if (!latch.await(START_STOP_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw new IllegalArgumentException("Message Listener Container not stopped after 10 seconds");
        }
        synchronized (caughtExceptions) {
            if (!caughtExceptions.isEmpty()) {
                throw new IllegalArgumentException("Message Listeners threw " + caughtExceptions.size() + " exceptions: first", caughtExceptions.get(0));
            }
        }
    }

    private boolean allListenersStarted() {
        for (DefaultMessageListenerContainer listener : listeners) {
            if (listener.getActiveConsumerCount() == 0) {
                return false;
            }
        }
        return true;
    }

    private void addCaughtException(Exception e) {
        // each listener will probably be on separate thread so we need to protect exception list
        synchronized (caughtExceptions) {
            caughtExceptions.add(e);
        }
    }

}

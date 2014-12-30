package com.sensus.activemqtests;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class EmbeddedBrokerManager {
    private final BrokerService broker;

    public EmbeddedBrokerManager(String brokerUri, boolean useJmx) throws Exception {
        this.broker = BrokerFactory.createBroker(brokerUri);
        this.broker.setUseJmx(useJmx);
    }

    public void init() throws Exception {
        this.broker.start();
        this.broker.waitUntilStarted();
    }

    public void destroy() throws Exception {
        this.broker.stop();
        this.broker.waitUntilStopped();
    }
}

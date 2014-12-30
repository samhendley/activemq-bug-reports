package com.sensus.activemqtests;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

public class JmsEnvironment {

    private final String hostName;
    private final String fileLocation;

    private EmbeddedBrokerManager broker;
    private final ActiveMQConnectionFactory connectionFactory;

    private final MultiListenerContainer listeners;

    public JmsEnvironment(String hostName, int port, String fileLocation) throws Exception {
        this.hostName = hostName;
        this.fileLocation = fileLocation;
        connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:" + port);
        connectionFactory.setUseAsyncSend(true);
        connectionFactory.setDispatchAsync(true);
        connectionFactory.setClientIDPrefix(hostName);

        listeners = new MultiListenerContainer(connectionFactory);

        init();
    }

    public void init() throws Exception {
        broker = new EmbeddedBrokerManager("xbean:file:" + fileLocation, true);
        broker.init();
    }

    public void start() throws Exception {
        listeners.start();
    }

    public void stop() throws Exception {
        listeners.stop();
    }

    public void destroy() throws Exception {
        broker.destroy();
    }

    public JmsTemplate getTemplate() {
        CachingConnectionFactory connectionFactory1 = new CachingConnectionFactory(connectionFactory);
        connectionFactory1.setSessionCacheSize(100);

        ConnectionFactory pooledFactory = connectionFactory1;
        JmsTemplate template = new JmsTemplate(pooledFactory);
        template.setTimeToLive(0);
        template.setDeliveryPersistent(false);
        return template;
    }

    public MultiListenerContainer getListeners() {
        return listeners;
    }


    public String getHostName() {
        return hostName;
    }
}

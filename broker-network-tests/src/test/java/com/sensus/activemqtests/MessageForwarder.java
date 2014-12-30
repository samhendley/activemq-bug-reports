package com.sensus.activemqtests;

import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

class MessageForwarder extends MessageCounter {

    private final Destination forwardingDestination;
    private final String event;

    public MessageForwarder(String hostName, Destination destination, int consumers, String event, Destination forwardingDestination) {
        super(hostName, destination, consumers);
        this.forwardingDestination = forwardingDestination;
        this.event = event;
    }

    @Override
    public void onMessage(final Message incoming) {
        super.onMessage(incoming);
        try {
            ObjectMessage om = (ObjectMessage) incoming;
            Tracer response = (Tracer) om.getObject();

            response.addEvent(new TraceEvent(hostName, event));

            responseTemplate.convertAndSend(forwardingDestination, response, new MessagePostProcessor() {
                public Message postProcessMessage(Message message) throws JMSException {
                    if (incoming.getJMSCorrelationID() != null) {
                        message.setJMSCorrelationID(incoming.getJMSCorrelationID());
                    }
                    return message;
                }
            });
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}

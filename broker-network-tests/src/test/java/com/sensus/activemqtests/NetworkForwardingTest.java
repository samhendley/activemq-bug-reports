package com.sensus.activemqtests;

import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;

public class NetworkForwardingTest {

    public static final int FAILURE_TIMEOUT = 120000;

    @Test
    public void testNetworkedForwardingSmallMemory() throws Exception {

        int memoryMaxMb = 10;
        testNetworkedBroker(memoryMaxMb);
    }

    @Test
    public void testNetworkedForwardingLargeMemory() throws Exception {

        int memoryMaxMb = 1000;
        testNetworkedBroker(memoryMaxMb);
    }

    private void testNetworkedBroker(int memoryMaxMb) throws Exception {
        Environments environments = new Environments(memoryMaxMb, "client", "server1", "server2");

        try {
            Integer preFetchOption = null;
            String preFetch = preFetchOption != null ? "?consumer.prefetchSize=" + preFetchOption : "";

            String forwardingQueueName = "queue://request-queue";
            Destination forwarderQueue = parseDestination(forwardingQueueName + preFetch);
            ActiveMQDestination responseQueue = parseDestination("queue://response-queue");

            JmsTemplate client = environments.makeTemplate("client");

            MessageForwarder forwarderFlip1 = new MessageForwarder("server1", forwarderQueue, 4, "FORWARD", responseQueue);
            MessageForwarder forwarderFlip2 = new MessageForwarder("server2", forwarderQueue, 4, "FORWARD", responseQueue);
            MessageCounter receiver = new MessageCounter("client", responseQueue, 1);

            environments.addListener(forwarderFlip1);
            environments.addListener(forwarderFlip2);
            environments.addListener(receiver);

            environments.start();

            Stopwatch sw = new Stopwatch();
            ActiveMQDestination requestDestination = parseDestination(forwardingQueueName);
            int numForwardedTracers = 1000000;
            for (int i = 0; i < numForwardedTracers; i++) {
                client.convertAndSend(requestDestination, new Tracer(i, 0));
                if ((i % 1000) == 0) {
                    // Thread.sleep(1);
                }
            }
            System.out.println("All " + numForwardedTracers + " enqueued in " + sw.elapsed());
            receiver.await(numForwardedTracers, new Timeout(FAILURE_TIMEOUT));
            System.out.println("All " + numForwardedTracers + " loopedback in " + sw.elapsed());

            //Thread.sleep(120000);
        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(60000);
            throw e;
        } finally {

            environments.destroy();
        }
    }

    public static ActiveMQDestination parseDestination(String destinationName) {
        try {
            return ActiveMQDestination.createDestination(destinationName, (byte) -1);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(destinationName + " is not a valid format for an activemq destination, should start with queue:// or topic://",
                    iae);
        }
    }
}

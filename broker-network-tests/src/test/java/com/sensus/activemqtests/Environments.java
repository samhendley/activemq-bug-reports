package com.sensus.activemqtests;

import org.apache.commons.io.FileUtils;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Environments {
    public static final int BASE_PORT = 61700;
    private final Map<String, JmsEnvironment> environments = new HashMap<String, JmsEnvironment>();

    public Environments(int memoryMaxMb, String... nodeNames) throws Exception {

        String templateFile = FileUtils.readFileToString(new File("src/test/resources/activemq-template.xml"));

        int basePort = BASE_PORT;
        Map<String, Integer> ports = new HashMap<String, Integer>();
        for (String node : nodeNames) {
            ports.put(node, basePort++);
        }

        for (String node : nodeNames) {
            int port = ports.get(node);
            StringBuilder otherHosts = new StringBuilder();
            for (String otherNodes : nodeNames) {
                if (!otherNodes.equals(node)) {
                    if (otherHosts.length() != 0) {
                        otherHosts.append(",");
                    }
                    otherHosts.append("tcp://localhost:").append(ports.get(otherNodes));
                }
            }
            String updated = templateFile.replaceAll("_BROKER_NAME_", node);
            updated = updated.replaceAll("_PORT_", "" + port);
            updated = updated.replaceAll("_OTHER_HOSTS_", otherHosts.toString());
            updated = updated.replaceAll("_MEMORY_MAX_", memoryMaxMb + "");

            new File("./target").mkdirs();
            String fileName = "./target/activemq-" + node + ".xml";
            new File(fileName).delete();
            FileUtils.writeStringToFile(new File(fileName), updated);
            environments.put(node, new JmsEnvironment(node, port, fileName));
        }
    }

    public void addListener(MessageCounter counter) {
        String hostName = counter.getHostName();
        JmsEnvironment jmsEnvironment = getBroker(hostName);
        counter.setResponseTemplate(jmsEnvironment.getTemplate());
        jmsEnvironment.getListeners().addListener(counter, counter.getDestination(), counter.getConsumers());
    }

    public JmsEnvironment getBroker(String hostname) {
        JmsEnvironment jmsEnvironment = environments.get(hostname);
        if (jmsEnvironment == null) {
            for (JmsEnvironment environment : environments.values()) {
                return environment;
            }
        }
        return jmsEnvironment;
    }

    public void start() throws Exception {
        for (JmsEnvironment jmsEnvironment : environments.values()) {
            jmsEnvironment.start();
        }
    }

    public void destroy() throws Exception {

        for (JmsEnvironment jmsEnvironment : environments.values()) {
            try {
                jmsEnvironment.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (JmsEnvironment jmsEnvironment : environments.values()) {
            try {
                jmsEnvironment.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JmsTemplate makeTemplate(String hostName) {
        return getBroker(hostName).getTemplate();
    }
}

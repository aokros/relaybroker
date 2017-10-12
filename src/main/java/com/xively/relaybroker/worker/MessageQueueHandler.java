package com.xively.relaybroker.worker;

import com.xively.relaybroker.broker.HttpPublisher;
import com.xively.relaybroker.client.XivelyClient;

import javax.swing.text.html.HTMLDocument;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MessageQueueHandler implements Runnable {
    private Map<String, XivelyClient> xivelyClients = null;
    private HttpPublisher httpPublisher;

    public void setXivelyClients(Map<String, XivelyClient> xivelyClients) {
        this.xivelyClients = xivelyClients;
    }

    public MessageQueueHandler(HttpPublisher httpPublisher) {
        this.httpPublisher = httpPublisher;
    }

    public void run() {
        try {
            while(!attemptToSendMessages()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean attemptToSendMessages() {
        if (xivelyClients != null && xivelyClients.size() > 0) {
            boolean allSent = false;
            try {
                String clientId;
                Iterator it = xivelyClients.keySet().iterator();
                while (it.hasNext()) {
                    clientId = (String) it.next();
                    XivelyClient xivelyClient = (XivelyClient) xivelyClients.get(clientId);
                    System.out.println("Processing messages for Client=" + clientId);
                    if (xivelyClient != null) {
                        while (xivelyClient.hasQueuedMessage()) {
                            boolean messageSent = httpPublisher.publishMessage(xivelyClient.getUsername(), xivelyClient.getPassword(), xivelyClient.getOldestMessage());
                            if (messageSent) {
                                xivelyClient.oldestMessageAcked();
                            } else {
                                break;
                            }
                        }
                        if (!xivelyClient.hasQueuedMessage()) {
                            allSent = true;
                        } else {
                            allSent = false;
                        }
                    }
                }
                return allSent;
            } catch (Exception e) {
                System.err.println("An error occured while consuming queues: " + e.getMessage());
                System.err.println(e.getClass());
                e.printStackTrace(System.err);
                System.err.println(e.getCause());
            }
        }
        return true;
    }
}

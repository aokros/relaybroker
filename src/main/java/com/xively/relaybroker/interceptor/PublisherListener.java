package com.xively.relaybroker.interceptor;

import com.xively.relaybroker.client.XivelyClient;
import com.xively.relaybroker.worker.TaskThread;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;

import java.util.HashMap;
import java.util.Map;

public class PublisherListener extends AbstractInterceptHandler {

    private TaskThread taskThread;
    private Runnable task;

    public PublisherListener(TaskThread taskThread, Runnable task) {
        super();
        this.taskThread = taskThread;
        this.task = task;
    }

    private Map<String, XivelyClient> xivelyClients = new HashMap<>();

    public Map<String, XivelyClient> getXivelyClients() {
        return xivelyClients;
    }

    @Override
    public String getID() {
        return "EmbeddedLauncherPublishListener";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        if (xivelyClients.containsKey(msg.getClientID())) {
            xivelyClients.get(msg.getClientID()).putMessageToQueue(msg);
            try {
                taskThread.run(this.task);
            } catch (IllegalStateException e) {
                System.out.println(e.toString());
            }
        }
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        if (msg.isPasswordFlag() && msg.isUserFlag()) {
            if (!xivelyClients.containsKey(msg.getClientID())) {
                XivelyClient newClient = new XivelyClient(msg.getUsername(), msg.getPassword());
                xivelyClients.put(msg.getClientID(), newClient);
            }
        }
    }
}

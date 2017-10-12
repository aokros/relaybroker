package com.xively.relaybroker;

import com.xively.relaybroker.broker.HttpPublisher;
import com.xively.relaybroker.interceptor.PublisherListener;
import com.xively.relaybroker.worker.MessageQueueHandler;
import com.xively.relaybroker.worker.TaskThread;
import io.moquette.server.Server;

import java.io.IOException;
import java.net.URISyntaxException;

public class RelayBroker {

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        final Server mqttBroker = new Server();
        TaskThread taskThread = new TaskThread();

        final HttpPublisher httpPublisher = new HttpPublisher();
        final MessageQueueHandler messageQueueHandler = new MessageQueueHandler(httpPublisher);
        final PublisherListener publisherListener = new PublisherListener(taskThread, messageQueueHandler);
        messageQueueHandler.setXivelyClients(publisherListener.getXivelyClients());

        mqttBroker.startServer();
        taskThread.start();
        mqttBroker.addInterceptHandler(publisherListener);
        taskThread.run();

        System.out.println("Broker started press [CTRL+C] to stop");
        //Bind  a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping broker");
            mqttBroker.stopServer();
            System.out.println("Broker stopped");

            System.out.println("Stopping worker");
            taskThread.stopXivelyPublisher();
            System.out.println("Stopped worker");
        }));
    }
}

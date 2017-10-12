package com.xively.relaybroker.client;

import io.moquette.interception.messages.InterceptPublishMessage;

import java.util.ArrayList;
import java.util.List;

public class XivelyClient {
    private byte[] password;
    private String username;
    private List<InterceptPublishMessage> messageQueue = new ArrayList<>();

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<InterceptPublishMessage> getMessageQueue() {
        return messageQueue;
    }

    public boolean hasQueuedMessage() {
        if (messageQueue.size() > 0) return true;
        else return false;
    }

    public InterceptPublishMessage getOldestMessage() {
        return messageQueue.get(0);
    }

    public void putMessageToQueue(InterceptPublishMessage msg) {
        messageQueue.add(msg);
    }

    public void oldestMessageAcked() {
        messageQueue.remove(0);
    }

    public XivelyClient(String username, byte[] password) {
        this.setPassword(password);
        this.setUsername(username);
    }
}

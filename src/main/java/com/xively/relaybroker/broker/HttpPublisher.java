package com.xively.relaybroker.broker;

import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpPublisher {
    private URI cloudBrokerAddress = null;

    public HttpPublisher() throws URISyntaxException {
        this.cloudBrokerAddress = new URI("https://broker.xively.eu/");
    }

    public boolean publishMessage(String username, byte[] password, InterceptPublishMessage message) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        String stringPassword = new String(password);
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, stringPassword)
        );

        AuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(cloudBrokerAddress.getHost(), cloudBrokerAddress.getPort(), cloudBrokerAddress.getScheme()), new BasicScheme());

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(2000)
                .build();
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        try {
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setHost(cloudBrokerAddress.getHost());
            uriBuilder.setScheme(cloudBrokerAddress.getScheme());
            uriBuilder.setPath("/messaging/publish/" + message.getTopicName());
            uriBuilder.addParameter("qos", "" + message.getQos().value());
            uriBuilder.addParameter("retain", message.isRetainFlag() ? "true" : "false");

            HttpPost httpPost = new HttpPost(uriBuilder.build());
            ByteBuf payload = message.getPayload();
            byte[] postPayloadBytes = new byte[payload.capacity()];
            payload.getBytes(0, postPayloadBytes);
            httpPost.setEntity(new ByteArrayEntity(postPayloadBytes));
            System.out.println("Executing request " + httpPost.getRequestLine());
            System.out.println("Body: \n" + httpPost.getEntity().getContent().toString());
            CloseableHttpResponse response = httpclient.execute(httpPost, context);
            System.out.println(username + ":" + stringPassword);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Responded with statusCode=" + statusCode);
                return true;
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.err.println(e.getStackTrace());
            } finally {
                response.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        } finally {
            httpclient.close();
        }
        return false;
    }
}

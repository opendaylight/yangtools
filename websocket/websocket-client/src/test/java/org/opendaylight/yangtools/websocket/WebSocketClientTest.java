/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.websocket;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.websocket.client.WebSocketIClient;
import org.opendaylight.yangtools.websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class WebSocketClientTest {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientTest.class.toString());
    private static final String MESSAGE = "Take me to your leader!";
    private Thread webSocketServerThread;

    /**
     * Tracks if the message from the server has been received
     */
    private final SettableFuture<Boolean> messageReceived = SettableFuture.create();

    /**
     * Tracks the port on which the server is listening
     */
    private int port = 0;

    @Before
    public void startWebSocketServer(){
        try {
            WebSocketServer webSocketServer = new WebSocketServer(0);
            webSocketServerThread = new Thread(webSocketServer);
            webSocketServerThread.setDaemon(false);
            webSocketServerThread.start();
            port = webSocketServer.getPort().get();
        } catch (Exception e) {
            logger.trace("Error starting websocket server");
        }
    }

    @Test
    public void connectAndSendData(){

        URI uri = null;
        try {
            uri = new URI(String.format("ws://localhost:%d/websocket", port));
            logger.info("CLIENT: " + uri);
            ClientMessageCallback messageCallback = new ClientMessageCallback();
            WebSocketIClient wsClient = new WebSocketIClient(uri,messageCallback);
            try {
                wsClient.connect();
                wsClient.writeAndFlush(MESSAGE);
                wsClient.writeAndFlush(new CloseWebSocketFrame());

                /*
                 * Wait for up to 5 seconds for the message to be received. If
                 * after that time, the message has not been received then
                 * consider this a failed test.
                 */
                messageReceived.get(5, TimeUnit.SECONDS);

                webSocketServerThread.interrupt();
            } catch (InterruptedException e) {
                logger.info("WebSocket client couldn't connect to : " + uri);
                Assert.fail("WebSocker client could not connect to : " + uri);
            } catch (ExecutionException | TimeoutException toe) {
                logger.info("Message not received");
                Assert.fail(toe.toString());
            }
        } catch (URISyntaxException e) {
            logger.info("There is an error in URL sytnax {}",e);
            Assert.fail("There is an error in URL sytnax");
        }
    }

    private class ClientMessageCallback implements org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback {
        @Override
        public void onMessageReceived(final Object message) {
            logger.info("received message {}",message);
            messageReceived.set(true);
        }
    }
}

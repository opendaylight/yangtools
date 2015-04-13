/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamReplay;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.to.RestRpcResult;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.websocket.client.WebSocketIClient;
import org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.ClientResponse;




public class RestListenableEventStreamContext<L extends NotificationListener> implements ListenableEventStreamContext,ClientMessageCallback {

    private static final Logger logger = LoggerFactory.getLogger(RestListenableEventStreamContext.class.toString());
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private WebSocketIClient wsClient;
    private Method listenerCallbackMethod;
    private final RestconfClientImpl restconfClient;
    private final EventStreamInfo streamInfo;
    private static final int STATUS_OK = 200;
    
    public RestListenableEventStreamContext(final EventStreamInfo streamInfo,final RestconfClientImpl restconfClient){
        this.restconfClient = restconfClient;
        this.streamInfo = streamInfo;
    }

    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(final T listener) {

        for (Method m:listener.getClass().getDeclaredMethods()){
            if (BindingReflections.isNotificationCallback(m)){
                this.listenerCallbackMethod = m;
                break;
            }
        }
        return new AbstractListenerRegistration<T>(listener) {
            @Override
            protected void removeRegistration() {
                stopListening();
            }
        };
    }

    @Override
    public ListenableFuture<RpcResult<Void>> startListening() {


        ClientResponse response = null;
        try {
            response = extractWebSocketUriFromRpc(this.streamInfo.getIdentifier());
        } catch (ExecutionException e) {
            logger.trace("Execution exception while extracting stream name {}",e);
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            logger.trace("InterruptedException while extracting stream name {}",e);
            throw new IllegalStateException(e);
        } catch (UnsupportedEncodingException e) {
            logger.trace("UnsupportedEncodingException while extracting stream name {}",e);
            throw new IllegalStateException(e);
        }
        boolean success = true;
        if (response.getStatus() != STATUS_OK) {
            success = false;
        }

        final RestRpcResult rpcResult = new RestRpcResult(success,response.getLocation());
        createWebsocketClient(response.getLocation());

        ListenableFuture<RpcResult<Void>> future = pool.submit(new Callable<RpcResult<Void>>() {
            @Override
            public RpcResult<Void> call() {
                return rpcResult;
            }
        });

        return future;
    }

    @Override
    public ListenableFuture<RpcResult<Void>> startListeningWithReplay(final Optional<Date> startTime, final Optional<Date> endTime) {
        //TODO RESTCONF doesn't provide this functionality
        return null;
    }

    @Override
    public void stopListening() {
        this.wsClient.writeAndFlush(new CloseWebSocketFrame(42,this.streamInfo.getIdentifier()));
    }

    @Override
    public ListenableFuture<Optional<EventStreamReplay>> getReplay(final Optional<Date> startTime, final Optional<Date> endTime) {
        //TODO RESTCONF doesn't provide this functionality
        return null;
    }

    @Override
    public void close() {
        this.stopListening();
    }

    private ClientResponse extractWebSocketUriFromRpc(final String methodName) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        ListenableFuture<ClientResponse> clientFuture = restconfClient.get(ResourceUri.STREAM.getPath()+"/"+encodeUri(this.streamInfo.getIdentifier()),MediaType.APPLICATION_XML,new Function<ClientResponse, ClientResponse>(){

            @Override
            public ClientResponse apply(final ClientResponse clientResponse) {
                return clientResponse;
            }
        });

        return clientFuture.get();
    }
    private void createWebsocketClient(final URI websocketServerUri){
        this.wsClient = new WebSocketIClient(websocketServerUri,this);
    }
    private String encodeUri(final String encodedPart) throws UnsupportedEncodingException {
        return URI.create(URLEncoder.encode(encodedPart, Charsets.US_ASCII.name()).toString()).toASCIIString();
    }

    @Override
    public void onMessageReceived(final Object message) {
        if (null == this.listenerCallbackMethod){
            throw new IllegalStateException("No listener method to invoke.");
        }
        try {
            this.listenerCallbackMethod.invoke(message);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to invoke callback", e);
        }
    }

}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.restconf.client.BindingToRestRpc;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamReplay;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.websocket.client.WebSocketIClient;
import org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;



@XmlRootElement
public class RestListenableEventStreamContext<T extends NotificationListener> implements ListenableEventStreamContext,ClientMessageCallback {

    private final URI defaultUri;
    private final Client client;

    private static final Logger logger = LoggerFactory.getLogger(RestListenableEventStreamContext.class.toString());
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    private URI websocketServerUri;
    private WebSocketIClient wsClient;
    private String streamName;

    private Method listenerCallbackMethod;

    public RestListenableEventStreamContext(URI uri){
        this.defaultUri = uri;
        ClientConfig config = new DefaultClientConfig();
        client  = Client.create(config);
    }
    @Override
    public <L extends NotificationListener> ListenerRegistration<L> registerNotificationListener(L listener) {

        try {
            this.streamName = BindingReflections.getModuleInfo(listener.getClass()).getName();
        } catch (Exception e) {
            logger.trace("Error resolving stream name form listener class.");
            throw new IllegalStateException("Error resolving stream name form listener class.");
        }

        for (Method m:listener.getClass().getDeclaredMethods()){
            if (BindingReflections.isNotificationCallback(m)){
                this.listenerCallbackMethod = m;
                break;
            }
        }

        final L listenerProxy = (L) BindingToRestRpc.getProxy(listener.getClass(), this.defaultUri);
        return new AbstractListenerRegistration<L>(listenerProxy) {
            @Override
            protected void removeRegistration() {
                // FIXME: implement this method
            }
        };
    }

    @Override
    public ListenableFuture<RpcResult<Void>> startListening() {


        ClientResponse response = extractWebSocketUriFromRpc(this.streamName);
        boolean success = true;
        if (response.getStatus() != 200) {
            success = false;
        }

        final RestRpcResult rpcResult = new RestRpcResult(success,response.getLocation());
        createWebsocketClient(response.getLocation());

        ListenableFuture<RpcResult<Void>> future = pool.submit(new Callable<RpcResult<Void>>() {
            @Override
            public RpcResult<Void> call() throws Exception {
                return rpcResult;
            }
        });

        return future;
    }

    @Override
    public ListenableFuture<RpcResult<Void>> startListeningWithReplay(Optional<Date> startTime, Optional<Date> endTime) {
        //TODO RESTCONF doesn't provide this functionality
        return null;
    }

    @Override
    public void stopListening() {
        this.wsClient.writeAndFlush(new CloseWebSocketFrame(42,this.streamName));
    }

    @Override
    public ListenableFuture<Optional<EventStreamReplay>> getReplay(Optional<Date> startTime, Optional<Date> endTime) {
        //TODO RESTCONF doesn't provide this functionality
        return null;
    }

    @Override
    public void close() {
        this.stopListening();
    }

    private ClientResponse extractWebSocketUriFromRpc(String streamName){
        String uri = null;
        try {
            uri = this.createUri(defaultUri+"/streams/stream/", streamName);
        } catch (UnsupportedEncodingException e) {
            logger.trace("Unsupported encoding.");
        }

        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.STREAMS.getPath());
        final ClientResponse response = resource.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        this.websocketServerUri = response.getLocation();
        return response;
    }
    private void createWebsocketClient(URI websocketServerUri){
        this.wsClient = new WebSocketIClient(websocketServerUri,this);
    }
    private String getRpcInput(String path,String ns) {
        StringBuilder sb = new StringBuilder();
        sb.append("<input xmlns=\"urn:opendaylight:params:xml:ns:yang:controller:md:sal:remote\">");
        sb.append("<path xmlns:int=\""+ns+"\">"+path+"</path>");
        sb.append("</input>");
        return sb.toString();
    }

    private String createUri(String prefix, String encodedPart) throws UnsupportedEncodingException {
        return URI.create(prefix + URLEncoder.encode(encodedPart, Charsets.US_ASCII.name()).toString()).toASCIIString();
    }

    @Override
    public void onMessageReceived(Object message) {
        if (null == this.listenerCallbackMethod){
            throw new IllegalStateException("No listener method to invoke.");
        }
        try {
            this.listenerCallbackMethod.invoke(message);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}

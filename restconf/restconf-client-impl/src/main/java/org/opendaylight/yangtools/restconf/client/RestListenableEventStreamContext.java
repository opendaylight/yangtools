/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.ClientResponse;
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
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestListenableEventStreamContext<T extends NotificationListener> implements ListenableEventStreamContext,ClientMessageCallback {

    private static final Logger logger = LoggerFactory.getLogger(RestListenableEventStreamContext.class.toString());
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private WebSocketIClient wsClient;
    private Method listenerCallbackMethod;
    private final BindingIndependentMappingService mappingService;
    private final SchemaContext schemaContext;
    private final RestconfClientImpl restconfClient;
    private final EventStreamInfo streamInfo;

    public RestListenableEventStreamContext(EventStreamInfo streamInfo,BindingIndependentMappingService mappingService,RestconfClientImpl restconfClient,SchemaContext schemaContext){
        this.schemaContext = schemaContext;
        this.restconfClient = restconfClient;
        this.mappingService = mappingService;
        this.streamInfo = streamInfo;
    }
    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(T listener) {

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
        this.wsClient.writeAndFlush(new CloseWebSocketFrame(42,this.streamInfo.getIdentifier()));
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

    private ClientResponse extractWebSocketUriFromRpc(String methodName) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        ListenableFuture<ClientResponse> clientFuture = restconfClient.get(ResourceUri.STREAM.getPath()+"/"+encodeUri(this.streamInfo.getIdentifier()),MediaType.APPLICATION_XML,new Function<ClientResponse, ClientResponse>(){

            @Override
            public ClientResponse apply(ClientResponse clientResponse) {
                return clientResponse;
            }
        });
        while (!clientFuture.isDone()){
            //noop
        }
        return clientFuture.get();
    }

/*
    private String subscribeAndReturnStreamName(String streamName) throws UnsupportedEncodingException, ExecutionException, InterruptedException {
        ListenableFuture<String> streamNameInFuture = restconfClient.get(ResourceUri.STREAM.getPath()+"/"+encodeUri(this.streamInfo.getIdentifier()),MediaType.APPLICATION_XML,new Function<ClientResponse,String>(){
            @Override
            public String apply(ClientResponse clientResponse) {
                Element rootElement = null;
                try {
                    Document responseDocument = XmlTools.fromXml(clientResponse.getEntityInputStream());
                    rootElement = responseDocument.getDocumentElement();
                } catch (Exception e) {
                    logger.trace("Error occurred while parsing client response {}",e);
                    throw new IllegalStateException(e);
                }
                NodeList w3cNodes = rootElement.getElementsByTagName("stream-name");
                if (w3cNodes.getLength()<=0){
                    throw new IllegalStateException("No tag stream-name in response.");
                }
                return w3cNodes.item(0).getNodeValue();
            }
        });
        while (!streamNameInFuture.isDone()){
            //noop
        }
        return streamNameInFuture.get();
    }
*/
    private void createWebsocketClient(URI websocketServerUri){
        this.wsClient = new WebSocketIClient(websocketServerUri,this);
    }

    private String encodeUri(String encodedPart) throws UnsupportedEncodingException {
        return URI.create(URLEncoder.encode(encodedPart, Charsets.US_ASCII.name()).toString()).toASCIIString();
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

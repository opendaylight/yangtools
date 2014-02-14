/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import java.io.IOException;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


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
    private final BindingIndependentMappingService mappingService;

    public RestListenableEventStreamContext(URI uri, BindingIndependentMappingService mappingService){
        this.defaultUri = uri;
        ClientConfig config = new DefaultClientConfig();
        client  = Client.create(config);
        this.mappingService = mappingService;
    }
    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(T listener) {

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

        Class targetIfc = null;
        if (!listener.getClass().isInterface()){
            if (listener.getClass().getInterfaces().length>0){
                targetIfc = listener.getClass().getInterfaces()[0];
            }
        } else {
            targetIfc = listener.getClass();
        }
        T listenerProxy = (T) BindingToRestRpc.getProxy(targetIfc, this.defaultUri,this.mappingService);
        ListenerRegistration listenerRegistration = new ListenerRegistrationImpl(listenerProxy);
        return listenerRegistration;
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

    private ClientResponse extractWebSocketUriFromRpc(String methodName){
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.STREAMS.getPath()+"/"+subscribeAndReturnStreamName(this.streamName));
        final ClientResponse responseWithWebsocketUrl = resource.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        this.websocketServerUri = responseWithWebsocketUrl.getLocation();
        return responseWithWebsocketUrl;
    }

    private String subscribeAndReturnStreamName(String streamName){
        String uri = null;
        try {
            uri = this.createUri(defaultUri+ResourceUri.STREAMS.toString(), this.streamName);
        } catch (UnsupportedEncodingException e) {
            logger.trace("Unsupported encoding.");
        }

        WebResource resource = client.resource(uri);
        final ClientResponse response = resource.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        DocumentBuilderFactory documentBuilder = DocumentBuilderFactory.newInstance();
        documentBuilder.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = documentBuilder.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        Document doc = null;
        try {
            doc = builder.parse(response.getEntityInputStream());
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Element rootElement = doc.getDocumentElement();
        NodeList w3cNodes = rootElement.getElementsByTagName("stream-name");
        if (w3cNodes.getLength()<=0){
            throw new IllegalStateException("No tag stream-name in response.");
        }
        return w3cNodes.item(0).getNodeValue();
    }
    private void createWebsocketClient(URI websocketServerUri){
        this.wsClient = new WebSocketIClient(websocketServerUri,this);
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

    private class ListenerRegistrationImpl<T extends NotificationListener> implements ListenerRegistration {

        private final T listener;

        public ListenerRegistrationImpl(T registeredListener){
            this.listener =   registeredListener;

        }
        @Override
        public Object getInstance() {
            return listener;
        }

        @Override
        public void close() throws Exception {
        }
    }
}

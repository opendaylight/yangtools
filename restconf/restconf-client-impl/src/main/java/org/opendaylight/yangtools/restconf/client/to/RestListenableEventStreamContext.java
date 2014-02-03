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
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.restconf.client.BindingToRestRpc;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamReplay;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.listener.ListenerRegistrationImpl;
import org.opendaylight.yangtools.websocket.client.WebSocketIClient;
import org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@XmlRootElement
public class RestListenableEventStreamContext implements ListenableEventStreamContext {

    private final URI defaultUri;
    private final EventStreamInfo streamInfo;
    private final Client client;
    private final WebTarget target;
    private static final Logger logger = LoggerFactory.getLogger(RestListenableEventStreamContext.class.toString());
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private String streamName;
    private URI websocketServerUri;
    private WebSocketIClient wsClient;

    public RestListenableEventStreamContext(URI uri, EventStreamInfo eventStreamInfo){
        this.defaultUri = uri;
        this.streamInfo = eventStreamInfo;
        ClientConfig config = new DefaultClientConfig();
        client  = Client.create(config);
        target = ClientFactory.newClient().target(this.defaultUri);
        this.extractStreamName();
    }
    @Override
    public <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(T listener) {
        T listenerProxy = (T) BindingToRestRpc.getProxy(listener.getClass(), this.defaultUri);
        ListenerRegistration listenerRegistration = new ListenerRegistrationImpl(listenerProxy);
        return listenerRegistration;
    }

    @Override
    public ListenableFuture<RpcResult<Void>> startListening() {

        final RestRpcResult rpcResult = new RestRpcResult();

        Response response = extractWebSocketUriFromRpc();

        if (response.getStatus() != 200) {
            rpcResult.setSucceeded(false);
        }

        //createWebsocketClient();

        rpcResult.setSucceeded(true);
        rpcResult.setResult(response.getLocation());

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
        wsClient.writeAndFlush(new CloseWebSocketFrame());
    }

    @Override
    public ListenableFuture<Optional<EventStreamReplay>> getReplay(Optional<Date> startTime, Optional<Date> endTime) {
        //TODO RESTCONF doesn't provide this functionality
        return null;
    }

    @Override
    public void close() {

    }

    private Response extractWebSocketUriFromRpc(){
        String uri = null;
        try {
            uri = this.createUri(defaultUri+"/streams/stream/", streamName);
        } catch (UnsupportedEncodingException e) {
            logger.trace("Unsupported encoding.");
        }
        WebTarget target = ClientFactory.newClient().target(uri);
        target = ClientFactory.newClient().target(uri);
        Response responseWithRedirectionUri = target.request(MediaType.APPLICATION_XML).get();
        this.websocketServerUri = responseWithRedirectionUri.getLocation();
        return responseWithRedirectionUri;
    }
    private void createWebsocketClient(){
        this.wsClient = new WebSocketIClient(this.websocketServerUri, new ClientMessageCallback() {
            @Override
            public void onMessageReceived(Object message) {

            }
        });
    }
    private String getRpcInput(String path,String ns) {
        StringBuilder sb = new StringBuilder();
        sb.append("<input xmlns=\"urn:opendaylight:params:xml:ns:yang:controller:md:sal:remote\">");
        sb.append("<path xmlns:int=\""+ns+"\">"+path+"</path>");
        sb.append("</input>");
        return sb.toString();
    }

    private void extractStreamName(){
        String uri = null;
        try {
            uri = this.createUri(defaultUri+"/operations/", streamInfo.getIdentifier());
        } catch (UnsupportedEncodingException e) {
            logger.trace("Unsupported encoding.");
        }

        List<String> pathAndNamespacePlaceholder = Arrays.asList(streamInfo.getIdentifier().split(","));

        WebTarget target = ClientFactory.newClient().target(uri);
        final Response responseWithStreamName =  target.request(MediaType.APPLICATION_XML).
                post(Entity.entity(
                        getRpcInput(
                                pathAndNamespacePlaceholder.get(0),
                                pathAndNamespacePlaceholder.get(1)
                        ),                        MediaType.APPLICATION_XML
                )
                );

        String xmlResponse = responseWithStreamName.readEntity(String.class);

        this.streamName = xmlResponse.replace("<stream-name>","");
        this.streamName = streamName.replace("</stream-name>","");
    }

    private String createUri(String prefix, String encodedPart) throws UnsupportedEncodingException {
        return URI.create(prefix + URLEncoder.encode(encodedPart, Charsets.US_ASCII.name()).toString()).toASCIIString();
    }
}

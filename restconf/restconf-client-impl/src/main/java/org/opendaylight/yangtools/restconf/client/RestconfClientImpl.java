/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientFactory;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.opendaylight.yangtools.RestRestconfService;
import org.opendaylight.yangtools.XmlToNodes;
import org.opendaylight.yangtools.XmlTools;
import org.opendaylight.yangtools.draft.Draft02;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.api.event.RestModule;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.restconf.client.service.ConfigurationDataStoreImpl;
import org.opendaylight.yangtools.restconf.client.service.OperationalDataStoreImpl;
import org.opendaylight.yangtools.restconf.client.to.RestListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.to.RestRpcServiceContext;
import org.opendaylight.yangtools.restconf.common.ResourceMediaTypes;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImpl implements RestconfClientContext, RestRestconfService {




    private final URI defaultUri;

    private final Client client;
    private final RestRestconfService restconfService;

    private final BindingIndependentMappingService mappingService;
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    private Logger logger = LoggerFactory.getLogger(RestconfClientImpl.class.toString());


    public RestconfClientImpl(URL url){
        ClientConfig config = new DefaultClientConfig();
        client  = Client.create(config);
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            logger.trace("Error in URI syntax {}",e.getMessage());
        }
        this.defaultUri = uri;

        WebTarget target = ClientFactory.newClient().target(this.defaultUri);
        this.restconfService = WebResourceFactory.newResource(RestRestconfService.class, target);

        //TODO get mapping service instance
        mappingService = new RuntimeGeneratedMappingServiceImpl();
    }

    public ListenableFuture<Set<Class<? extends RpcService>>> getRpcServices() {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
        final ClientResponse response = resource.accept(ResourceMediaTypes.xml.getMediaType())
                .get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        List<RestModule> _modules = null;
        try {
            _modules = XmlTools.getModulesFromInputStream(response.getEntityInputStream());
        } catch (Exception e) {
            throw new IllegalStateException("Error parsing modules from received XML.");
        }
        final List<RestModule> modules = _modules;
        ListenableFuture<Set<Class<? extends RpcService>>> future = pool.submit(new Callable<Set<Class<? extends RpcService>>>() {
            @Override
            public Set<Class<? extends RpcService>> call() throws Exception {
                Set<Class<? extends RpcService>> rpcClasses = new HashSet<Class<? extends RpcService>>();
                Set<RpcService> rpcNamespaces =  XmlTools.fromInputStream(response.getEntityInputStream());
                for (RestModule module:modules){
                    Optional<Class<? extends RpcService>> optionalRpcService = mappingService.getRpcServiceClassFor(module.getNamespace(), module.getRevision());
                    rpcClasses.add(optionalRpcService.get());
                }
                return rpcClasses;
            }
        });
        return future;
    }

    @Override
    public <T extends RpcService> RpcServiceContext<T> getRpcServiceContext(Class<T> rpcService) {
        RestRpcServiceContext restRpcServiceContext = new RestRpcServiceContext(rpcService,this.defaultUri);
        return restRpcServiceContext;
    }

    @Override
    public ListenableFuture<Set<EventStreamInfo>> getAvailableEventStreams() {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.STREAMS.getPath());
        final ClientResponse response = resource.accept(ResourceMediaTypes.xml.getMediaType())
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        ListenableFuture<Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo>> future = pool.submit(new Callable<Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo>>() {
            @Override
            public Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo> call() throws Exception {
                return XmlTools.evenStreamsFromInputStream(response.getEntityInputStream());
            }
        });
        return future;
    }

    @Override
    public ListenableEventStreamContext getEventStreamContext(EventStreamInfo info) {
        RestListenableEventStreamContext listenableEventStream = new RestListenableEventStreamContext(defaultUri);
        return listenableEventStream;
    }

    @Override
    public ConfigurationDatastore getConfigurationDatastore() {
        return new ConfigurationDataStoreImpl(mappingService,this.defaultUri);
    }

    @Override
    public OperationalDatastore getOperationalDatastore() {
        return new OperationalDataStoreImpl(mappingService,this.defaultUri);
    }

    @Override
    public void close() {

    }

    @Override
    public Object getRoot() {
        return restconfService.getRoot();
    }

    public String getModules() {
        return restconfService.getModules();
    }

    @Override
    public String invokeRpc(@PathParam("identifier") String identifier, @QueryParam("input") String payload) {
        return restconfService.invokeRpc(
                identifier,
                payload);
    }

    @Override
    public String invokeRpc(@PathParam("identifier") String identifier) {
        return restconfService.invokeRpc(identifier);
    }

    @Override
    public String readConfigurationData(@PathParam("identifier") String identifier) {
        return restconfService.readConfigurationData(identifier);
    }

    @Override
    public String readOperationalData(@PathParam("identifier") String identifier) {
        return restconfService.readOperationalData(identifier);
    }



    @Override
    public Response updateConfigurationData(@PathParam("identifier") String identifier, @QueryParam("input")  String payload) {
        return restconfService.updateConfigurationData(identifier,payload);
    }

    @Override
    public Response createConfigurationData(@PathParam("identifier") String identifier, @QueryParam("input")  String payload) {
        return restconfService.createConfigurationData(identifier,payload);
    }

    @Override
    public Response createConfigurationData( @QueryParam("input") String payload) {
        return restconfService.createConfigurationData(payload);
    }

    @PUT
    @Path("/config/{identifier:.+}")
    @Consumes({Draft02.MediaTypes.DATA+JSON, Draft02.MediaTypes.DATA+XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response updateConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.xml.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }

    public Response createConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.xml.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }

    @Override
    public Response deleteConfigurationData(@PathParam("identifier") String identifier) {
        return restconfService.deleteConfigurationData(identifier);
    }


}

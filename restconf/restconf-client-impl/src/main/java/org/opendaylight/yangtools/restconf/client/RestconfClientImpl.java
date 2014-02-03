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
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.XmlTools;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.dto.RestEventStreamInfo;
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
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImpl implements RestconfClientContext { //, RestRestconfService {




    private final URI defaultUri;

    private final Client client;

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
            logger.trace("Error in URI syntax {}",e.getMessage(),e);
        }
        this.defaultUri = uri;

        //TODO set schemaContext to mapping service
        mappingService = new RuntimeGeneratedMappingServiceImpl();
    }

    public ListenableFuture<Set<Class<? extends RpcService>>> getRpcServices() {
        ListenableFuture<Set<Class<? extends RpcService>>> future = pool.submit(new Callable<Set<Class<? extends RpcService>>>() {
            @Override
            public Set<Class<? extends RpcService>> call() throws Exception {
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .get(ClientResponse.class);
                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }

                StringWriter writer = new StringWriter();
                IOUtils.copy(response.getEntityInputStream(), writer, "UTF-8");
                String theString = writer.toString();

                List<RestModule> modules = XmlTools.getModulesFromInputStream(new ByteArrayInputStream(theString.getBytes("UTF-8")));

                Set<Class<? extends RpcService>> rpcClasses = new HashSet<Class<? extends RpcService>>();
                Set<RpcService> rpcNamespaces =  XmlTools.fromInputStream(new ByteArrayInputStream(theString.getBytes("UTF-8")));

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
        ListenableFuture<Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo>> future = pool.submit(new Callable<Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo>>() {
            @Override
            public Set<org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo> call() throws Exception {
                // when restconf will support discovery by /restconf/streams change ResourceUri.MODULES to ResourceUri.STREAMS
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .get(ClientResponse.class);

                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }
                List<RestModule> modules = XmlTools.getModulesFromInputStream(response.getEntityInputStream());
                // when restconf will support discovery by /restconf/streams use this  instead of next iteration
                //return XmlTools.evenStreamsFromInputStream(response.getEntityInputStream());
                Set<EventStreamInfo> evtStreamInfos = new HashSet<EventStreamInfo>();
                for (RestModule module:modules){
                    RestEventStreamInfo esi = new RestEventStreamInfo();
                    esi.setIdentifier(module.getName());
                    esi.setDescription(module.getNamespace()+" "+module.getRevision());
                    evtStreamInfos.add(esi);
                }
                return evtStreamInfos;
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

/*
    @Override
    public Object getRoot() {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.RESTCONF.getPath());
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class);
        return response.getEntity();
    }
    public String getModules() {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class);
        return response.getEntity().toString();
    }
    @Override
    public String invokeRpc(@PathParam("identifier") String identifier, @QueryParam("input") CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.OPERATIONS.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));
        return response.getEntity().toString();
    }
    @Override
    public String invokeRpc(@PathParam("identifier") String identifier) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.OPERATIONS.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class);
        return response.getEntity().toString();
    }
    @Override
    public String readConfigurationData(@PathParam("identifier") String identifier) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).get(Response.class);
        return response.getEntity().toString();
    }
    @Override
    public String readOperationalData(@PathParam("identifier") String identifier) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.OPERATIONAL.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).get(Response.class);
        return response.getEntity().toString();
    }
    @Override
    public Response createConfigurationData( @QueryParam("input") CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath());
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }
    @PUT
    @Path("/config/{identifier:.+}")
    @Consumes({Draft02.MediaTypes.DATA+RestRestconfService.JSON, Draft02.MediaTypes.DATA+RestRestconfService.XML,
            MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response updateConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }

    public Response createConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).post(Response.class, XmlToNodes.compositeNodeToXml(payload));

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }

    @Override
    public Response deleteConfigurationData(@PathParam("identifier") String identifier) {
        WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+identifier);
        Response response = resource.accept(ResourceMediaTypes.XML.getMediaType()).delete(Response.class);

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        return response;
    }
*/


}

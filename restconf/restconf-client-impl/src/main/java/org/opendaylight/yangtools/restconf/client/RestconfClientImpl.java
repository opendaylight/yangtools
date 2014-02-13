/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.auth.AuthenticationHolder;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.dto.RestEventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.dto.RestModule;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.restconf.client.to.RestListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.to.RestRpcServiceContext;
import org.opendaylight.yangtools.restconf.common.ResourceMediaTypes;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.restconf.utils.RestconfUtils;
import org.opendaylight.yangtools.restconf.utils.XmlTools;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImpl implements RestconfClientContext, SchemaContextListener {

    private final URI defaultUri;

    private final Client restClient;

    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    private final Logger logger = LoggerFactory.getLogger(RestconfClientImpl.class.toString());

    private final SchemaContextHolder schemaContextHolder;

    private final BindingIndependentMappingService mappingService;

    private OperationalDataStoreImpl operationalDatastoreAccessor;
    private ConfigurationDataStoreImpl configurationDatastoreAccessor;

    public RestconfClientImpl(URL url,BindingIndependentMappingService mappingService,
                              SchemaContextHolder schemaContextHolder,
                              AuthenticationHolder authHolder){
        Preconditions.checkArgument(url != null,"Restconf endpoint URL must be supplied.");
        Preconditions.checkArgument(mappingService != null, "Mapping service must not be null.");
        Preconditions.checkNotNull(schemaContextHolder, "Schema Context Holder must not be null.");
        ClientConfig config = new DefaultClientConfig();
        restClient  = Client.create(config);
        if(authHolder.authenticationRequired()){
            switch (authHolder.getAuthType()){
                case DIGEST: restClient.addFilter(new HTTPDigestAuthFilter(authHolder.getUserName(), authHolder.getPassword()));

                DEFAULT: restClient.addFilter(new HTTPBasicAuthFilter(authHolder.getUserName(), authHolder.getPassword()));
                    break;
            }

        }
        URI uri = null;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            logger.trace("Error in URI syntax {}",e.getMessage(),e);
        }
        this.defaultUri = uri;
        this.mappingService = mappingService;
        this.schemaContextHolder = schemaContextHolder;
    }

    protected URI getDefaultUri() {
        return defaultUri;
    }

    protected ListeningExecutorService getPool() {
        return pool;
    }

    protected SchemaContextHolder getSchemaContextHolder() {
        return schemaContextHolder;
    }

    protected BindingIndependentMappingService getMappingService() {
        return mappingService;
    }

    @Override
    public ListenableFuture<Set<Class<? extends RpcService>>> getRpcServices() {
        ListenableFuture<Set<Class<? extends RpcService>>> future = pool.submit(new Callable<Set<Class<? extends RpcService>>>() {
            @Override
            public Set<Class<? extends RpcService>> call() throws Exception {
                WebResource resource = restClient.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .get(ClientResponse.class);
                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }

                return RestconfUtils.rpcServicesFromInputStream(response.getEntityInputStream(),mappingService,schemaContextHolder.getSchemaContext());
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
                WebResource resource = restClient.resource(defaultUri.toString() + ResourceUri.MODULES.getPath());
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
        if (configurationDatastoreAccessor == null)
            configurationDatastoreAccessor = new ConfigurationDataStoreImpl(this);
        return configurationDatastoreAccessor;
    }

    @Override
    public OperationalDatastore getOperationalDatastore() {
        if(operationalDatastoreAccessor == null)
            operationalDatastoreAccessor =  new OperationalDataStoreImpl(this);
        return operationalDatastoreAccessor;
    }

    @Override
    public void close() {
        this.pool.shutdown();
    }


    @Override
    public void onGlobalContextUpdated(SchemaContext context) {

    }

    protected Client getRestClient() {
        return restClient;
    }

    public SchemaContext getSchemaContext() {
        return this.schemaContextHolder.getSchemaContext();
    }

    protected <T> ListenableFuture<T> get(final String path, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new GetAndTransformTask<T>(constructPath(path),processingFunction));
    }

    protected <T> ListenableFuture<T> get(final String path,final String mediaType, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new GetAndTransformTask<T>(constructPath(path),mediaType,processingFunction));
    }

    protected String constructPath(String path) {
        return getDefaultUri().toString() + path;
    }

    //, RestRestconfService {

    private class GetAndTransformTask<T> implements Callable<T> {

        private final Function<ClientResponse, T> transformation;
        private final String path;
        private final String acceptType;

        public GetAndTransformTask(String path, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType = ResourceMediaTypes.XML.getMediaType();
        }

        public GetAndTransformTask(String path, String mediaType, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType = mediaType;
        }

        @Override
        public T call() {

            WebResource resource = restClient.resource(path);
            ClientResponse response = resource.accept(acceptType).get(ClientResponse.class);

            // Applies the specific transformation for supplied resource.
            return transformation.apply(response);
        }

    }

}

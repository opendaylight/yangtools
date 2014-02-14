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
import javax.ws.rs.core.MediaType;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.auth.AuthenticationHolder;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.dto.RestEventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.dto.RestModule;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
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
                              SchemaContextHolder schemaContextHolder){
        Preconditions.checkArgument(url != null,"Restconf endpoint URL must be supplied.");
        Preconditions.checkArgument(mappingService != null, "Mapping service must not be null.");
        Preconditions.checkNotNull(schemaContextHolder, "Schema Context Holder must not be null.");
        ClientConfig config = new DefaultClientConfig();
        this.restClient  = Client.create(config);
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
        return get(ResourceUri.MODULES.getPath(), ResourceMediaTypes.XML.getMediaType(),new Function<ClientResponse, Set<Class<? extends RpcService>>>() {
            @Override
            public Set<Class<? extends RpcService>> apply(ClientResponse clientResponse) {
                if (clientResponse.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + clientResponse.getStatus());
                }
                return RestconfUtils.rpcServicesFromInputStream(clientResponse.getEntityInputStream(),mappingService,schemaContextHolder.getSchemaContext());
            }
        });
    }

    @Override
    public <T extends RpcService> RpcServiceContext<T> getRpcServiceContext(Class<T> rpcService) {
        RestRpcServiceContext restRpcServiceContext = new RestRpcServiceContext(rpcService,this.mappingService,this,schemaContextHolder.getSchemaContext());
        return restRpcServiceContext;
    }

    @Override
    public ListenableFuture<Set<EventStreamInfo>> getAvailableEventStreams() {
        return get(ResourceUri.MODULES.getPath(), ResourceMediaTypes.XML.getMediaType(),new Function<ClientResponse, Set<EventStreamInfo>>() {
            @Override
            public Set<EventStreamInfo> apply(ClientResponse clientResponse) {
                if (clientResponse.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + clientResponse.getStatus());
                }
                List<RestModule> modules = null;
                try {
                    modules = XmlTools.getModulesFromInputStream(clientResponse.getEntityInputStream());
                } catch (Exception e) {
                    logger.trace("");
                }
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
    }

    @Override
    public ListenableEventStreamContext getEventStreamContext(EventStreamInfo info) {
        RestListenableEventStreamContext listenableEventStream = new RestListenableEventStreamContext(this.mappingService,this,schemaContextHolder.getSchemaContext());
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

    public void setAuthenticationHolder(AuthenticationHolder authenticationHolder) {
        if(authenticationHolder.authenticationRequired()){
            switch (authenticationHolder.getAuthType()){
                case DIGEST: restClient.addFilter(new HTTPDigestAuthFilter(authenticationHolder.getUserName(), authenticationHolder.getPassword()));
                    break;
                default: restClient.addFilter(new HTTPBasicAuthFilter(authenticationHolder.getUserName(), authenticationHolder.getPassword()));
                    break;
            }
        }
    }

    @Override
    public void close() {
        this.pool.shutdown();
    }


    @Override
    public void onGlobalContextUpdated(SchemaContext context) {

    }

    public SchemaContext getSchemaContext() {
        return this.schemaContextHolder.getSchemaContext();
    }

    protected <T> ListenableFuture<T> get(final String path, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path), RestOperation.GET,processingFunction));
    }

    protected <T> ListenableFuture<T> get(final String path,final String mediaType, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),mediaType,RestOperation.GET,processingFunction));
    }

    protected <T> ListenableFuture<T> post(final String path, String payload, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),payload,RestOperation.POST,processingFunction));
    }

    protected <T> ListenableFuture<T> post(final String path,String payload,final String mediaType, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),payload,RestOperation.POST,mediaType,processingFunction));
    }

    protected <T> ListenableFuture<T> put(final String path, String payload, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),RestOperation.PUT,payload,processingFunction));
    }

    protected <T> ListenableFuture<T> put(final String path,String payload,final String mediaType, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),payload,RestOperation.PUT,mediaType,processingFunction));
    }
    protected <T> ListenableFuture<T> delete(final String path, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),RestOperation.DELETE,processingFunction));
    }

    protected <T> ListenableFuture<T> delete(final String path,final String mediaType, final Function<ClientResponse, T> processingFunction) {
        return pool.submit(new ExecuteOperationAndTransformTask<T>(constructPath(path),RestOperation.DELETE,mediaType,processingFunction));
    }

    protected String constructPath(String path) {
        return getDefaultUri().toString() + path;
    }

    private enum RestOperation{
        PUT,POST,GET,DELETE;
    }

    private class ExecuteOperationAndTransformTask<T> implements Callable<T> {
        private final Function<ClientResponse, T> transformation;
        private final String path;
        private final String acceptType;
        private final String payload;
        private final RestOperation restOperation;

        public ExecuteOperationAndTransformTask(String path, String payload, RestOperation operation, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType = MediaType.APPLICATION_XML; //ResourceMediaTypes.XML.getMediaType();
            this.payload = payload;
            this.restOperation = operation;
        }

        public ExecuteOperationAndTransformTask(String path,String payload, RestOperation operation,String mediaType, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType = mediaType;
            this.payload = payload;
            this.restOperation = operation;
        }
        public ExecuteOperationAndTransformTask(String path, RestOperation operation,String mediaType, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType = mediaType;
            this.payload = null;
            this.restOperation = operation;
        }
        public ExecuteOperationAndTransformTask(String path, RestOperation operation, Function<ClientResponse, T> processingFunction) {
            this.path = path;
            this.transformation = processingFunction;
            this.acceptType =  MediaType.APPLICATION_XML;
            this.payload = null;
            this.restOperation = operation;
        }

        @Override
        public T call() {
            ClientResponse response = null;
            try {
                WebResource resource = restClient.resource(path);
                switch (restOperation){
                    case PUT: response = resource.type(MediaType.APPLICATION_XML).accept(acceptType).put(ClientResponse.class, payload);
                        break;
                    case POST : response = resource.type(MediaType.APPLICATION_XML).accept(acceptType).post(ClientResponse.class, payload);
                         break;
                    case GET: response = resource.type(MediaType.APPLICATION_XML).accept(acceptType).get(ClientResponse.class);
                        break;
                    case DELETE: response = resource.type(MediaType.APPLICATION_XML).accept(acceptType).delete(ClientResponse.class);
                        break;
                }

            } catch (Exception e){
                logger.trace("Exception occured while posting data to client {}",e);
            }


            return transformation.apply(response);
        }
    }

}

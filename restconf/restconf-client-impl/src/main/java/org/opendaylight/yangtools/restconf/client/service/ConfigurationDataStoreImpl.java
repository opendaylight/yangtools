/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.service;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.LimitedDepthRetrievalStrategy;
import org.opendaylight.yangtools.restconf.client.api.data.RetrievalStrategy;
import org.opendaylight.yangtools.restconf.client.to.RestDataObject;
import org.opendaylight.yangtools.restconf.client.to.RestRpcError;
import org.opendaylight.yangtools.restconf.client.to.RestRpcResult;
import org.opendaylight.yangtools.restconf.common.QueryParameters;
import org.opendaylight.yangtools.restconf.common.ResourceMediaTypes;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.XmlTreeBuilder;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationDataStoreImpl implements ConfigurationDatastore {



    private final URI defaultUri;
    private final Client client;
    private final WebResource resource;

    private final BindingIndependentMappingService mappingService;
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDataStoreImpl.class.toString());
    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    public ConfigurationDataStoreImpl(BindingIndependentMappingService mappingService,URI uri){
        this.mappingService = mappingService;
        ClientConfig config = new DefaultClientConfig();
        this.client  = Client.create(config);
        this.resource = client.resource(uri).path("/");
        this.defaultUri = uri;
    }

    public ListenableFuture<RpcResult<Boolean>> deleteData(final InstanceIdentifier<?> path) {
        ListenableFuture future = pool.submit(new Callable<RpcResult<Boolean>>() {
            @Override
            public RpcResult<Boolean> call() throws Exception {
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+mappingService.toDataDom(path));
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .delete(ClientResponse.class);

                if (response.getStatus() != 200) {
                    RpcError rpcError = new RestRpcError(RpcError.ErrorSeverity.ERROR,RpcError.ErrorType.RPC,null,null,"HTTP status "+response.getStatus(),null,null);
                    Collection<RpcError> errors = new ArrayList<RpcError>();
                    errors.add(rpcError);
                    RestRpcResult rpcResult = new RestRpcResult(false,null,errors);
                    return (RpcResult<Boolean>) Optional.of(rpcResult);
                }
                CompositeNode nodeWrapper =  XmlTreeBuilder.buildDataTree(response.getEntityInputStream()).getParent();
                DataObject  dataObject = mappingService.dataObjectFromDataDom(path, nodeWrapper); //getDataFromResponse
                RestRpcResult rpcResult = new RestRpcResult(true,dataObject,null);
                //TODO verify if this is what is expected to be
                return (RpcResult<Boolean>) Optional.of(rpcResult);
            }
        });
        return future;
    }

    @Override
    public ListenableFuture<RpcResult<Boolean>> putData(final InstanceIdentifier<?> path) {
        ListenableFuture future = pool.submit(new Callable<RpcResult<Boolean>>() {
            @Override
            public RpcResult<Boolean> call() throws Exception {
                //TODO verify if this is what is expected to be
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+mappingService.toDataDom(path));
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .put(ClientResponse.class);

                if (response.getStatus() != 200) {
                    RpcError rpcError = new RestRpcError(RpcError.ErrorSeverity.ERROR,RpcError.ErrorType.RPC,null,null,"HTTP status "+response.getStatus(),null,null);
                    Collection<RpcError> errors = new ArrayList<RpcError>();
                    errors.add(rpcError);
                    RestRpcResult rpcResult = new RestRpcResult(false,null,errors);
                    return (RpcResult<Boolean>) Optional.of(rpcResult);
                }
                CompositeNode nodeWrapper =  XmlTreeBuilder.buildDataTree(response.getEntityInputStream()).getParent();
                DataObject  dataObject = mappingService.dataObjectFromDataDom(path, nodeWrapper);
                RestRpcResult rpcResult = new RestRpcResult(true,dataObject);
                return (RpcResult<Boolean>) Optional.of(rpcResult);
            }
        });
        return future;
    }


    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(final InstanceIdentifier<T> path) {
        ListenableFuture future = pool.submit(new Callable<Optional<T>>() {
            @Override
            public Optional<T> call() throws Exception {
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+mappingService.toDataDom(path));
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .get(ClientResponse.class);

                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }
                CompositeNode nodeWrapper =  XmlTreeBuilder.buildDataTree(response.getEntityInputStream()).getParent();
                DataObject dataObject = mappingService.dataObjectFromDataDom(path, nodeWrapper); //getDataFromResponse
                return (Optional<T>) Optional.of(new RestDataObject(dataObject.getImplementedInterface()));
            }
        });
        return future;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(final InstanceIdentifier<T> path, final RetrievalStrategy strategy) {
        ListenableFuture future = pool.submit(new Callable<Optional<T>>() {
            @Override
            public Optional<T> call() throws Exception {
                String _strategy = "";
                if (strategy instanceof  LimitedDepthRetrievalStrategy){
                    _strategy = QueryParameters.DEPTH.getQueryParameter()+"="+((LimitedDepthRetrievalStrategy)strategy).getDepthLimit();
                }
                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.CONFIG.getPath()+"/"+mappingService.toDataDom(path)+_strategy);
                final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                        .get(ClientResponse.class);

                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + response.getStatus());
                }

                CompositeNode nodeWrapper =  XmlTreeBuilder.buildDataTree(response.getEntityInputStream()).getParent();
                DataObject dataObject = mappingService.dataObjectFromDataDom(path, nodeWrapper); //getDataFromResponse
                final DataObject finalData = dataObject;
                return (Optional<T>) Optional.of(new RestDataObject(finalData.getImplementedInterface()));
            }
        });

        return future;
    }
}

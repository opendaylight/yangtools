/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.to.RestRpcError;
import org.opendaylight.yangtools.restconf.client.to.RestRpcResult;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.restconf.utils.RestconfUtils;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.ClientResponse;

public class ConfigurationDataStoreImpl extends AbstractDataStore implements ConfigurationDatastore  {

    private final ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    @Override
    protected String getStorePrefix() {
        return ResourceUri.CONFIG.getPath();
    }

    public ConfigurationDataStoreImpl(RestconfClientImpl client) {
        super(client);
    }


    @Override
    public ListenableFuture<RpcResult<Boolean>> deleteData(final InstanceIdentifier<?> path) {
        final SchemaContext schemaContext = getClient().getSchemaContext();
        final BindingIndependentMappingService mappingService = getClient().getMappingService();
        final Map.Entry<String, DataSchemaNode> pathWithSchema = RestconfUtils.toRestconfIdentifier(path, mappingService, schemaContext);
        final String restconfPath = getStorePrefix() + pathWithSchema.getKey();
        return getClient().delete(restconfPath,MediaType.APPLICATION_XML,new Function<ClientResponse, RpcResult<Boolean>>() {
            @Override
            public RpcResult<Boolean> apply(ClientResponse clientResponse) {
                Entry<String, DataSchemaNode> restconfEntry = RestconfUtils.toRestconfIdentifier(mappingService.toDataDom(path), schemaContext);
                if (clientResponse.getStatus() != 200) {
                    RpcError rpcError = new RestRpcError(RpcError.ErrorSeverity.ERROR,RpcError.ErrorType.RPC,null,null,"HTTP status "+clientResponse.getStatus(),null,null);
                    Collection<RpcError> errors = new ArrayList<RpcError>();
                    errors.add(rpcError);
                    RestRpcResult rpcResult = new RestRpcResult(false,null,errors);
                    return (RpcResult<Boolean>) Optional.of(rpcResult);
                }
                DataObject dataObject = RestconfUtils.dataObjectFromInputStream(path, clientResponse.getEntityInputStream(), schemaContext, mappingService,restconfEntry.getValue());
                RestRpcResult rpcResult = new RestRpcResult(true,dataObject,null);
                return (RpcResult<Boolean>) Optional.of(rpcResult);
            }
        });
    }

    @Override
    public ListenableFuture<RpcResult<Boolean>> putData(final InstanceIdentifier<?> path) {
        final SchemaContext schemaContext = getClient().getSchemaContext();
        final BindingIndependentMappingService mappingService = getClient().getMappingService();
        final Map.Entry<String, DataSchemaNode> pathWithSchema = RestconfUtils.toRestconfIdentifier(path, mappingService, schemaContext);
        final String restconfPath = getStorePrefix() + pathWithSchema.getKey();

        return getClient().put(restconfPath,MediaType.APPLICATION_XML,new Function<ClientResponse, RpcResult<Boolean>>() {
            @Override
            public RpcResult<Boolean> apply(ClientResponse clientResponse) {
                Map.Entry<String, DataSchemaNode> restconfEntry = RestconfUtils.toRestconfIdentifier(mappingService.toDataDom(path), schemaContext);
                if (clientResponse.getStatus() != 200) {
                    RpcError rpcError = new RestRpcError(RpcError.ErrorSeverity.ERROR,RpcError.ErrorType.RPC,null,null,"HTTP status "+clientResponse.getStatus(),null,null);
                    Collection<RpcError> errors = new ArrayList<RpcError>();
                    errors.add(rpcError);
                    RestRpcResult rpcResult = new RestRpcResult(false,null,errors);
                    return (RpcResult<Boolean>) Optional.of(rpcResult);
                }
                DataObject dataObject = RestconfUtils.dataObjectFromInputStream(path, clientResponse.getEntityInputStream(),schemaContext,mappingService,restconfEntry.getValue());
                RestRpcResult rpcResult = new RestRpcResult(true,dataObject);
                return (RpcResult<Boolean>) Optional.of(rpcResult);
            }
        });
    }
}

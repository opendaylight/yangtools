/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Function;
import com.sun.jersey.api.client.ClientResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.restconf.client.to.RestRpcError;
import org.opendaylight.yangtools.restconf.client.to.RestRpcResult;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class BindingToRestRpc implements InvocationHandler {

    private final RestconfClientImpl client;
    private static final Logger logger = LoggerFactory.getLogger(BindingToRestRpc.class);
    private final BindingNormalizedNodeCodecRegistry mappingService;
    private final SchemaContext schcemaContext;
    private final Module module;
    private static final int STATUS_OK = 200;
    
    public BindingToRestRpc(final Class<?> proxiedInterface,final BindingNormalizedNodeCodecRegistry mappingService2,final RestconfClientImpl client,final SchemaContext schemaContext) throws Exception {
        this.mappingService = mappingService2;
        this.client  = client;
        this.schcemaContext = schemaContext;
        YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(proxiedInterface);
        this.module = schemaContext.findModuleByName(moduleInfo.getName(),org.opendaylight.yangtools.yang.common.QName.parseRevision(moduleInfo.getRevision()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(final Object o,final Method method, final Object[] objects) throws Exception {
        for (RpcDefinition rpcDef:module.getRpcs()){
            if (method.getName().equals(BindingMapping.getMethodName(rpcDef.getQName()))){

                String moduleName = BindingReflections.getModuleInfo(method.getDeclaringClass()).getName();
                String rpcMethodName = rpcDef.getQName().getLocalName();
                Document rpcInputDoc = null;
                for (Object component:objects){
                    ContainerNode rpcInput = mappingService.toNormalizedNodeRpcData((DataObject) component);

                    // FIXME: NormalizedNodeXmlStreamWriter

                }

                String payloadString = null;
                final DataSchemaNode rpcOutputSchema = rpcDef.getOutput();
                return client.post(ResourceUri.OPERATIONS.getPath() + "/" + moduleName + ":" + rpcMethodName,payloadString,new Function<ClientResponse, Object>() {
                    @Override
                    public Object apply(final ClientResponse clientResponse) {
                        if (clientResponse.getStatus() != STATUS_OK) {
                            throw new IllegalStateException("Can't get data from restconf. "+clientResponse.getClientResponseStatus());
                        }
                        List<RpcError> errors =  new ArrayList<>();
                        try {
                            ContainerNode output = null;
                            DataContainer rpcOutputDataObject = mappingService.fromNormalizedNodeRpcData(rpcOutputSchema.getPath(), output);
                            return new RestRpcResult(true,rpcOutputDataObject);
                        } catch (Exception e) {
                            logger.trace("Error while extracting rpc output in proxy method {}",e);
                            RestRpcError error = new RestRpcError(RpcError.ErrorSeverity.ERROR, RpcError.ErrorType.APPLICATION,"Error while extracting rpc output in proxy method.",e);
                            errors.add(error);
                        }
                        return new RestRpcResult(false,errors);
                    }
                });
            }
        }
        throw new IllegalStateException("Unexpected state of proxy method.");
    }

    @SuppressWarnings("unchecked")
    public static<T> T getProxy(final Class<T> rpcService,
                                final BindingNormalizedNodeCodecRegistry mappingService2,
                                final RestconfClientImpl restconfClient,
                                final SchemaContext schemaContext) {
        T proxiedType = null;
        try {
            proxiedType = (T) Proxy.newProxyInstance
                    (BindingToRestRpc.class.getClassLoader(),
                            new Class[]{rpcService}, new BindingToRestRpc(rpcService, mappingService2, restconfClient, schemaContext));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        return proxiedType;
    }


}

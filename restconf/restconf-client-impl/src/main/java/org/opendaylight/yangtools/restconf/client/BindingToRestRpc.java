/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.restconf.client.to.RestRpcError;
import org.opendaylight.yangtools.restconf.client.to.RestRpcResult;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.restconf.utils.XmlTools;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class BindingToRestRpc implements InvocationHandler {

    private final RestconfClientImpl client;
    private static final Logger logger = LoggerFactory.getLogger(BindingToRestRpc.class);
    private final BindingIndependentMappingService mappingService;
    private final SchemaContext schemaContext;
    private final Module module;

    public BindingToRestRpc(final Class<?> proxiedInterface,final BindingIndependentMappingService mappingService,final RestconfClientImpl client,final SchemaContext schemaContext) throws Exception {
        this.mappingService = mappingService;
        this.client  = client;
        this.schemaContext = schemaContext;
        YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(proxiedInterface);
        this.module = schemaContext.findModuleByName(moduleInfo.getName(),org.opendaylight.yangtools.yang.common.QName.parseRevision(moduleInfo.getRevision()));
    }

    @Override
    public Object invoke(final Object o,final Method method, final Object[] objects) throws Exception {
        for (RpcDefinition rpcDef:module.getRpcs()){
            if (method.getName().equals(BindingMapping.getMethodName(rpcDef.getQName()))){

                String moduleName = BindingReflections.getModuleInfo(method.getDeclaringClass()).getName();
                String rpcMethodName = rpcDef.getQName().getLocalName();
                Document rpcInputDoc = null;
                for (Object component:objects){
                    CompositeNode rpcInput = mappingService.toDataDom((DataObject) component);
                    rpcInputDoc = XmlDocumentUtils.toDocument(rpcInput,rpcDef.getInput(),XmlDocumentUtils.defaultValueCodecProvider(), schemaContext);
                }
                DOMImplementationLS lsImpl = (DOMImplementationLS)rpcInputDoc.getImplementation().getFeature("LS", "3.0");
                LSSerializer serializer = lsImpl.createLSSerializer();
                serializer.getDomConfig().setParameter("xml-declaration", false);

                String payloadString = serializer.writeToString(rpcInputDoc);
                final Class<? extends DataContainer> desiredOutputClass = (Class<? extends DataContainer>)BindingReflections.resolveRpcOutputClass(method).get();
                final DataSchemaNode rpcOutputSchema = rpcDef.getOutput();
                return client.post(ResourceUri.OPERATIONS.getPath() + "/" + moduleName + ":" + rpcMethodName,payloadString,new Function<ClientResponse, Object>() {
                    @Override
                    public Object apply(final ClientResponse clientResponse) {
                        if (clientResponse.getStatus() != 200) {
                            throw new IllegalStateException("Can't get data from restconf. "+clientResponse.getClientResponseStatus());
                        }
                        List<RpcError> errors =  new ArrayList<>();
                        try {
                            Document rpcOutputDocument = XmlTools.fromXml(clientResponse.getEntityInputStream());
                            CompositeNode cn = (CompositeNode) XmlDocumentUtils.toDomNode(rpcOutputDocument.getDocumentElement(),
                                    Optional.of(rpcOutputSchema),
                                    Optional.of(XmlDocumentUtils.defaultValueCodecProvider()));
                            DataContainer rpcOutputDataObject = mappingService.dataObjectFromDataDom(desiredOutputClass, cn);
                            return new RestRpcResult(true,rpcOutputDataObject);
                        } catch (Exception e) {
                            logger.trace("Error while extracting rpc output in proxy method {}",e);
                            RestRpcError error = new RestRpcError(RpcError.ErrorSeverity.ERROR, RpcError.ErrorType.APPLICATION,"Error while extracting rpc output in proxy method.",e);
                        }
                        return new RestRpcResult(false,errors);
                    }
                });
            }
        }
        throw new IllegalStateException("Unexpected state of proxy method.");
    }

    public static<T> T getProxy(final Class<T> proxiedInterface,
                                final BindingIndependentMappingService mappingService,
                                final RestconfClientImpl restconfClient,
                                final SchemaContext schemaContext) {
        T proxiedType = null;
        try {
            proxiedType = (T) Proxy.newProxyInstance
                    (BindingToRestRpc.class.getClassLoader(),
                            new Class[]{proxiedInterface}, new BindingToRestRpc(proxiedInterface, mappingService, restconfClient, schemaContext));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

        return proxiedType;
    }


}

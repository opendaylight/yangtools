/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.restconf.common.ResourceMediaTypes;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.restconf.utils.XmlTools;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class BindingToRestRpc implements InvocationHandler {

    private final Client client;
    private final URI defaultUri;
    private static final Logger logger = LoggerFactory.getLogger(BindingToRestRpc.class);
    private final BindingIndependentMappingService mappingService;

    public BindingToRestRpc(URI uri,BindingIndependentMappingService mappingService) {
        this.mappingService = mappingService;
        ClientConfig config = new DefaultClientConfig();
        this.client  = Client.create(config);
        this.client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        this.defaultUri = uri;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

        YangModelParser parser = new YangParserImpl();

        Method getInstanceMethod = Class.forName(method.getDeclaringClass().getPackage().getName()+".$YangModuleInfoImpl").getMethod("getInstance",new Class[0]);

        YangModuleInfo yangModuleInfo = (YangModuleInfo) getInstanceMethod.invoke(null,new Object[0]);

        List<InputStream> moduleStreams = new ArrayList<InputStream>();
        moduleStreams.add(yangModuleInfo.getModuleSourceStream());

        Set<Module> modules = parser.parseYangModelsFromStreams(moduleStreams);

        for (Module m:modules){
            for (RpcDefinition rpcDef:m.getRpcs()){
                if (method.getName().equals(BindingMapping.getMethodName(rpcDef.getQName()))){

                    String moduleName = BindingReflections.getModuleInfo(method.getDeclaringClass()).getName();
                    String rpcMethodName = rpcDef.getQName().getLocalName();
                    Document mergedDoc = null;
                    for (Object component:objects){
                        Document doc = XmlDocumentUtils.toDocument(mappingService.toDataDom((DataObject) component), XmlDocumentUtils.defaultValueCodecProvider());
                        if (null == mergedDoc) {
                            mergedDoc = doc;
                        } else {
                            mergedDoc.importNode(doc.getFirstChild(),true);
                        }
                    }
                    DOMImplementationLS lsImpl = (DOMImplementationLS)mergedDoc.getImplementation().getFeature("LS", "3.0");
                    LSSerializer serializer = lsImpl.createLSSerializer();
                    serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
                    String str = serializer.writeToString(mergedDoc);
                    WebResource resource = client.resource(defaultUri.toString() + ResourceUri.OPERATIONS.getPath() + "/"+ moduleName+":"+rpcMethodName);

                    final ClientResponse response = resource.accept(ResourceMediaTypes.XML.getMediaType())
                            .post(ClientResponse.class,str);


                    if (response.getStatus() != 200) {
                        throw new IllegalStateException("Can't get data from restconf. "+response.getClientResponseStatus());
                    }
                    return XmlTools.unmarshallXml(method.getReturnType(), response.getEntityInputStream());
                }
            }
        }
        return null;
    }

    public static<T> T getProxy(Class<T> intf,
                                URI uri,BindingIndependentMappingService mappingService) {
            Object obj = (T) Proxy.newProxyInstance
                    (BindingToRestRpc.class.getClassLoader(),
                            new Class[]{intf}, new BindingToRestRpc(uri,mappingService));

            return (T) obj;
    }


}

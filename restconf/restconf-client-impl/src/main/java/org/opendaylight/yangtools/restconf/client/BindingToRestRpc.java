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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import org.opendaylight.yangtools.XmlTools;
import org.opendaylight.yangtools.restconf.common.ResourceMediaTypes;
import org.opendaylight.yangtools.restconf.common.ResourceUri;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;

public class BindingToRestRpc implements InvocationHandler {

    private Client client;
    private URI defaultUri;
    private final BindingIndependentMappingService mappingService;

    public BindingToRestRpc(URI uri) {
        ClientConfig config = new DefaultClientConfig();
        this.client  = Client.create(config);
        this.defaultUri = uri;
        mappingService = new RuntimeGeneratedMappingServiceImpl();
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        Set<org.opendaylight.yangtools.yang.common.QName> methods =  mappingService.getRpcQNamesFor((Class<? extends RpcService>) o.getClass());
        Iterator<org.opendaylight.yangtools.yang.common.QName> methodsIterator = methods.iterator();
        while (methodsIterator.hasNext()){
            org.opendaylight.yangtools.yang.common.QName methodQN = methodsIterator.next();
            if (method.toString().equals(BindingMapping.getMethodName(methodQN))){

                String moduleName = BindingReflections.getModuleInfo(o.getClass()).getName();
                String rpcMethodName = methodQN.getLocalName();

                WebResource resource = client.resource(defaultUri.toString() + ResourceUri.OPERATIONS.getPath() + "/"+ moduleName+":"+rpcMethodName);

                final ClientResponse response = resource.accept(ResourceMediaTypes.xml.getMediaType())
                        .get(ClientResponse.class);


                if (response.getStatus() != 200) {
                    throw new IllegalStateException("Can't get data from restconf ");
                }
                return XmlTools.unmarshallXml(method.getReturnType(),response.getEntityInputStream());
            }
        }
        return null;
    }

    public static<T> T getProxy(Class<T> intf,
                                URI uri) {
        return (T) Proxy.newProxyInstance
                (BindingToRestRpc.class.getClassLoader(),
                        new Class[]{intf}, new BindingToRestRpc(uri));
    }


}

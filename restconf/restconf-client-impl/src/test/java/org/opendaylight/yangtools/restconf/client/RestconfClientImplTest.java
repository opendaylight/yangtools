/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import javassist.ClassPool;

import org.junit.Test;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.UnsupportedProtocolException;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImplTest {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientImplTest.class);
    private static final String restconfUrl = "http://localhost:8080";
    public static final String JSON = "+json";
    public static final String XML = "+xml";
    private  RestconfClientContext restconfClientContext;




    @Test
    public void testGetRpcServiceContext() throws ExecutionException, InterruptedException, MalformedURLException, UnsupportedProtocolException {
        URI uri = URI.create(restconfUrl);
        RestconfClientFactory factory = new RestconfClientFactory();
        RuntimeGeneratedMappingServiceImpl mappingService = new RuntimeGeneratedMappingServiceImpl();
        mappingService.setPool(new ClassPool());
        mappingService.init();

        ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());

        mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());

        restconfClientContext = factory.getRestconfClientContext(uri.toURL(), mappingService, mappingService);
        assertNotNull(restconfClientContext);

        OperationalDatastore datastore = restconfClientContext.getOperationalDatastore();

          // Example use of client
//        ListenableFuture<Optional<Nodes>> result = datastore.readData(InstanceIdentifier.builder(Nodes.class).toInstance());
//        Optional<Nodes> optionalNodes = result.get();
//        Nodes node = optionalNodes.get();
//        assertNotNull(node);


    }

}

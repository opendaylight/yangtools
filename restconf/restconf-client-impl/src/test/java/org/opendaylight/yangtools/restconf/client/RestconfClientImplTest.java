/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.ListenableFuture;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.UnsupportedProtocolException;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientImplTest {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientImplTest.class);
    private static final String restconfUrl = "http://localhost:8080";
    //private static final String restconfUrl = "http://pce-guest35.cisco.com:9080";
    public static final String JSON = "+json";
    public static final String XML = "+xml";
    private  RestconfClientContext restconfClientContext;

    @Before
    public void setupRestconfClientContext() throws MalformedURLException, UnsupportedProtocolException {

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        this.restconfClientContext = new RestconfClientFactory().getRestconfClientContext(new URL(restconfUrl),new SchemaContextHolder() {

            @Override
            public SchemaContext getSchemaContext() {
                return moduleInfo.tryToCreateSchemaContext().get();
            }
        });
        assertNotNull(this.restconfClientContext);
    }

//    @Test
    public void testGetAvailableEventStreams(){
        ListenableFuture<Set<EventStreamInfo>> streamsFuture = restconfClientContext.getAvailableEventStreams();
        while (!streamsFuture.isDone()){
            //noop
        }
        if (streamsFuture.isDone()){
            try {
                Set<EventStreamInfo> streams = streamsFuture.get();
                assertNotNull(streams);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            } catch (ExecutionException e) {
                fail(e.getMessage());
            }
        }
    }
//    @Test
    public void testGetRpcServices(){
        ListenableFuture<Set<Class<? extends RpcService>>> servicesFuture = restconfClientContext.getRpcServices();
        while (!servicesFuture.isDone()){
            //noop
        }
        if (servicesFuture.isDone()){
            try {
                Set<Class<? extends RpcService>> streams = servicesFuture.get();
                assertNotNull(streams);
            } catch (InterruptedException e) {
                fail(e.getMessage());
            } catch (ExecutionException e) {
                fail(e.getMessage());
            }
        }
    }

//    @Test
    public void getEventStreamContext() throws MalformedURLException, UnsupportedProtocolException, ExecutionException, InterruptedException {
        ListenableFuture<Set<EventStreamInfo>>  evtStreams = restconfClientContext.getAvailableEventStreams();
        while (!evtStreams.isDone()){
            //noop
        }

        Iterator<EventStreamInfo> it = evtStreams.get().iterator();
        ListenableEventStreamContext evtStreamCtx = restconfClientContext.getEventStreamContext(it.next());
        assertNotNull(evtStreamCtx);
    }
//    @Test
    public void testGetOperationalDatastore() throws ExecutionException, InterruptedException, MalformedURLException, UnsupportedProtocolException {
        OperationalDatastore datastore = restconfClientContext.getOperationalDatastore();
        assertNotNull(datastore);

    }
//    @Test
    public void testGetConfigurationDatastore() throws ExecutionException, InterruptedException, MalformedURLException, UnsupportedProtocolException {
        ConfigurationDatastore datastore = restconfClientContext.getConfigurationDatastore();
        assertNotNull(datastore);
    }


}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import com.google.common.util.concurrent.ListenableFuture;
import java.net.URI;
import java.util.Set;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.restconf.jaxrs.api.RestconfServiceImpl;
import org.opendaylight.yangtools.restconf.jaxrs.api.RestconfService;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RestconfClient implements RestconfClientContext {

    private final RestconfService restconfService;


    public RestconfClient(URI uri){
        this.restconfService = new RestconfServiceImpl(uri);
    }
    @Override
    public ListenableFuture<Set<Class<? extends RpcService>>> getRpcServices() {
        restconfService.getOperations();
        return null;
    }

    @Override
    public <T extends RpcService> RpcServiceContext<T> getRpcServiceContext(Class<T> rpcService) {
        restconfService.get
    }

    @Override
    public ListenableFuture<Set<EventStreamInfo>> getAvailableEventStreams() {
        return null;
    }

    @Override
    public ListenableEventStreamContext getEventStreamContext(EventStreamInfo info) {
        return null;
    }

    @Override
    public ConfigurationDatastore getConfigurationDatastore() {
        return null;
    }

    @Override
    public OperationalDatastore getOperationalDatastore() {
        return null;
    }

    @Override
    public void close() {

    }
}

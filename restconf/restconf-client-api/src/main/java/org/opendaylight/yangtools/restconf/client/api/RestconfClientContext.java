/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api;

import java.util.Set;

import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamContext;
import org.opendaylight.yangtools.yang.binding.RpcService;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;

public interface RestconfClientContext extends AutoCloseable {
    /**
     * Returns a map of {@link RpcService} which provides invocation
     * handling for RPCs supported by server.
     * 
     * @return
     */
    ClassToInstanceMap<RpcService> getRpcServices();

    <T extends RpcService> T getRpcService(Class<T> rpcService);

    OperationalDatastore getOperationalDatastore();

    ConfigurationDatastore getConfigurationDatastore();
    
    ListenableFuture<Set<EventStreamInfo>> getAvailableEventStreams();
    
    EventStreamContext getEventStreamContext(EventStreamInfo info); 
}

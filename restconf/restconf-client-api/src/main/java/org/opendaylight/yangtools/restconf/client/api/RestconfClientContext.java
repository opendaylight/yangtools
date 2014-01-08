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
import org.opendaylight.yangtools.restconf.client.api.event.ListenableEventStreamContext;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.yang.binding.RpcService;

import com.google.common.util.concurrent.ListenableFuture;

public interface RestconfClientContext extends AutoCloseable {
	/**
	 * Returns a set of {@link RpcServiceContext} which provides invocation
	 * handling for RPCs supported by the backing server.
	 * 
	 * @return Future representing the asynchronous call to fetch the information.
	 */
	ListenableFuture<Set<Class<? extends RpcService>>> getRpcServices();
	<T extends RpcService> RpcServiceContext<T> getRpcServiceContext(Class<T> rpcService);

	ListenableFuture<Set<EventStreamInfo>> getAvailableEventStreams();
	ListenableEventStreamContext getEventStreamContext(EventStreamInfo info);

	ConfigurationDatastore getConfigurationDatastore();
	OperationalDatastore getOperationalDatastore();

	@Override
	void close();
}

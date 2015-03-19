/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.restconf.client.BindingToRestRpc;
import org.opendaylight.yangtools.restconf.client.RestconfClientImpl;
import org.opendaylight.yangtools.restconf.client.api.rpc.RpcServiceContext;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRpcServiceContext <T extends RpcService> implements RpcServiceContext<T> {

    private static final Logger logger = LoggerFactory.getLogger(RestRpcServiceContext.class);
    private T rpcServiceProxy;

    public RestRpcServiceContext(Class<T> rpcService,BindingNormalizedNodeCodecRegistry mappingService, RestconfClientImpl
            restconfClient,SchemaContext schemaContext){
        this.rpcServiceProxy = BindingToRestRpc.getProxy(rpcService,mappingService,restconfClient,schemaContext);
    }

    @Override
    public T getRpcService() {
        return this.rpcServiceProxy;
    }

    @Override
    public void close() {

    }
}

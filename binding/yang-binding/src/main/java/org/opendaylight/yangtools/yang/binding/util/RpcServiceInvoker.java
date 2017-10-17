/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.base.Preconditions;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides single method invocation of RPCs on supplied instance.
 *
 * <p>
 * RPC Service invoker provides common invocation interface for any subtype of {@link RpcService}.
 * via {@link #invokeRpc(RpcService, QName, DataObject)} method.
 */
public abstract class RpcServiceInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServiceInvoker.class);

    /**
     * Creates RPCServiceInvoker for specified RpcService type.
     *
     * @param type RpcService interface, which was generated from model.
     * @return Cached instance of {@link RpcServiceInvoker} for supplied RPC type.
     */
    public static RpcServiceInvoker from(final Class<? extends RpcService> type) {
        return ClassBasedRpcServiceInvoker.instanceFor(type);
    }

    /**
     * Creates an RPCServiceInvoker for specified QName-&lt;Method mapping.
     *
     * @param qnameToMethod translation mapping, must not be null nor empty.
     * @return An {@link RpcMethodInvoker} instance.
     */
    public static RpcServiceInvoker from(final Map<QName, Method> qnameToMethod) {
        Preconditions.checkArgument(!qnameToMethod.isEmpty());
        QNameModule module = null;

        for (QName qname : qnameToMethod.keySet()) {
            if (module != null) {
                if (!module.equals(qname.getModule())) {
                    LOG.debug("QNames from different modules {} and {}, falling back to QName map", module,
                        qname.getModule());
                    return QNameRpcServiceInvoker.instanceFor(qnameToMethod);
                }
            } else {
                module = qname.getModule();
            }
        }

        // All module are equal, which means we can use localName only
        return LocalNameRpcServiceInvoker.instanceFor(module, qnameToMethod);
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl Imlementation on which RPC should be invoked.
     * @param rpcName Name of RPC to be invoked.
     * @param input Input data for RPC.
     * @return Future which will complete once rpc procesing is finished.
     */
    public abstract Future<RpcResult<?>> invokeRpc(@Nonnull RpcService impl, @Nonnull QName rpcName,
            @Nullable DataObject input);
}

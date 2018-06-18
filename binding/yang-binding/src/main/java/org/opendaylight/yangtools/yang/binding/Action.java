/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import javax.annotation.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Interface extended by all interfaces generated for a YANG {@code action}.
 *
 * @author Robert Varga
 */
@Beta
@FunctionalInterface
@NonNullByDefault
public interface Action<P extends DataObject, I extends RpcInput, O extends RpcOutput> {
    /**
     * Invoke the action.
     *
     * @param path Invocation path
     * @param input Input argument
     * @return Future result of invocation
     * @throws NullPointerException if any of the arguments are null
     */
    @CheckReturnValue
    FluentFuture<RpcResult<O>> invoke(InstanceIdentifier<P> path, I input);
}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * This is an old tagging interface, which is not used anywhere right now.
 *
 * @deprecated This slated for removal unless we find a use for it.
 */
@Deprecated
public interface RpcImplementation {

    Set<Class<? extends RpcInput>> getSupportedInputs();

    <I extends RpcInput, O extends RpcOutput> ListenableFuture<RpcResult<O>> invoke(Class<I> type, I input);
}

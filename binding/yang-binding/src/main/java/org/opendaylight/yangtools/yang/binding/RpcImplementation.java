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

public interface RpcImplementation {

    // FIXME: Change to RpcInput
    Set<Class<? extends DataContainer>> getSupportedInputs();

    // FIXME: Change to RpcInput
    <T extends DataContainer> ListenableFuture<RpcResult<?>> invoke(Class<T> type, T input);
}

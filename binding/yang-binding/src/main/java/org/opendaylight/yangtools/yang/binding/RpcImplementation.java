/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.Set;
import java.util.concurrent.Future;

import org.opendaylight.yangtools.yang.common.RpcResult;

public interface RpcImplementation {

    // Fixme: Change to RpcInput
    Set<Class<? extends DataContainer>> getSupportedInputs();

    // Fixme: Change to RpcInput
    <T extends DataContainer> Future<RpcResult<?>> invoke(Class<T> type, T input);
}

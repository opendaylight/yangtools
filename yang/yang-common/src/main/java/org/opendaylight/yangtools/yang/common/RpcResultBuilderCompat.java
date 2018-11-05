/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Package-internal compatibility superclass for RpcResultBuilder. This is necessary to ensure
 * {@link RpcResultBuilder#buildFuture()} remains ABI-compatible with ListenableFuture return.
 *
 * @deprecated Remove this class in 3.0.0.
 */
@Deprecated
abstract class RpcResultBuilderCompat<T> {

    public abstract ListenableFuture<RpcResult<T>> buildFuture();
}

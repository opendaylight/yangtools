/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.util.Collection;

/**
 * Represents a general result of a call, request, or operation.
 *
 * @param <T> the result value type
 */
@Deprecated
public interface RpcResult<T> {

    /**
     * Returns whether or not processing of the call was successful.
     *
     * @return true if processing was successful, false otherwise.
     */
    boolean isSuccessful();

    /**
     * Returns the value result of the call or null if no result is available.
     */
    T getResult();

    /**
     * Returns a set of errors and warnings which occurred during processing
     * the call.
     *
     * @return a Collection of {@link RpcError}
     */
    Collection<RpcError> getErrors();
}

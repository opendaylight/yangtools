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
 *
 * Result of call to YANG enabled system.
 *
 *
 * @param <T> Return type
 */
public interface RpcResult<T> {

    /**
     * True if processing of request was successful
     *
     * @return true if processing was successful.
     */
    boolean isSuccessful();

    /**
     *
     * Returns result of call or null if no result is available.
     *
     * @return result of call or null if no result is available.
     *
     */
    T getResult();

    /**
     *
     * Returns set of errors and warnings which occured during processing
     * the request.
     *
     * @return
     */
    Collection<RpcError> getErrors();
}

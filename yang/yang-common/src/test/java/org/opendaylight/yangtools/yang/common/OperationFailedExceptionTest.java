/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OperationFailedExceptionTest {

    @Test
    public void testOperationFailedException() {
        final Throwable cause = new Throwable( "mock cause" );
        final RpcError rpcErrorShort = RpcResultBuilder.newError(RpcError.ErrorType.RPC, "tag", "msg");
        final OperationFailedException operationFailedException1 = new OperationFailedException("error msg", cause,
                rpcErrorShort);
        final OperationFailedException operationFailedException2 = new OperationFailedException("error msg", rpcErrorShort);
        assertEquals(operationFailedException1.getErrorList(), operationFailedException2.getErrorList());
        assertTrue(operationFailedException1.toString().contains("error msg"));
    }
}

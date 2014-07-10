/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;

/**
 * Unit tests for RpcResultBuilder.
 *
 * @author Thomas Pantelis
 */
public class RpcResultBuilderTest {

    @Test
    public void testSuccess() {
        RpcResult<String> result = RpcResultBuilder.<String>success().withResult( "foo" ).build();
        verifyRpcResult( result, true, "foo" );
        assertNotNull( "getErrors returned null", result.getErrors() );
        assertEquals( "getErrors size", 0, result.getErrors().size() );

        result = RpcResultBuilder.<String>success( "bar" ).build();
        verifyRpcResult( result, true, "bar" );
    }

    @Test
    public void testFailed() {
        Throwable cause = new Throwable( "mock cause" );
        Throwable cause2 = new Throwable( "mock cause2" );
        RpcResult<String> result = RpcResultBuilder.<String>failed()
                  .withError( ErrorType.PROTOCOL, "error message 1" )
                  .withError( ErrorType.APPLICATION, "lock_denied", "error message 2" )
                  .withError( ErrorType.RPC, "in-use", "error message 3", "my-app-tag", "my-info", cause )
                  .withError( ErrorType.TRANSPORT, "error message 4", cause2 )
                  .build();
        verifyRpcResult( result, false, null );
        verifyRpcError( result, 0, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message 1", null, null, null );
        verifyRpcError( result, 1, ErrorSeverity.ERROR, ErrorType.APPLICATION, "lock_denied",
                        "error message 2", null, null, null );
        verifyRpcError( result, 2, ErrorSeverity.ERROR, ErrorType.RPC, "in-use",
                        "error message 3", "my-app-tag", "my-info", cause );
        verifyRpcError( result, 3, ErrorSeverity.ERROR, ErrorType.TRANSPORT, "operation-failed",
                        "error message 4", null, null, cause2 );
        assertEquals( "getErrors size", 4, result.getErrors().size() );
    }

    @Test
    public void testWithWarnings() {
        Throwable cause = new Throwable( "mock cause" );
        RpcResult<String> result = RpcResultBuilder.<String>success()
                  .withWarning( ErrorType.APPLICATION, "lock_denied", "message 1" )
                  .withWarning( ErrorType.RPC, "in-use", "message 2", "my-app-tag", "my-info", cause )
                  .build();
        verifyRpcResult( result, true, null );
        verifyRpcError( result, 0, ErrorSeverity.WARNING, ErrorType.APPLICATION, "lock_denied",
                        "message 1", null, null, null );
        verifyRpcError( result, 1, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message 2", "my-app-tag", "my-info", cause );
        assertEquals( "getErrors size", 2, result.getErrors().size() );
    }

    @Test
    public void testFrom() {
        Throwable cause = new Throwable( "mock cause" );
        RpcResult<String> result = RpcResultBuilder.<String>success()
                .withResult( "foo" )
                .withWarning( ErrorType.RPC, "in-use", "message", "my-app-tag", "my-info", cause )
                .build();

        RpcResult<String> copy = RpcResultBuilder.<String>from( result )
                .withError( ErrorType.PROTOCOL, "error message" )
                .build();
        verifyRpcResult( copy, true, "foo" );
        verifyRpcError( copy, 0, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message", "my-app-tag", "my-info", cause );
        verifyRpcError( copy, 1, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message", null, null, null );
    }

    @Test
    public void testWithRpcErrors() {
        Throwable cause = new Throwable( "mock cause" );
        RpcResult<String> result = RpcResultBuilder.<String>failed()
                .withWarning( ErrorType.RPC, "in-use", "message", "my-app-tag", "my-info", cause )
                .withError( ErrorType.PROTOCOL, "error message" )
                .build();

        RpcResult<String> result2 = RpcResultBuilder.<String>failed()
                .withRpcErrors( result.getErrors() )
                .build();
        verifyRpcError( result2, 0, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message", "my-app-tag", "my-info", cause );
        verifyRpcError( result2, 1, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message", null, null, null );
    }

    void verifyRpcError( RpcResult<?> result, int errorIndex, ErrorSeverity expSeverity,
            ErrorType expErrorType, String expTag, String expMessage, String expAppTag,
            String expInfo, Throwable expCause ) {

        List<RpcError> errors = new ArrayList<>( result.getErrors() );
        assertTrue( "Expected error at index " + errorIndex + " not found",
                    errorIndex < errors.size() );
        RpcError error = errors.get( errorIndex );
        assertEquals( "getSeverity", expSeverity, error.getSeverity() );
        assertEquals( "getErrorType", expErrorType, error.getErrorType() );
        assertEquals( "getTag", expTag, error.getTag() );
        assertEquals( "getMessage", expMessage, error.getMessage() );
        assertEquals( "getApplicationTag", expAppTag, error.getApplicationTag() );
        assertEquals( "getInfo", expInfo, error.getInfo() );
        assertEquals( "getCause", expCause, error.getCause() );
    }

    void verifyRpcResult( RpcResult<?> result, boolean expSuccess, Object expValue ) {
        assertEquals( "isSuccessful", expSuccess, result.isSuccessful() );
        assertEquals( "getResult", expValue, result.getResult() );
    }
}

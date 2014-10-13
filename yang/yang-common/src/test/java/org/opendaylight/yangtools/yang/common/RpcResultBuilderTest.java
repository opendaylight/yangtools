/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testToString() {
        final RpcError testError = RpcResultBuilder.newError(ErrorType.APPLICATION, "test error", "test error");
        assertNotNull("String representation of rpcError shouldn't be null.", testError.toString());
    }

    @Test
    public void testStatusMethod() {
        final RpcResultBuilder<String> testStatusBuilder = RpcResultBuilder.<String>status(false);
        final RpcResult<String> testStatus = testStatusBuilder.build();
        assertFalse("Status should be false.", testStatus.isSuccessful());
    }

    @Test
    public void testRpcResultToString() {
        final RpcResultBuilder<String> testStatusBuilder = RpcResultBuilder.<String>status(false);
        final RpcResult<String> testStatus = testStatusBuilder.build();
        assertNotNull("String representation of rpcResult shouldn't be null.", testStatus.toString());
    }

    @Test
    public void testNewErrorMethod() {
        final RpcError testRpcError = RpcResultBuilder.newError(ErrorType.APPLICATION, "test error", "test error");
        final RpcError testRpcError2 = RpcResultBuilder.newError(ErrorType.APPLICATION, null, "test error");
        final RpcError testRpcError3 = RpcResultBuilder.newError(ErrorType.PROTOCOL, "HTTP", "Bad request", "test app tag", "test info", new Throwable("Test new error method"));
        final RpcError testRpcError4 = RpcResultBuilder.newError(ErrorType.PROTOCOL, null, "Bad request", "test app tag", "test info", new Throwable("Test new error method"));

        assertNotNull("testRpcError shouldn't be null.", testRpcError);
        assertEquals("Tag of testRpcError2 should be 'operation-failed'.", "operation-failed", testRpcError2.getTag());
        assertNotNull("testRpcError3 shouldn't be null.", testRpcError3);
        assertEquals("Tag of testRpcError4 should be 'operation-failed'.", "operation-failed", testRpcError4.getTag());
    }

    @Test
    public void testNewWarningMethod() {
        final RpcError testRpcError = RpcResultBuilder.newWarning(ErrorType.APPLICATION, "test rpc", "rpc message failed");
        final RpcError testRpcError2 = RpcResultBuilder.newWarning(ErrorType.PROTOCOL, "test rpc2", "rpc message failed", "test app tag", "test app info", new Throwable("test rpc warning"));

        assertNotNull("testRpcError shouldn't be null.", testRpcError);
        assertNotNull("testRpcError2 shouldn't be null.", testRpcError2);
    }

    @Test
    public void testWithRpcErrorMethod() {
        final RpcError testRpcError = RpcResultBuilder.newWarning(ErrorType.APPLICATION, "test rpc error", "rpc message failed");
        final RpcResultBuilder<String> testRpcResultBuilder = RpcResultBuilder.<String>status(false)
                .withRpcError(testRpcError)
                .withRpcErrors(null)
                .withError(ErrorType.APPLICATION, "app error")
                .withError(ErrorType.APPLICATION, "operation-failed", "test with rpc")
                .withError(ErrorType.APPLICATION, "test with rpc", new Throwable("Test error"))
                .withError(ErrorType.APPLICATION, "app error", "test with rpc", "operation-failed", "test info", new Throwable("Test error"));

       final RpcResult<String> rpcResult = testRpcResultBuilder.build();
       assertEquals("Count of rpc errors should be '5'.", 5, rpcResult.getErrors().size());
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

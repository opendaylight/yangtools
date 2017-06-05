/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        RpcResult<String> result = RpcResultBuilder.<String>success().withResult("foo").build();
        verifyRpcResult(result, true, "foo");
        assertNotNull("getErrors returned null", result.getErrors());
        assertEquals("getErrors size", 0, result.getErrors().size());

        result = RpcResultBuilder.success("bar").build();
        verifyRpcResult(result, true, "bar");
    }

    @Test
    public void testFailed() {
        Throwable cause = new Throwable("mock cause");
        Throwable cause2 = new Throwable("mock cause2");
        RpcResult<String> result = RpcResultBuilder.<String>failed()
                  .withError(ErrorType.PROTOCOL, "error message 1")
                  .withError(ErrorType.APPLICATION, "lock_denied", "error message 2")
                  .withError(ErrorType.RPC, "in-use", "error message 3", "my-app-tag", "my-info", cause)
                  .withError(ErrorType.TRANSPORT, "error message 4", cause2)
                  .build();
        verifyRpcResult(result, false, null);
        verifyRpcError(result, 0, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message 1", null, null, null);
        verifyRpcError(result, 1, ErrorSeverity.ERROR, ErrorType.APPLICATION, "lock_denied",
                        "error message 2", null, null, null);
        verifyRpcError(result, 2, ErrorSeverity.ERROR, ErrorType.RPC, "in-use",
                        "error message 3", "my-app-tag", "my-info", cause);
        verifyRpcError(result, 3, ErrorSeverity.ERROR, ErrorType.TRANSPORT, "operation-failed",
                        "error message 4", null, null, cause2);
        assertEquals("getErrors size", 4, result.getErrors().size());
    }

    @Test
    public void testWithWarnings() {
        Throwable cause = new Throwable("mock cause");
        RpcResult<String> result = RpcResultBuilder.<String>success()
                  .withWarning(ErrorType.APPLICATION, "lock_denied", "message 1")
                  .withWarning(ErrorType.RPC, "in-use", "message 2", "my-app-tag", "my-info", cause)
                  .build();
        verifyRpcResult(result, true, null);
        verifyRpcError(result, 0, ErrorSeverity.WARNING, ErrorType.APPLICATION, "lock_denied",
                        "message 1", null, null, null);
        verifyRpcError(result, 1, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message 2", "my-app-tag", "my-info", cause);
        assertEquals("getErrors size", 2, result.getErrors().size());
    }

    @Test
    public void testFrom() {
        Throwable cause = new Throwable("mock cause");
        RpcResult<String> result = RpcResultBuilder.<String>success()
                .withResult("foo")
                .withWarning(ErrorType.RPC, "in-use", "message", "my-app-tag", "my-info", cause)
                .build();

        RpcResult<String> copy = RpcResultBuilder.from(result)
                .withError(ErrorType.PROTOCOL, "error message")
                .build();
        verifyRpcResult(copy, true, "foo");
        verifyRpcError(copy, 0, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message", "my-app-tag", "my-info", cause);
        verifyRpcError(copy, 1, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message", null, null, null);
    }

    @Test
    public void testWithRpcErrors() {
        Throwable cause = new Throwable("mock cause");
        RpcResult<String> result = RpcResultBuilder.<String>failed()
                .withWarning(ErrorType.RPC, "in-use", "message", "my-app-tag", "my-info", cause)
                .withError(ErrorType.PROTOCOL, "error message")
                .build();

        RpcResult<String> result2 = RpcResultBuilder.<String>failed()
                .withRpcErrors(result.getErrors())
                .build();
        verifyRpcError(result2, 0, ErrorSeverity.WARNING, ErrorType.RPC, "in-use",
                        "message", "my-app-tag", "my-info", cause);
        verifyRpcError(result2, 1, ErrorSeverity.ERROR, ErrorType.PROTOCOL, "operation-failed",
                        "error message", null, null, null);
    }

    @Test
    public void testErrors() {
        final RpcResultBuilder<Object> rpcResultBuilder = RpcResultBuilder.status(true);
        final RpcError rpcErrorShort = RpcResultBuilder.newError(RpcError.ErrorType.RPC, "tag", "msg");
        final RpcError rpcErrorLong = RpcResultBuilder.newError(RpcError.ErrorType.RPC, "tag", "msg", "applicationTag",
                "info", null);
        final RpcError rpcErrorShortWarn = RpcResultBuilder.newWarning(RpcError.ErrorType.RPC, "tag", "msg");
        final RpcError rpcErrorLongWarn = RpcResultBuilder.newWarning(RpcError.ErrorType.RPC, "tag", "msg",
                "applicationTag",
                "info", null);
        rpcResultBuilder.withRpcError(rpcErrorShort);
        final RpcResult<Object> rpcResult = rpcResultBuilder.build();
        final RpcResultBuilder<RpcResult<Object>> rpcResultRpcResultBuilder1 = RpcResultBuilder.success(
                rpcResultBuilder);
        final RpcResultBuilder<RpcResult<Object>> rpcResultRpcResultBuilder2 = rpcResultRpcResultBuilder1.withResult(
                rpcResultBuilder);

        assertEquals(rpcErrorShort.getErrorType(), rpcErrorShortWarn.getErrorType());
        assertEquals(rpcErrorLong.getErrorType(), rpcErrorLongWarn.getErrorType());
        assertEquals(rpcResultRpcResultBuilder1, rpcResultRpcResultBuilder2);
        assertNotNull(rpcResultBuilder.buildFuture());
        assertEquals("RpcResult [successful=true, result=null, errors=[RpcError [message=msg, severity=ERROR, "
                + "errorType=RPC, tag=tag, applicationTag=null, info=null, cause=null]]]", rpcResult.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialization() throws Exception {
        RpcResult<String> result = RpcResultBuilder.<String>success().withResult("foo").build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(result);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        RpcResult<String> clone = (RpcResult<String>) in.readObject();

        verifyRpcResult(clone, true, "foo");

        Throwable cause = new Throwable("mock cause");
        result = RpcResultBuilder.<String>failed()
                .withError(ErrorType.RPC, "in-use", "error message", "my-app-tag", "my-info", cause)
                .build();

        bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);
        out.writeObject(result);

        in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        clone = (RpcResult<String>) in.readObject();

        verifyRpcResult(clone, false, null);
        verifyRpcError(result, 0, ErrorSeverity.ERROR, ErrorType.RPC, "in-use",
                "error message", "my-app-tag", "my-info", cause);
    }

    void verifyRpcError(final RpcResult<?> result, final int errorIndex, final ErrorSeverity expSeverity,
            final ErrorType expErrorType, final String expTag, final String expMessage, final String expAppTag,
            final String expInfo, final Throwable expCause) {

        List<RpcError> errors = new ArrayList<>(result.getErrors());
        assertTrue("Expected error at index " + errorIndex + " not found",
                    errorIndex < errors.size());
        RpcError error = errors.get(errorIndex);
        assertEquals("getSeverity", expSeverity, error.getSeverity());
        assertEquals("getErrorType", expErrorType, error.getErrorType());
        assertEquals("getTag", expTag, error.getTag());
        assertEquals("getMessage", expMessage, error.getMessage());
        assertEquals("getApplicationTag", expAppTag, error.getApplicationTag());
        assertEquals("getInfo", expInfo, error.getInfo());
        assertEquals("getCause", expCause, error.getCause());
    }

    void verifyRpcResult(final RpcResult<?> result, final boolean expSuccess, final Object expValue) {
        assertEquals("isSuccessful", expSuccess, result.isSuccessful());
        assertEquals("getResult", expValue, result.getResult());
    }
}

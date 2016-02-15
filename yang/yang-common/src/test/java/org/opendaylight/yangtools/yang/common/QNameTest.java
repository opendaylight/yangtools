/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.util.concurrent.ListenableFuture;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.junit.Test;

public class QNameTest {
    private final String namespace = "urn:foo", revision = "2013-12-24", localName = "bar";
    private final URI ns;

    public QNameTest() throws Exception {
        this.ns = new URI(namespace);
    }

    @Test
    public void testStringSerialization() throws Exception {
        {
            QName qName = QName.create(namespace, revision, localName);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + namespace + QName.QNAME_REVISION_DELIMITER
                    + revision + QName.QNAME_RIGHT_PARENTHESIS + localName, qName.toString());
            QName copied = QName.create(qName.toString());
            assertEquals(qName, copied);
        }
        // no revision
        {
            QName qName = new QName(ns, localName);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + namespace + QName.QNAME_RIGHT_PARENTHESIS
                    + localName, qName.toString());
            QName copied = QName.create(qName.toString());
            assertEquals(qName, copied);
        }
        // no namespace nor revision
        {
            QName qName = new QName((URI) null, localName);
            assertEquals(localName, qName.toString());
            QName copied = QName.create(qName.toString());
            assertEquals(qName, copied);
        }
    }

    @Test
    public void testIllegalLocalNames() {
        assertLocalNameFails(null);
        assertLocalNameFails("");
        assertLocalNameFails("(");
        assertLocalNameFails(")");
        assertLocalNameFails("?");
        assertLocalNameFails("&");
    }

    @Test
    public void testCompareTo() throws Exception {
        String A = "a";
        String B = "b";

        QName a = QName.create(A);
        QName b = QName.create(A);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);

        // compare with localName
        a = QName.create(A);
        b = QName.create(B);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with namespace
        a = QName.create(A, revision, A);
        b = QName.create(B, revision, A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with 1 null namespace
        a = QName.create(null, QName.parseRevision(revision), A);
        b = QName.create(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with both null namespace
        b = QName.create(null, QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);

        // compare with revision
        a = QName.create(A, "2013-12-24", A);
        b = QName.create(A, "2013-12-25", A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with 1 null revision
        a = QName.create(URI.create(A), null, A);
        b = QName.create(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with both null revision
        b = QName.create(URI.create(A), null, A);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);
    }

    @Test
    public void testQName() {
        final QName qName = QName.create(namespace, revision, localName);
        final QName qName1 = QName.create(namespace, localName);
        final QName qName2 = QName.create(qName1, localName);
        assertEquals(qName1, qName.withoutRevision());
        assertEquals(qName1, qName2);
        assertTrue(qName.isEqualWithoutRevision(qName1));
        assertNotNull(QName.formattedRevision(new Date()));
        assertNotNull(qName.hashCode());
        assertEquals(qName, qName.intern());
    }

    @Test
    public void testQNameModule() {
        final QNameModule qNameModule = QNameModule.create(ns, new Date());
        assertNotNull(qNameModule.toString());
        assertNotNull(qNameModule.getRevisionNamespace());
    }

    @Test
    public void testYangConstants() {
        final URI uriYang = YangConstants.RFC6020_YANG_NAMESPACE;
        final URI uriYin = YangConstants.RFC6020_YIN_NAMESPACE;
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:1"), uriYang);
        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:yin:1"), uriYin);
        assertEquals(QNameModule.create(uriYang, null).intern(), YangConstants.RFC6020_YANG_MODULE);
        assertEquals(QNameModule.create(uriYin, null).intern(), YangConstants.RFC6020_YIN_MODULE);
    }

    @Test
    public void testRpcResultBuilder () {
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
        final RpcResultBuilder<RpcResult<Object>> rpcResultRpcResultBuilder1 = RpcResultBuilder.success
                (rpcResultBuilder);
        final RpcResultBuilder<RpcResult<Object>> rpcResultRpcResultBuilder2 = rpcResultRpcResultBuilder1.withResult
                (rpcResultBuilder);

        assertEquals(rpcErrorShort.getErrorType(), rpcErrorShortWarn.getErrorType());
        assertEquals(rpcErrorLong.getErrorType(), rpcErrorLongWarn.getErrorType());
        assertEquals(rpcResultRpcResultBuilder1, rpcResultRpcResultBuilder2);
        assertTrue(rpcResultBuilder.buildFuture() instanceof ListenableFuture);
        assertEquals("RpcResult [successful=true, result=null, errors=[RpcError [message=msg, severity=ERROR, " +
                "errorType=RPC, tag=tag, applicationTag=null, info=null, cause=null]]]", rpcResult.toString());
    }

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

    private void assertLocalNameFails(final String localName) {
        try {
            new QName((URI)null, localName);
            fail("Local name should fail:" + localName);
        } catch (IllegalArgumentException e) {
        }
    }
}

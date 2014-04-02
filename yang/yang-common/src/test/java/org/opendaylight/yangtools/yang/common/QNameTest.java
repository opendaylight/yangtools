/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

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
        a = new QName(null, QName.parseRevision(revision), A);
        b = new QName(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with both null namespace
        b = new QName(null, QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);

        // compare with revision
        a = QName.create(A, "2013-12-24", A);
        b = QName.create(A, "2013-12-25", A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with 1 null revision
        a = new QName(URI.create(A), null, A);
        b = new QName(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);

        // compare with both null revision
        b = new QName(URI.create(A), null, A);
        assertTrue(a.compareTo(b) == 0);
        assertTrue(b.compareTo(a) == 0);
    }

    private void assertLocalNameFails(String localName) {
        try {
            new QName((URI)null, localName);
            fail("Local name should fail:" + localName);
        } catch (IllegalArgumentException e) {

        }
    }

}

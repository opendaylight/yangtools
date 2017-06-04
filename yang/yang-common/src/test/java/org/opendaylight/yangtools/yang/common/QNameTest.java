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

import java.net.URI;
import java.util.Date;
import org.junit.Test;

public class QNameTest {
    private final String namespace = "urn:foo";
    private final String revision = "2013-12-24";
    private final String localName = "bar";
    private final URI ns = URI.create(namespace);

    @Test
    public void testStringSerialization() throws Exception {
        {
            QName qname = QName.create(namespace, revision, localName);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + namespace + QName.QNAME_REVISION_DELIMITER
                    + revision + QName.QNAME_RIGHT_PARENTHESIS + localName, qname.toString());
            QName copied = QName.create(qname.toString());
            assertEquals(qname, copied);
        }
        // no revision
        {
            QName qname = new QName(ns, localName);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + namespace + QName.QNAME_RIGHT_PARENTHESIS
                    + localName, qname.toString());
            QName copied = QName.create(qname.toString());
            assertEquals(qname, copied);
        }
        // no namespace nor revision
        {
            QName qname = new QName(null, localName);
            assertEquals(localName, qname.toString());
            QName copied = QName.create(qname.toString());
            assertEquals(qname, copied);
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
        final String A = "a";
        final String B = "b";

        QName qa = QName.create(A);
        QName qb = QName.create(A);
        assertTrue(qa.compareTo(qb) == 0);
        assertTrue(qb.compareTo(qa) == 0);

        // compare with localName
        qa = QName.create(A);
        qb = QName.create(B);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with namespace
        qa = QName.create(A, revision, A);
        qb = QName.create(B, revision, A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with 1 null namespace
        qa = QName.create(null, QName.parseRevision(revision), A);
        qb = QName.create(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with both null namespace
        qb = QName.create(null, QName.parseRevision(revision), A);
        assertTrue(qa.compareTo(qb) == 0);
        assertTrue(qb.compareTo(qa) == 0);

        // compare with revision
        qa = QName.create(A, "2013-12-24", A);
        qb = QName.create(A, "2013-12-25", A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with 1 null revision
        qa = QName.create(URI.create(A), null, A);
        qb = QName.create(URI.create(A), QName.parseRevision(revision), A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with both null revision
        qb = QName.create(URI.create(A), null, A);
        assertTrue(qa.compareTo(qb) == 0);
        assertTrue(qb.compareTo(qa) == 0);
    }

    @Test
    public void testQName() {
        final QName qname = QName.create(namespace, revision, localName);
        final QName qname1 = QName.create(namespace, localName);
        final QName qname2 = QName.create(qname1, localName);
        assertEquals(qname1, qname.withoutRevision());
        assertEquals(qname1, qname2);
        assertTrue(qname.isEqualWithoutRevision(qname1));
        assertNotNull(QName.formattedRevision(new Date()));
        assertNotNull(qname.hashCode());
        assertEquals(qname, qname.intern());
    }

    @Test
    public void testQNameModule() {
        final QNameModule qnameModule = QNameModule.create(ns, new Date());
        assertNotNull(qnameModule.toString());
        assertNotNull(qnameModule.getRevisionNamespace());
    }

    private static void assertLocalNameFails(final String localName) {
        try {
            new QName(null, localName);
            fail("Local name should fail:" + localName);
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}

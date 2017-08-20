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
    private static final String NAMESPACE = "urn:foo";
    private static final String REVISION = "2013-12-24";
    private static final String LOCALNAME = "bar";
    private static final URI NS = URI.create(NAMESPACE);

    @Test
    public void testStringSerialization() throws Exception {
        {
            QName qname = QName.create(NAMESPACE, REVISION, LOCALNAME);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + NAMESPACE + QName.QNAME_REVISION_DELIMITER
                    + REVISION + QName.QNAME_RIGHT_PARENTHESIS + LOCALNAME, qname.toString());
            QName copied = QName.create(qname.toString());
            assertEquals(qname, copied);
        }
        // no revision
        {
            QName qname = new QName(NS, LOCALNAME);
            assertEquals(QName.QNAME_LEFT_PARENTHESIS + NAMESPACE + QName.QNAME_RIGHT_PARENTHESIS
                    + LOCALNAME, qname.toString());
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

        // compare with namespace
        QName qa = QName.create(A, REVISION, A);
        QName qb = QName.create(B, REVISION, A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with revision
        qa = QName.create(A, "2013-12-24", A);
        qb = QName.create(A, "2013-12-25", A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with 1 null revision
        qa = QName.create(URI.create(A), null, A);
        qb = QName.create(URI.create(A), QName.parseRevision(REVISION), A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with both null revision
        qb = QName.create(URI.create(A), null, A);
        assertTrue(qa.compareTo(qb) == 0);
        assertTrue(qb.compareTo(qa) == 0);
    }

    @Test
    public void testQName() {
        final QName qname = QName.create(NAMESPACE, REVISION, LOCALNAME);
        final QName qname1 = QName.create(NAMESPACE, LOCALNAME);
        final QName qname2 = QName.create(qname1, LOCALNAME);
        assertEquals(qname1, qname.withoutRevision());
        assertEquals(qname1, qname2);
        assertTrue(qname.isEqualWithoutRevision(qname1));
        assertNotNull(qname.hashCode());
        assertEquals(qname, qname.intern());
    }

    @Test
    public void testQNameModule() {
        final QNameModule qnameModule = QNameModule.create(NS, Revision.forDate(new Date()));
        assertNotNull(qnameModule.toString());
        assertNotNull(qnameModule.getRevisionNamespace());
    }

    private static void assertLocalNameFails(final String localName) {
        try {
            new QName(NS, localName);
            fail("Local name should fail:" + localName);
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}

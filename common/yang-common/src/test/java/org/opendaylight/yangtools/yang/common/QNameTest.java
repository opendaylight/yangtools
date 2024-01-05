/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class QNameTest {
    private static final String NAMESPACE = "urn:foo";
    private static final String REVISION = "2013-12-24";
    private static final String LOCALNAME = "bar";
    private static final XMLNamespace NS = XMLNamespace.of(NAMESPACE);

    @Test
    public void testStringSerialization() throws Exception {
        var qname = QName.create(NAMESPACE, REVISION, LOCALNAME);
        assertEquals("(urn:foo?revision=2013-12-24)bar", qname.toString());
        assertEquals(qname, QName.create(qname.toString()));
    }

    @Test
    public void testStringSerializationNoRevision() throws Exception {
        // no revision
        var qname = QName.create(NS, LOCALNAME);
        assertEquals("(urn:foo)bar", qname.toString());
        assertEquals(qname, QName.create(qname.toString()));
    }

    @Test
    public void testIllegalLocalNames() {
        assertThrows(NullPointerException.class, () -> QName.create(NS, null));
        assertThrows(IllegalArgumentException.class, () -> QName.create(NS, ""));
        assertThrows(IllegalArgumentException.class, () -> QName.create(NS, "("));
        assertThrows(IllegalArgumentException.class, () -> QName.create(NS, ")"));
        assertThrows(IllegalArgumentException.class, () -> QName.create(NS, "?"));
        assertThrows(IllegalArgumentException.class, () -> QName.create(NS, "&"));
    }

    @Test
    public void testCompareTo() throws Exception {
        final String A = "a";
        final String B = "b";

        // compare with namespace
        var qa = QName.create(A, REVISION, A);
        var qb = QName.create(B, REVISION, A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with revision
        qa = QName.create(A, "2013-12-24", A);
        qb = QName.create(A, "2013-12-25", A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with 1 null revision
        qa = QName.create(XMLNamespace.of(A), A);
        qb = QName.create(XMLNamespace.of(A), Revision.of(REVISION), A);
        assertTrue(qa.compareTo(qb) < 0);
        assertTrue(qb.compareTo(qa) > 0);

        // compare with both null revision
        qb = QName.create(XMLNamespace.of(A), A);
        assertTrue(qa.compareTo(qb) == 0);
        assertTrue(qb.compareTo(qa) == 0);
    }

    @Test
    public void testQName() {
        final var qname = QName.create(NAMESPACE, REVISION, LOCALNAME);
        final var qname1 = QName.create(NAMESPACE, LOCALNAME);
        final var qname2 = QName.create(qname1, LOCALNAME);
        assertEquals(qname1, qname.withoutRevision());
        assertEquals(qname1, qname2);
        assertTrue(qname.isEqualWithoutRevision(qname1));
        assertEquals("2000-01-01", QName.formattedRevision(Revision.ofNullable("2000-01-01")));
        assertEquals(qname, qname.intern());
    }

    @Test
    public void testQNameModule() throws URISyntaxException {
        final var qnameModule = QNameModule.of(NS, Revision.of("2000-01-01"));
        assertEquals("QNameModule{ns=urn:foo, rev=2000-01-01}", qnameModule.toString());
    }
}

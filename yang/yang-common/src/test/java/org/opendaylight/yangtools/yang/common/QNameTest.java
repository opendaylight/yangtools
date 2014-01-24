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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    private void assertLocalNameFails(String localName) {
        try {
            new QName((URI)null, localName);
            fail("Local name should fail:" + localName);
        } catch (IllegalArgumentException e) {

        }
    }

}

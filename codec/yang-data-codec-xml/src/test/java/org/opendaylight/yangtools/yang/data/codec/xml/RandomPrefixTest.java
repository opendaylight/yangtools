/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

class RandomPrefixTest {
    static final int MAX_COUNTER = 4000;

    @Test
    void testEncodeDecode() {
        final var allGenerated = new ArrayList<>(MAX_COUNTER);
        for (int i = 0; i < MAX_COUNTER; i++) {
            final var encoded = RandomPrefix.encode(i);
            assertEquals(RandomPrefix.decode(encoded), i);
            allGenerated.add(encoded);
        }

        assertEquals(allGenerated.size(), MAX_COUNTER);
        assertEquals("dPT", allGenerated.get(MAX_COUNTER - 1));
        assertEquals("a", allGenerated.get(0));
        assertEquals(allGenerated.size(), new HashSet<>(allGenerated).size());
    }

    @Test
    void testQNameWithPrefix() {
        final var a = new RandomPrefix(null);

        final var allGenerated = new ArrayList<String>();
        for (int i = 0; i < MAX_COUNTER; i++) {
            final var prefix = RandomPrefix.encode(i);
            final var uri = XMLNamespace.of("localhost:" + prefix);
            final var qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
            allGenerated.add(a.encodePrefix(qname.getNamespace()));
        }

        assertEquals(MAX_COUNTER, allGenerated.size());
        // We are generating MAX_COUNTER_VALUE + 27 prefixes total, so we should encounter a reset in prefix a start
        // from 0 at some point. At the end, there should be only 27 values in RandomPrefix cache
        assertEquals(MAX_COUNTER, Iterables.size(a.getPrefixes()));
        assertThat(allGenerated, not(hasItem("xml")));
        assertThat(allGenerated, not(hasItem("xmla")));
        assertThat(allGenerated, not(hasItem("xmlz")));

        assertEquals(1, Iterables.frequency(allGenerated, "a"));
    }

    @Test
    void test2QNames1Namespace() {
        final var a = new RandomPrefix(null);

        final var uri = XMLNamespace.of("localhost");
        final var qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
        final var qname2 = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");

        assertEquals(a.encodePrefix(qname.getNamespace()), a.encodePrefix(qname2.getNamespace()));
    }

    @Test
    void testQNameNoPrefix() {
        final var a = new RandomPrefix(null);

        final var uri = XMLNamespace.of("localhost");
        var qname = QName.create(uri, Revision.of("2000-01-01"), "local-name");
        assertEquals("a", a.encodePrefix(qname.getNamespace()));
        qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
        assertEquals("a", a.encodePrefix(qname.getNamespace()));
        qname = QName.create(QNameModule.create(XMLNamespace.of("second"), Revision.of("2000-01-01")), "local-name");
        assertEquals("b", a.encodePrefix(qname.getNamespace()));
    }
}

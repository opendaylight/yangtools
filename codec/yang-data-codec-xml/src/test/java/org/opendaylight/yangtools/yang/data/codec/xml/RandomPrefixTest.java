/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class RandomPrefixTest {
    static final int MAX_COUNTER = 4000;

    @Test
    public void testEncodeDecode() throws Exception {

        final List<String> allGenerated = new ArrayList<>(MAX_COUNTER);
        for (int i = 0; i < MAX_COUNTER; i++) {
            final String encoded = RandomPrefix.encode(i);
            assertEquals(RandomPrefix.decode(encoded), i);
            allGenerated.add(encoded);
        }

        assertEquals(allGenerated.size(), MAX_COUNTER);
        assertEquals("dPT", allGenerated.get(MAX_COUNTER - 1));
        assertEquals("a", allGenerated.get(0));
        assertEquals(allGenerated.size(), new HashSet<>(allGenerated).size());
    }

    @Test
    public void testQNameWithPrefix() {
        final RandomPrefix a = new RandomPrefix(null);

        final List<String> allGenerated = new ArrayList<>();
        for (int i = 0; i < MAX_COUNTER; i++) {
            final String prefix = RandomPrefix.encode(i);
            final XMLNamespace uri = XMLNamespace.of("localhost:" + prefix);
            final QName qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
            allGenerated.add(a.encodePrefix(qname.getNamespace()));
        }

        assertEquals(MAX_COUNTER, allGenerated.size());
        // We are generating MAX_COUNTER_VALUE + 27 prefixes total, so we should encounter a reset in prefix a start
        // from 0 at some point. At the end, there should be only 27 values in RandomPrefix cache
        assertEquals(MAX_COUNTER, Iterables.size(a.getPrefixes()));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xml")));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xmla")));
        assertThat(allGenerated, CoreMatchers.not(CoreMatchers.hasItem("xmlz")));

        assertEquals(1, Iterables.frequency(allGenerated, "a"));
    }

    @Test
    public void test2QNames1Namespace() throws Exception {
        final RandomPrefix a = new RandomPrefix(null);

        final XMLNamespace uri = XMLNamespace.of("localhost");
        final QName qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
        final QName qname2 = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");

        assertEquals(a.encodePrefix(qname.getNamespace()), a.encodePrefix(qname2.getNamespace()));
    }

    @Test
    public void testQNameNoPrefix() throws Exception {
        final RandomPrefix a = new RandomPrefix(null);

        final XMLNamespace uri = XMLNamespace.of("localhost");
        QName qname = QName.create(uri, Revision.of("2000-01-01"), "local-name");
        assertEquals("a", a.encodePrefix(qname.getNamespace()));
        qname = QName.create(QNameModule.create(uri, Revision.of("2000-01-01")), "local-name");
        assertEquals("a", a.encodePrefix(qname.getNamespace()));
        qname = QName.create(QNameModule.create(XMLNamespace.of("second"), Revision.of("2000-01-01")), "local-name");
        assertEquals("b", a.encodePrefix(qname.getNamespace()));

    }
}

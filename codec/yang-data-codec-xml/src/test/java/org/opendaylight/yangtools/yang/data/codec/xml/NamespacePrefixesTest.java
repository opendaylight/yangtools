/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

class NamespacePrefixesTest {
    static final int MAX_COUNTER = 4000;

    @Test
    void testEncodeDecode() {
        final var allGenerated = new ArrayList<>(MAX_COUNTER);
        for (int i = 0; i < MAX_COUNTER; i++) {
            final var encoded = NamespacePrefixes.encode(i);
            assertDecodesTo(i, encoded);
            allGenerated.add(encoded);
        }

        assertEquals(MAX_COUNTER, allGenerated.size());
        assertEquals("dPT", allGenerated.get(MAX_COUNTER - 1));
        assertEquals("a", allGenerated.get(0));
        assertEquals(allGenerated.size(), new HashSet<>(allGenerated).size());
    }

    @Test
    void testQNameWithPrefix() {
        final var a = new NamespacePrefixes(null, null);

        final var allGenerated = new ArrayList<String>();
        for (int i = 0; i < MAX_COUNTER; i++) {
            allGenerated.add(a.encodePrefix(XMLNamespace.of("localhost:" + NamespacePrefixes.encode(i))));
        }

        assertEquals(MAX_COUNTER, allGenerated.size());
        // We are generating MAX_COUNTER_VALUE + 27 prefixes total, so we should encounter a reset in prefix a start
        // from 0 at some point. At the end, there should be only 27 values in RandomPrefix cache
        assertEquals(MAX_COUNTER, a.emittedPrefixes().size());
        assertThat(allGenerated).doesNotContain("xml");
        assertThat(allGenerated).doesNotContain("xmla");
        assertThat(allGenerated).doesNotContain("xmlz");

        assertEquals(1, Iterables.frequency(allGenerated, "a"));
    }

    @Test
    void test2QNames1Namespace() {
        final var a = new NamespacePrefixes(null, null);

        final var uri = XMLNamespace.of("localhost");

        assertEquals(a.encodePrefix(uri), a.encodePrefix(uri));
        assertEquals(List.of(Map.entry(uri, "a")), a.emittedPrefixes());
    }

    @Test
    void testQNameNoPrefix() {
        final var a = new NamespacePrefixes(null, new PreferredPrefixes.Precomputed(Map.of()));

        final var uri = XMLNamespace.of("localhost");
        final var second = XMLNamespace.of("second");
        assertEquals("a", a.encodePrefix(uri));
        assertEquals("a", a.encodePrefix(uri));
        assertEquals("b", a.encodePrefix(second));
        assertEquals(List.of(Map.entry(uri, "a"), Map.entry(second, "b")), a.emittedPrefixes());
    }

    private static void assertDecodesTo(final int expected, final String str) {
        int actual = 0;
        for (char c : str.toCharArray()) {
            int idx = NamespacePrefixes.LOOKUP.indexOf(c);
            assertNotEquals(-1, idx, () -> "Invalid string " + str);
            actual = (actual << NamespacePrefixes.SHIFT) + idx;
        }
        assertEquals(expected, actual);
    }
}

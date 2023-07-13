/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapAdaptorTest {
    private MapAdaptor adaptor;

    @BeforeEach
    void setUp() {
        adaptor = MapAdaptor.getInstance(true, 10, 5);
    }

    @Test
    void testTreeToEmpty() {
        final var input = new TreeMap<>();

        // Converts the input into a hashmap;
        final var snap = adaptor.takeSnapshot(input);
        assertNotSame(input, snap);
        assertInstanceOf(HashMap.class, snap);

        final var opt1 = adaptor.optimize(input);
        assertSame(ImmutableMap.of(), opt1);

        final var opt2 = adaptor.optimize(snap);
        assertSame(ImmutableMap.of(), opt2);
    }

    @Test
    void testTreeToSingleton() {
        final var input = new TreeMap<>();
        input.put("a", "b");

        final var snap = adaptor.takeSnapshot(input);
        assertNotSame(input, snap);
        assertInstanceOf(HashMap.class, snap);
        assertEquals(input, snap);

        final var opt1 = adaptor.optimize(input);
        assertNotSame(input, opt1);
        assertEquals(input, opt1);
        assertEquals(Collections.singletonMap(null, null).getClass(), opt1.getClass());
        final var snap1 = adaptor.takeSnapshot(opt1);
        assertInstanceOf(HashMap.class, snap1);
        assertEquals(input, snap1);

        final var opt2 = adaptor.optimize(snap);
        assertNotSame(snap, opt2);
        assertEquals(input, opt2);
        assertEquals(Collections.singletonMap(null, null).getClass(), opt2.getClass());

        final var snap2 = adaptor.takeSnapshot(opt2);
        assertNotSame(opt2, snap2);
        assertInstanceOf(HashMap.class, snap2);
        assertEquals(input, snap2);
    }

    @Test
    void testTreeToTrie() {
        final var input = new TreeMap<String, String>();
        for (var c = 'a'; c <= 'z'; ++c) {
            final var s = String.valueOf(c);
            input.put(s, s);
        }

        final var snap = adaptor.takeSnapshot(input);
        assertInstanceOf(HashMap.class, snap);
        assertEquals(input, snap);

        final var opt1 = adaptor.optimize(input);
        assertEquals(input, opt1);
        assertEquals(ReadOnlyTrieMap.class, opt1.getClass());

        final var snap2 = adaptor.takeSnapshot(opt1);
        assertInstanceOf(ReadWriteTrieMap.class, snap2);
        assertEquals(opt1, snap2);
        assertEquals(26, snap2.size());

        // snap2 and snap3 are independent
        final var snap3 = adaptor.takeSnapshot(opt1);

        snap2.remove("a");
        assertEquals(25, snap2.size());
        assertEquals(26, snap3.size());

        snap3.remove("b");
        snap3.remove("c");
        assertEquals(25, snap2.size());
        assertEquals(24, snap3.size());

        snap2.put("foo", "foo");
        snap2.put("bar", "baz");
        snap3.put("bar", "baz");
        assertEquals(27, snap2.size());
        assertEquals(25, snap3.size());
    }

    @Test
    void testTrieToHash() {
        final var input = new TreeMap<String, String>();
        for (char c = 'a'; c <= 'k'; ++c) {
            final String s = String.valueOf(c);
            input.put(s, s);
        }

        // Translated to read-only
        final var opt1 = adaptor.optimize(input);
        assertEquals(input, opt1);
        assertEquals(ReadOnlyTrieMap.class, opt1.getClass());
        assertEquals(11, opt1.size());

        // 11 elements -- should retain TrieMap
        final var snap1 = adaptor.takeSnapshot(opt1);
        assertEquals(ReadWriteTrieMap.class, snap1.getClass());
        assertEquals(11, snap1.size());

        for (var c = 'e'; c <= 'k'; ++c) {
            final var s = String.valueOf(c);
            snap1.remove(s);
        }

        // 4 elements: should revert to HashMap
        assertEquals(4, snap1.size());

        final var opt2 = adaptor.optimize(snap1);
        assertEquals(snap1, opt2);
        assertEquals(HashMap.class, opt2.getClass());
        assertEquals(4, opt2.size());
    }
}

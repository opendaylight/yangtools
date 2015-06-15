/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;

public class MapAdaptorTest {
    private MapAdaptor adaptor;

    @Before
    public void setUp() {
        adaptor = MapAdaptor.getInstance(true, 10, 5);
    }

    @Test
    public void testTreeToEmpty() {
        final Map<String, String> input = new TreeMap<>();

        // Converts the input into a hashmap;
        final Map<?, ?> snap = adaptor.takeSnapshot(input);
        assertNotSame(input, snap);
        assertTrue(snap instanceof HashMap);

        final Map<?, ?> opt1 = adaptor.optimize(input);
        assertSame(ImmutableMap.of(), opt1);

        final Map<?, ?> opt2 = adaptor.optimize(snap);
        assertSame(ImmutableMap.of(), opt2);
    }

    @Test
    public void testTreeToSingleton() {
        final Map<String, String> input = new TreeMap<>();
        input.put("a", "b");

        final Map<?, ?> snap = adaptor.takeSnapshot(input);
        assertNotSame(input, snap);
        assertTrue(snap instanceof HashMap);
        assertEquals(input, snap);

        final Map<?, ?> opt1 = adaptor.optimize(input);
        assertNotSame(input, opt1);
        assertEquals(input, opt1);
        assertEquals(Collections.singletonMap(null, null).getClass(), opt1.getClass());
        final Map<?, ?> snap1 = adaptor.takeSnapshot(opt1);
        assertTrue(snap1 instanceof HashMap);
        assertEquals(input, snap1);

        final Map<?, ?> opt2 = adaptor.optimize(snap);
        assertNotSame(snap, opt2);
        assertEquals(input, opt2);
        assertEquals(Collections.singletonMap(null, null).getClass(), opt2.getClass());

        final Map<?, ?> snap2 = adaptor.takeSnapshot(opt2);
        assertNotSame(opt2, snap2);
        assertTrue(snap2 instanceof HashMap);
        assertEquals(input, snap2);
    }

    @Test
    public void testTreeToTrie() {
        final Map<String, String> input = new TreeMap<>();
        for (char c = 'a'; c <= 'z'; ++c) {
            final String s = String.valueOf(c);
            input.put(s, s);
        }

        final Map<String, String> snap = adaptor.takeSnapshot(input);
        assertTrue(snap instanceof HashMap);
        assertEquals(input, snap);

        final Map<String, String> opt1 = adaptor.optimize(input);
        assertEquals(input, opt1);
        assertEquals(ReadOnlyTrieMap.class, opt1.getClass());

        final Map<String, String> snap2 = adaptor.takeSnapshot(opt1);
        assertTrue(snap2 instanceof ReadWriteTrieMap);
        assertEquals(opt1, snap2);
        assertEquals(26, snap2.size());

        // snap2 and snap3 are independent
        final Map<String, String> snap3 = adaptor.takeSnapshot(opt1);

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
    public void testTrieToHash() {
        final Map<String, String> input = new TreeMap<>();
        for (char c = 'a'; c <= 'k'; ++c) {
            final String s = String.valueOf(c);
            input.put(s, s);
        }

        // Translated to read-only
        final Map<String, String> opt1 = adaptor.optimize(input);
        assertEquals(input, opt1);
        assertEquals(ReadOnlyTrieMap.class, opt1.getClass());
        assertEquals(11, opt1.size());

        // 11 elements -- should retain TrieMap
        final Map<String, String> snap1 = adaptor.takeSnapshot(opt1);
        assertEquals(ReadWriteTrieMap.class, snap1.getClass());
        assertEquals(11, snap1.size());

        for (char c = 'e'; c <= 'k'; ++c) {
            final String s = String.valueOf(c);
            snap1.remove(s);
        }

        // 4 elements: should revert to HashMap
        assertEquals(4, snap1.size());

        final Map<String, String> opt2 = adaptor.optimize(snap1);
        assertEquals(snap1, opt2);
        assertEquals(HashMap.class, opt2.getClass());
        assertEquals(4, opt2.size());
    }
}

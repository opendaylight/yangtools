/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class ImmutableMapTemplateTest {
    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String BAZ = "baz";
    private static final Set<String> ONE_KEYSET = ImmutableSet.of(FOO);
    private static final Set<String> TWO_KEYSET = ImmutableSet.of(FOO, BAR);

    @Test
    public void testEmpty() {
        ImmutableMapTemplate<?> template;
        try {
            template = ImmutableMapTemplate.ordered(ImmutableList.of());
            fail("Returned template " + template);
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            template = ImmutableMapTemplate.unordered(ImmutableList.of());
            fail("Returned template " + template);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testOneKeyTemplate() {
        assertOne(ImmutableMapTemplate.ordered(ONE_KEYSET), SharedSingletonMap.Ordered.class);
        assertOne(ImmutableMapTemplate.unordered(ONE_KEYSET), SharedSingletonMap.Unordered.class);
    }

    @Test
    public void testTwoKeyTemplate() {
        assertTwo(ImmutableMapTemplate.ordered(TWO_KEYSET), ImmutableOffsetMap.Ordered.class);
        assertTwo(ImmutableMapTemplate.unordered(TWO_KEYSET), ImmutableOffsetMap.Unordered.class);
    }

    private static void assertOne(final ImmutableMapTemplate<String> template, final Class<?> mapClass) {
        assertEquals(ONE_KEYSET, template.keySet());
        assertEquals(mapClass.getSimpleName() + "{keySet=[foo]}", template.toString());

        // Successful instantiation
        Map<String, String> map = template.instantiateWithValues(BAR);
        assertTrue(mapClass.isInstance(map));
        assertEquals(ImmutableMap.of(FOO, BAR), map);
        assertEquals("{foo=bar}", map.toString());

        map = template.instantiateTransformed(ImmutableMap.of(FOO, BAR), (key, value) -> key);
        assertTrue(mapClass.isInstance(map));
        assertEquals(ImmutableMap.of(FOO, FOO), map);
        assertEquals("{foo=foo}", map.toString());

        // Null transformation
        try {
            map = template.instantiateTransformed(ImmutableMap.of(FOO, BAR), (key, value) -> null);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Empty input
        try {
            map = template.instantiateWithValues();
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            map = template.instantiateTransformed(ImmutableMap.of(), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Two-item input
        try {
            map = template.instantiateWithValues(FOO, BAR);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            map = template.instantiateTransformed(ImmutableMap.of(FOO, FOO, BAR, BAR), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Mismatched input
        try {
            map = template.instantiateTransformed(ImmutableMap.of(BAR, FOO), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private static void assertTwo(final ImmutableMapTemplate<String> template, final Class<?> mapClass) {
        assertEquals(TWO_KEYSET, template.keySet());
        assertEquals(mapClass.getSimpleName() + "{offsets={foo=0, bar=1}}", template.toString());

        // Successful instantiation
        Map<String, String> map = template.instantiateWithValues(BAR, FOO);
        assertTrue(mapClass.isInstance(map));
        assertEquals(ImmutableMap.of(FOO, BAR, BAR, FOO), map);
        assertEquals("{foo=bar, bar=foo}", map.toString());

        map = template.instantiateTransformed(ImmutableMap.of(FOO, BAR, BAR, FOO), (key, value) -> key);
        assertTrue(mapClass.isInstance(map));
        assertEquals(ImmutableMap.of(FOO, FOO, BAR, BAR), map);
        assertEquals("{foo=foo, bar=bar}", map.toString());

        // Empty input
        try {
            map = template.instantiateWithValues();
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            map = template.instantiateTransformed(ImmutableMap.of(), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }

        // One-item input
        try {
            map = template.instantiateWithValues(FOO);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            map = template.instantiateTransformed(ImmutableMap.of(FOO, BAR), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Mismatched input
        try {
            map = template.instantiateTransformed(ImmutableMap.of(FOO, BAR, BAZ, FOO), (key, value) -> key);
            fail("Returned map " + map);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}

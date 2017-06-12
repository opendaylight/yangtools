/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class OffsetMapTest {
    private final Map<String, String> twoEntryMap = ImmutableMap.of("k1", "v1", "k2", "v2");
    private final Map<String, String> threeEntryMap = ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3");

    private ImmutableOffsetMap<String, String> createMap() {
        return (ImmutableOffsetMap<String, String>) ImmutableOffsetMap.orderedCopyOf(twoEntryMap);
    }

    private ImmutableOffsetMap<String, String> unorderedMap() {
        return (ImmutableOffsetMap<String, String>) ImmutableOffsetMap.unorderedCopyOf(twoEntryMap);
    }

    @Before
    public void setup() {
        OffsetMapCache.invalidateCache();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongImmutableConstruction() {
        new ImmutableOffsetMap.Ordered<>(Collections.<String, Integer>emptyMap(), new String[1]);
    }

    @Test
    public void testCopyEmptyMap() {
        final Map<String, String> source = Collections.emptyMap();
        final Map<String, String> result = ImmutableOffsetMap.orderedCopyOf(source);

        assertEquals(source, result);
        assertTrue(result instanceof ImmutableMap);
    }

    @Test
    public void testCopySingletonMap() {
        final Map<String, String> source = Collections.singletonMap("a", "b");
        final Map<String, String> result = ImmutableOffsetMap.orderedCopyOf(source);

        assertEquals(source, result);
        assertTrue(result instanceof SharedSingletonMap);
    }

    @Test
    public void testCopyMap() {
        final ImmutableOffsetMap<String, String> map = createMap();

        // Equality in both directions
        assertEquals(twoEntryMap, map);
        assertEquals(map, twoEntryMap);

        // hashcode has to match
        assertEquals(twoEntryMap.hashCode(), map.hashCode());

        // Iterator order needs to be preserved
        assertTrue(Iterators.elementsEqual(twoEntryMap.entrySet().iterator(), map.entrySet().iterator()));

        // Should result in the same object
        assertSame(map, ImmutableOffsetMap.orderedCopyOf(map));

        final Map<String, String> mutable = map.toModifiableMap();
        final Map<String, String> copy = ImmutableOffsetMap.orderedCopyOf(mutable);

        assertEquals(mutable, copy);
        assertEquals(map, copy);
        assertNotSame(mutable, copy);
        assertNotSame(map, copy);
    }

    @Test
    public void testImmutableSimpleEquals() {
        final Map<String, String> map = createMap();

        assertTrue(map.equals(map));
        assertFalse(map.equals(null));
        assertFalse(map.equals("string"));
    }

    @Test
    public void testImmutableGet() {
        final Map<String, String> map = createMap();

        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertNull(map.get("non-existent"));
        assertNull(map.get(null));
    }

    @Test
    public void testImmutableGuards() {
        final Map<String, String> map = createMap();

        try {
            map.values().add("v1");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.values().remove("v1");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.values().clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            final Iterator<String> it = map.values().iterator();
            it.next();
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.keySet().add("k1");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.keySet().clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.keySet().remove("k1");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            final Iterator<String> it = map.keySet().iterator();
            it.next();
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.entrySet().clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.entrySet().add(new SimpleEntry<>("k1", "v1"));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.entrySet().remove(new SimpleEntry<>("k1", "v1"));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            final Iterator<Entry<String, String>> it = map.entrySet().iterator();
            it.next();
            it.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.put("k1", "fail");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.putAll(ImmutableMap.of("k1", "fail"));
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }

        try {
            map.remove("k1");
            fail();
        } catch (UnsupportedOperationException e) {
            // OK
        }
    }

    @Test
    public void testMutableGet() {
        final Map<String, String> map = createMap().toModifiableMap();

        map.put("k3", "v3");
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
        assertNull(map.get("non-existent"));
        assertNull(map.get(null));
    }

    @Test
    public void testImmutableSize() {
        final Map<String, String> map = createMap();
        assertEquals(2, map.size());
    }

    @Test
    public void testImmutableIsEmpty() {
        final Map<String, String> map = createMap();
        assertFalse(map.isEmpty());
    }

    @Test
    public void testImmutableContains() {
        final Map<String, String> map = createMap();
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertFalse(map.containsKey("non-existent"));
        assertFalse(map.containsKey(null));
        assertTrue(map.containsValue("v1"));
        assertFalse(map.containsValue("non-existent"));
    }

    @Test
    public void testImmutableEquals() {
        final Map<String, String> map = createMap();

        assertFalse(map.equals(threeEntryMap));
        assertFalse(map.equals(ImmutableMap.of("k1", "v1", "k3", "v3")));
        assertFalse(map.equals(ImmutableMap.of("k1", "v1", "k2", "different-value")));
    }

    @Test
    public void testMutableContains() {
        final Map<String, String> map = createMap().toModifiableMap();
        map.put("k3", "v3");
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("non-existent"));
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testtoModifiableMap() {
        final ImmutableOffsetMap<String, String> source = createMap();
        final Map<String, String> result = source.toModifiableMap();

        // The two maps should be equal, but isolated
        assertTrue(result instanceof MutableOffsetMap);
        assertEquals(source, result);
        assertEquals(result, source);

        // Quick test for clearing MutableOffsetMap
        result.clear();
        assertEquals(0, result.size());
        assertEquals(Collections.emptyMap(), result);

        // The two maps should differ now
        assertFalse(source.equals(result));
        assertFalse(result.equals(source));

        // The source map should still equal the template
        assertEquals(twoEntryMap, source);
        assertEquals(source, twoEntryMap);
    }

    @Test
    public void testReusedFields() {
        final ImmutableOffsetMap<String, String> source = createMap();
        final MutableOffsetMap<String, String> mutable = source.toModifiableMap();

        // Should not affect the result
        mutable.remove("non-existent");

        // Resulting map should be equal, but not the same object
        final ImmutableOffsetMap<String, String> result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertNotSame(source, result);
        assertEquals(source, result);

        // Internal fields should be reused
        assertSame(source.offsets(), result.offsets());
        assertSame(source.objects(), result.objects());
    }

    @Test
    public void testReusedOffsets() {
        final ImmutableOffsetMap<String, String> source = createMap();
        final MutableOffsetMap<String, String> mutable = source.toModifiableMap();

        mutable.remove("k1");
        mutable.put("k1", "v1");

        final ImmutableOffsetMap<String, String> result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertTrue(source.equals(result));
        assertTrue(result.equals(source));

        // Iterator order must not be preserved
        assertFalse(Iterators.elementsEqual(source.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    public void testReusedOffsetsUnordered() {
        final ImmutableOffsetMap<String, String> source = unorderedMap();
        final MutableOffsetMap<String, String> mutable = source.toModifiableMap();

        mutable.remove("k1");
        mutable.put("k1", "v1");

        final ImmutableOffsetMap<String, String> result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertEquals(source, result);

        // Only offsets should be shared
        assertSame(source.offsets(), result.offsets());
        assertNotSame(source.objects(), result.objects());

        // Iterator order needs to be preserved
        assertTrue(Iterators.elementsEqual(source.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    public void testEmptyMutable() throws CloneNotSupportedException {
        final MutableOffsetMap<String, String> map = MutableOffsetMap.ordered();
        assertTrue(map.isEmpty());

        final Map<String, String> other = map.clone();
        assertEquals(other, map);
        assertNotSame(other, map);
    }

    @Test
    public void testMutableToEmpty() {
        final MutableOffsetMap<String, String> mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.remove("k2");

        assertTrue(mutable.isEmpty());
        assertSame(ImmutableMap.of(), mutable.toUnmodifiableMap());
    }

    @Test
    public void testMutableToSingleton() {
        final MutableOffsetMap<String, String> mutable = createMap().toModifiableMap();

        mutable.remove("k1");

        final Map<String, String> result = mutable.toUnmodifiableMap();

        // Should devolve to a singleton
        assertTrue(result instanceof SharedSingletonMap);
        assertEquals(ImmutableMap.of("k2", "v2"), result);
    }

    @Test
    public void testMutableToNewSingleton() {
        final MutableOffsetMap<String, String> mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");

        final Map<String, String> result = mutable.toUnmodifiableMap();

        assertTrue(result instanceof ImmutableOffsetMap);
        assertEquals(ImmutableMap.of("k2", "v2", "k3", "v3"), result);
    }

    @Test
    public void testMutableSize() {
        final MutableOffsetMap<String, String> mutable = createMap().toModifiableMap();
        assertEquals(2, mutable.size());

        mutable.put("k3", "v3");
        assertEquals(3, mutable.size());
        mutable.remove("k2");
        assertEquals(2, mutable.size());
        mutable.put("k1", "new-v1");
        assertEquals(2, mutable.size());
    }

    @Test
    public void testExpansionWithOrder() {
        final MutableOffsetMap<String, String> mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");
        mutable.put("k1", "v1");

        assertEquals(ImmutableMap.of("k1", "v1", "k3", "v3"), mutable.newKeys());

        final Map<String, String> result = mutable.toUnmodifiableMap();

        assertTrue(result instanceof ImmutableOffsetMap);
        assertEquals(threeEntryMap, result);
        assertEquals(result, threeEntryMap);
        assertFalse(Iterators.elementsEqual(threeEntryMap.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    public void testExpansionWithoutOrder() {
        final MutableOffsetMap<String, String> mutable = unorderedMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");
        mutable.put("k1", "v1");

        assertEquals(ImmutableMap.of("k3", "v3"), mutable.newKeys());

        final Map<String, String> result = mutable.toUnmodifiableMap();

        assertTrue(result instanceof ImmutableOffsetMap);
        assertEquals(threeEntryMap, result);
        assertEquals(result, threeEntryMap);
        assertTrue(Iterators.elementsEqual(threeEntryMap.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    public void testReplacedValue() {
        final ImmutableOffsetMap<String, String> source = createMap();
        final MutableOffsetMap<String, String> mutable = source.toModifiableMap();

        mutable.put("k1", "replaced");

        final ImmutableOffsetMap<String, String> result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        final Map<String, String> reference = ImmutableMap.of("k1", "replaced", "k2", "v2");

        assertEquals(reference, result);
        assertEquals(result, reference);
        assertSame(source.offsets(), result.offsets());
        assertNotSame(source.objects(), result.objects());
    }

    @Test
    public void testCloneableFlipping() throws CloneNotSupportedException {
        final MutableOffsetMap<String, String> source = createMap().toModifiableMap();

        // Must clone before mutation
        assertTrue(source.needClone());

        // Non-existent entry, should not clone
        source.remove("non-existent");
        assertTrue(source.needClone());

        // Changes the array, should clone
        source.remove("k1");
        assertFalse(source.needClone());

        // Create a clone of the map, which shares the array
        final MutableOffsetMap<String, String> result = source.clone();
        assertFalse(source.needClone());
        assertTrue(result.needClone());
        assertSame(source.array(), result.array());

        // Changes the array, should clone
        source.put("k1", "v2");
        assertFalse(source.needClone());
        assertTrue(result.needClone());

        // Forced copy, no cloning needed, but maps are equal
        final ImmutableOffsetMap<String, String> immutable = (ImmutableOffsetMap<String, String>) source
                .toUnmodifiableMap();
        assertFalse(source.needClone());
        assertTrue(source.equals(immutable));
        assertTrue(immutable.equals(source));
        assertTrue(Iterables.elementsEqual(source.entrySet(), immutable.entrySet()));
    }

    @Test
    public void testCloneableFlippingUnordered() throws CloneNotSupportedException {
        final MutableOffsetMap<String, String> source = unorderedMap().toModifiableMap();

        // Must clone before mutation
        assertTrue(source.needClone());

        // Non-existent entry, should not clone
        source.remove("non-existent");
        assertTrue(source.needClone());

        // Changes the array, should clone
        source.remove("k1");
        assertFalse(source.needClone());

        // Create a clone of the map, which shares the array
        final MutableOffsetMap<String, String> result = source.clone();
        assertFalse(source.needClone());
        assertTrue(result.needClone());
        assertSame(source.array(), result.array());

        // Changes the array, should clone
        source.put("k1", "v2");
        assertFalse(source.needClone());
        assertTrue(result.needClone());

        // Creates a immutable view, which shares the array
        final ImmutableOffsetMap<String, String> immutable = (ImmutableOffsetMap<String, String>) source
                .toUnmodifiableMap();
        assertTrue(source.needClone());
        assertSame(source.array(), immutable.objects());
    }

    @Test
    public void testMutableEntrySet() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();

        assertTrue(map.entrySet().add(new SimpleEntry<>("k3", "v3")));
        assertTrue(map.containsKey("k3"));
        assertEquals("v3", map.get("k3"));

        // null is not an Entry: ignore
        assertFalse(map.entrySet().remove(null));

        // non-matching value: ignore
        assertFalse(map.entrySet().remove(new SimpleEntry<>("k1", "other")));
        assertTrue(map.containsKey("k1"));

        // ignore null values
        assertFalse(map.entrySet().remove(new SimpleEntry<>("k1", null)));
        assertTrue(map.containsKey("k1"));

        assertTrue(map.entrySet().remove(new SimpleEntry<>("k1", "v1")));
        assertFalse(map.containsKey("k1"));
    }

    private static void assertIteratorBroken(final Iterator<?> it) {
        try {
            it.hasNext();
            fail();
        } catch (ConcurrentModificationException e) {
            // OK
        }
        try {
            it.next();
            fail();
        } catch (ConcurrentModificationException e) {
            // OK
        }
        try {
            it.remove();
            fail();
        } catch (ConcurrentModificationException e) {
            // OK
        }
    }

    @Test
    public void testMutableSimpleEquals() {
        final ImmutableOffsetMap<String, String> source = createMap();
        final Map<String, String> map = source.toModifiableMap();

        assertTrue(map.equals(map));
        assertFalse(map.equals(null));
        assertFalse(map.equals("string"));
        assertTrue(map.equals(source));
    }

    @Test
    public void testMutableSimpleHashCode() {
        final Map<String, String> map = createMap().toModifiableMap();

        assertEquals(twoEntryMap.hashCode(), map.hashCode());
    }

    @Test
    public void testMutableIteratorBasics() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();

        // Not advanced, remove should fail
        try {
            it.remove();
            fail();
        } catch (IllegalStateException e) {
            // OK
        }

        assertTrue(it.hasNext());
        assertEquals("k1", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("k2", it.next().getKey());
        assertFalse(it.hasNext());

        // Check end-of-iteration throw
        try {
            it.next();
            fail();
        } catch (NoSuchElementException e) {
            // OK
        }
    }

    @Test
    public void testMutableIteratorWithRemove() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();

        // Advance one element
        assertTrue(it.hasNext());
        assertEquals("k1", it.next().getKey());

        // Remove k1
        it.remove();
        assertEquals(1, map.size());
        assertFalse(map.containsKey("k1"));

        // Iterator should still work
        assertTrue(it.hasNext());
        assertEquals("k2", it.next().getKey());
        assertFalse(it.hasNext());
    }

    @Test
    public void testMutableIteratorOffsetReplaceWorks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.put("k1", "new-v1");
        assertTrue(it.hasNext());
    }

    @Test
    public void testMutableIteratorNewReplaceWorks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        map.put("k3", "v3");
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.put("k3", "new-v3");
        assertTrue(it.hasNext());
    }

    @Test
    public void testMutableIteratorOffsetAddBreaks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        map.put("k3", "v3");
        map.remove("k1");

        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.put("k1", "new-v1");
        assertIteratorBroken(it);
    }

    @Test
    public void testMutableIteratorNewAddBreaks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.put("k3", "v3");
        assertIteratorBroken(it);
    }

    @Test
    public void testMutableIteratorOffsetRemoveBreaks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.remove("k1");
        assertIteratorBroken(it);
    }

    @Test
    public void testMutableIteratorNewRemoveBreaks() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        map.put("k3", "v3");
        final Iterator<Entry<String, String>> it = map.entrySet().iterator();
        it.next();

        map.remove("k3");
        assertIteratorBroken(it);
    }

    @Test
    public void testMutableCrossIteratorRemove() {
        final MutableOffsetMap<String, String> map = createMap().toModifiableMap();
        final Set<Entry<String, String>> es = map.entrySet();
        final Iterator<Entry<String, String>> it1 = es.iterator();
        final Iterator<Entry<String, String>> it2 = es.iterator();

        // Remove k1 via it1
        it1.next();
        it2.next();
        it1.remove();
        assertEquals(1, map.size());

        // Check it2 was broken
        assertIteratorBroken(it2);
    }

    @Test
    public void testImmutableSerialization() throws IOException, ClassNotFoundException {
        final Map<String, String> source = createMap();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        final Map<String, String> result = (Map<String, String>) ois.readObject();

        assertEquals(source, result);
    }
}

/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OffsetMapTest {
    private final Map<String, String> twoEntryMap = ImmutableMap.of("k1", "v1", "k2", "v2");
    private final Map<String, String> threeEntryMap = ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3");

    private ImmutableOffsetMap<String, String> createMap() {
        return (ImmutableOffsetMap<String, String>) ImmutableOffsetMap.orderedCopyOf(twoEntryMap);
    }

    private ImmutableOffsetMap<String, String> unorderedMap() {
        return (ImmutableOffsetMap<String, String>) ImmutableOffsetMap.unorderedCopyOf(twoEntryMap);
    }

    @BeforeEach
    void setup() {
        OffsetMapCache.invalidateCache();
    }

    public void testWrongImmutableConstruction() {
        assertThrows(IllegalArgumentException.class,
            () -> new ImmutableOffsetMap.Ordered<>(ImmutableMap.of(), new String[1]));
    }

    @Test
    void testCopyEmptyMap() {
        final var source = Map.of();
        final var result = ImmutableOffsetMap.orderedCopyOf(source);

        assertEquals(source, result);
        assertInstanceOf(ImmutableMap.class, result);
    }

    @Test
    void testCopySingletonMap() {
        final var source = Map.of("a", "b");
        final var result = ImmutableOffsetMap.orderedCopyOf(source);

        assertEquals(source, result);
        assertInstanceOf(SharedSingletonMap.class, result);
    }

    @Test
    void testCopyMap() {
        final var map = createMap();

        // Equality in both directions
        assertEquals(twoEntryMap, map);
        assertEquals(map, twoEntryMap);

        // hashcode has to match
        assertEquals(twoEntryMap.hashCode(), map.hashCode());

        // Iterator order needs to be preserved
        assertTrue(Iterators.elementsEqual(twoEntryMap.entrySet().iterator(), map.entrySet().iterator()));

        // Should result in the same object
        assertSame(map, ImmutableOffsetMap.orderedCopyOf(map));

        final var mutable = map.toModifiableMap();
        final var copy = ImmutableOffsetMap.orderedCopyOf(mutable);

        assertEquals(mutable, copy);
        assertEquals(map, copy);
        assertNotSame(mutable, copy);
        assertNotSame(map, copy);
    }

    @Test
    void testImmutableSimpleEquals() {
        final var map = createMap();

        assertEquals(map, map);
        assertNotEquals(null, map);
        assertNotEquals("string", map);
    }

    @Test
    void testImmutableGet() {
        final var map = createMap();

        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertNull(map.get("non-existent"));
        assertNull(map.get(null));
    }

    @Test
    void testImmutableGuards() {
        final var map = createMap();

        final var values = map.values();
        assertThrows(UnsupportedOperationException.class, () -> values.add("v1"));
        assertThrows(UnsupportedOperationException.class, () -> values.remove("v1"));
        assertThrows(UnsupportedOperationException.class, () -> values.clear());

        final var vit = values.iterator();
        vit.next();
        assertThrows(UnsupportedOperationException.class, () -> vit.remove());

        final var keySet = map.keySet();
        assertThrows(UnsupportedOperationException.class, () -> keySet.add("k1"));
        assertThrows(UnsupportedOperationException.class, () -> keySet.clear());
        assertThrows(UnsupportedOperationException.class, () -> keySet.remove("k1"));

        final var kit = keySet.iterator();
        kit.next();
        assertThrows(UnsupportedOperationException.class, () -> kit.remove());

        final var entrySet = map.entrySet();
        assertThrows(UnsupportedOperationException.class, () -> entrySet.clear());
        assertThrows(UnsupportedOperationException.class, () -> entrySet.add(new SimpleEntry<>("k1", "v1")));
        assertThrows(UnsupportedOperationException.class, () -> entrySet.remove(new SimpleEntry<>("k1", "v1")));

        final var eit = entrySet.iterator();
        eit.next();
        assertThrows(UnsupportedOperationException.class, () -> eit.remove());

        assertThrows(UnsupportedOperationException.class, () -> map.clear());
        assertThrows(UnsupportedOperationException.class, () -> map.put("k1", "fail"));
        assertThrows(UnsupportedOperationException.class, () -> map.putAll(ImmutableMap.of("k1", "fail")));
        assertThrows(UnsupportedOperationException.class, () -> map.remove("k1"));
    }

    @Test
    void testMutableGet() {
        final var map = createMap().toModifiableMap();

        map.put("k3", "v3");
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
        assertNull(map.get("non-existent"));
        assertNull(map.get(null));
    }

    @Test
    void testImmutableSize() {
        final var map = createMap();
        assertEquals(2, map.size());
    }

    @Test
    void testImmutableIsEmpty() {
        final var map = createMap();
        assertFalse(map.isEmpty());
    }

    @Test
    void testImmutableContains() {
        final var map = createMap();
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertFalse(map.containsKey("non-existent"));
        assertFalse(map.containsKey(null));
        assertTrue(map.containsValue("v1"));
        assertFalse(map.containsValue("non-existent"));
    }

    @Test
    void testImmutableEquals() {
        final var map = createMap();

        assertNotEquals(map, threeEntryMap);
        assertNotEquals(map, ImmutableMap.of("k1", "v1", "k3", "v3"));
        assertNotEquals(map, ImmutableMap.of("k1", "v1", "k2", "different-value"));
    }

    @Test
    void testMutableContains() {
        final var map = createMap().toModifiableMap();
        map.put("k3", "v3");
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
        assertTrue(map.containsKey("k3"));
        assertFalse(map.containsKey("non-existent"));
        assertFalse(map.containsKey(null));
    }

    @Test
    void testtoModifiableMap() {
        final var source = createMap();
        final var result = source.toModifiableMap();

        // The two maps should be equal, but isolated
        assertInstanceOf(MutableOffsetMap.class, result);
        assertEquals(source, result);
        assertEquals(result, source);

        // Quick test for clearing MutableOffsetMap
        result.clear();
        assertEquals(0, result.size());
        assertEquals(Map.of(), result);

        // The two maps should differ now
        assertNotEquals(source, result);
        assertNotEquals(result, source);

        // The source map should still equal the template
        assertEquals(twoEntryMap, source);
        assertEquals(source, twoEntryMap);
    }

    @Test
    void testReusedFields() {
        final var source = createMap();
        final var mutable = source.toModifiableMap();

        // Should not affect the result
        mutable.remove("non-existent");

        // Resulting map should be equal, but not the same object
        final var result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertNotSame(source, result);
        assertEquals(source, result);

        // Internal fields should be reused
        assertSame(source.offsets(), result.offsets());
        assertSame(source.objects(), result.objects());
    }

    @Test
    void testReusedOffsets() {
        final var source = createMap();
        final var mutable = source.toModifiableMap();

        mutable.remove("k1");
        mutable.put("k1", "v1");

        final var result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertEquals(source, result);
        assertEquals(result, source);

        // Iterator order must not be preserved
        assertFalse(Iterators.elementsEqual(source.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    void testReusedOffsetsUnordered() {
        final var source = unorderedMap();
        final var mutable = source.toModifiableMap();

        mutable.remove("k1");
        mutable.put("k1", "v1");

        final var result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        assertEquals(source, result);

        // Only offsets should be shared
        assertSame(source.offsets(), result.offsets());
        assertNotSame(source.objects(), result.objects());

        // Iterator order needs to be preserved
        assertTrue(Iterators.elementsEqual(source.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    void testEmptyMutable() throws CloneNotSupportedException {
        final var map = MutableOffsetMap.ordered();
        assertTrue(map.isEmpty());

        final var other = map.clone();
        assertEquals(other, map);
        assertNotSame(other, map);
    }

    @Test
    void testMutableToEmpty() {
        final var mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.remove("k2");

        assertTrue(mutable.isEmpty());
        assertSame(ImmutableMap.of(), mutable.toUnmodifiableMap());
    }

    @Test
    void testMutableToSingleton() {
        final var mutable = createMap().toModifiableMap();

        mutable.remove("k1");

        final var result = mutable.toUnmodifiableMap();

        // Should devolve to a singleton
        assertInstanceOf(SharedSingletonMap.class, result);
        assertEquals(ImmutableMap.of("k2", "v2"), result);
    }

    @Test
    void testMutableToNewSingleton() {
        final var mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");

        final var result = mutable.toUnmodifiableMap();

        assertInstanceOf(ImmutableOffsetMap.class, result);
        assertEquals(ImmutableMap.of("k2", "v2", "k3", "v3"), result);
    }

    @Test
    void testMutableSize() {
        final var mutable = createMap().toModifiableMap();
        assertEquals(2, mutable.size());

        mutable.put("k3", "v3");
        assertEquals(3, mutable.size());
        mutable.remove("k2");
        assertEquals(2, mutable.size());
        mutable.put("k1", "new-v1");
        assertEquals(2, mutable.size());
    }

    @Test
    void testExpansionWithOrder() {
        final var mutable = createMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");
        mutable.put("k1", "v1");

        assertEquals(ImmutableMap.of("k1", "v1", "k3", "v3"), mutable.newKeys());

        final var result = mutable.toUnmodifiableMap();

        assertInstanceOf(ImmutableOffsetMap.class, result);
        assertEquals(threeEntryMap, result);
        assertEquals(result, threeEntryMap);
        assertFalse(Iterators.elementsEqual(threeEntryMap.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    void testExpansionWithoutOrder() {
        final var mutable = unorderedMap().toModifiableMap();

        mutable.remove("k1");
        mutable.put("k3", "v3");
        mutable.put("k1", "v1");

        assertEquals(ImmutableMap.of("k3", "v3"), mutable.newKeys());

        final var result = mutable.toUnmodifiableMap();

        assertInstanceOf(ImmutableOffsetMap.class, result);
        assertEquals(threeEntryMap, result);
        assertEquals(result, threeEntryMap);
        assertTrue(Iterators.elementsEqual(threeEntryMap.entrySet().iterator(), result.entrySet().iterator()));
    }

    @Test
    void testReplacedValue() {
        final var source = createMap();
        final var mutable = source.toModifiableMap();

        mutable.put("k1", "replaced");

        final var result = (ImmutableOffsetMap<String, String>) mutable
                .toUnmodifiableMap();
        final var reference = ImmutableMap.of("k1", "replaced", "k2", "v2");

        assertEquals(reference, result);
        assertEquals(result, reference);
        assertSame(source.offsets(), result.offsets());
        assertNotSame(source.objects(), result.objects());
    }

    @Test
    void testCloneableFlipping() throws CloneNotSupportedException {
        final var source = createMap().toModifiableMap();

        // Must clone before mutation
        assertTrue(source.needClone());

        // Non-existent entry, should not clone
        source.remove("non-existent");
        assertTrue(source.needClone());

        // Changes the array, should clone
        source.remove("k1");
        assertFalse(source.needClone());

        // Create a clone of the map, which shares the array
        final var result = source.clone();
        assertFalse(source.needClone());
        assertTrue(result.needClone());
        assertSame(source.array(), result.array());

        // Changes the array, should clone
        source.put("k1", "v2");
        assertFalse(source.needClone());
        assertTrue(result.needClone());

        // Forced copy, no cloning needed, but maps are equal
        final var immutable = (ImmutableOffsetMap<String, String>) source
                .toUnmodifiableMap();
        assertFalse(source.needClone());
        assertEquals(source, immutable);
        assertEquals(immutable, source);
        assertTrue(Iterables.elementsEqual(source.entrySet(), immutable.entrySet()));
    }

    @Test
    void testCloneableFlippingUnordered() throws CloneNotSupportedException {
        final var source = unorderedMap().toModifiableMap();

        // Must clone before mutation
        assertTrue(source.needClone());

        // Non-existent entry, should not clone
        source.remove("non-existent");
        assertTrue(source.needClone());

        // Changes the array, should clone
        source.remove("k1");
        assertFalse(source.needClone());

        // Create a clone of the map, which shares the array
        final var result = source.clone();
        assertFalse(source.needClone());
        assertTrue(result.needClone());
        assertSame(source.array(), result.array());

        // Changes the array, should clone
        source.put("k1", "v2");
        assertFalse(source.needClone());
        assertTrue(result.needClone());

        // Creates a immutable view, which shares the array
        final var immutable = (ImmutableOffsetMap<String, String>) source.toUnmodifiableMap();
        assertTrue(source.needClone());
        assertSame(source.array(), immutable.objects());
    }

    @Test
    void testMutableEntrySet() {
        final var map = createMap().toModifiableMap();

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
        assertThrows(ConcurrentModificationException.class, () -> it.hasNext());
        assertThrows(ConcurrentModificationException.class, () -> it.next());
        assertThrows(ConcurrentModificationException.class, () -> it.remove());
    }

    @Test
    void testMutableSimpleEquals() {
        final var source = createMap();
        final var map = source.toModifiableMap();

        assertEquals(map, map);
        assertNotEquals(null, map);
        assertNotEquals("string", map);
        assertEquals(map, source);
    }

    @Test
    void testMutableSimpleHashCode() {
        final var map = createMap().toModifiableMap();

        assertEquals(twoEntryMap.hashCode(), map.hashCode());
    }

    @Test
    void testMutableIteratorBasics() {
        final var map = createMap().toModifiableMap();
        final var it = map.entrySet().iterator();

        // Not advanced, remove should fail
        assertThrows(IllegalStateException.class, () -> it.remove());

        assertTrue(it.hasNext());
        assertEquals("k1", it.next().getKey());
        assertTrue(it.hasNext());
        assertEquals("k2", it.next().getKey());
        assertFalse(it.hasNext());

        // Check end-of-iteration throw
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    void testMutableIteratorWithRemove() {
        final var map = createMap().toModifiableMap();
        final var it = map.entrySet().iterator();

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
    void testMutableIteratorOffsetReplaceWorks() {
        final var map = createMap().toModifiableMap();
        final var it = map.entrySet().iterator();
        it.next();

        map.put("k1", "new-v1");
        assertTrue(it.hasNext());
    }

    @Test
    void testMutableIteratorNewReplaceWorks() {
        final var map = createMap().toModifiableMap();
        map.put("k3", "v3");
        final var it = map.entrySet().iterator();
        it.next();

        map.put("k3", "new-v3");
        assertTrue(it.hasNext());
    }

    @Test
    void testMutableIteratorOffsetAddBreaks() {
        final var map = createMap().toModifiableMap();
        map.put("k3", "v3");
        map.remove("k1");

        final var it = map.entrySet().iterator();
        it.next();

        map.put("k1", "new-v1");
        assertIteratorBroken(it);
    }

    @Test
    void testMutableIteratorNewAddBreaks() {
        final var map = createMap().toModifiableMap();
        final var it = map.entrySet().iterator();
        it.next();

        map.put("k3", "v3");
        assertIteratorBroken(it);
    }

    @Test
    void testMutableIteratorOffsetRemoveBreaks() {
        final var map = createMap().toModifiableMap();
        final var it = map.entrySet().iterator();
        it.next();

        map.remove("k1");
        assertIteratorBroken(it);
    }

    @Test
    void testMutableIteratorNewRemoveBreaks() {
        final var map = createMap().toModifiableMap();
        map.put("k3", "v3");
        final var it = map.entrySet().iterator();
        it.next();

        map.remove("k3");
        assertIteratorBroken(it);
    }

    @Test
    void testMutableCrossIteratorRemove() {
        final var map = createMap().toModifiableMap();
        final var es = map.entrySet();
        final var it1 = es.iterator();
        final var it2 = es.iterator();

        // Remove k1 via it1
        it1.next();
        it2.next();
        it1.remove();
        assertEquals(1, map.size());

        // Check it2 was broken
        assertIteratorBroken(it2);
    }

    @Test
    void testImmutableSerialization() throws IOException, ClassNotFoundException {
        final var source = createMap();

        final var bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(source);
        }

        final var ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        @SuppressWarnings("unchecked")
        final var result = (Map<String, String>) ois.readObject();

        assertEquals(source, result);
    }
}

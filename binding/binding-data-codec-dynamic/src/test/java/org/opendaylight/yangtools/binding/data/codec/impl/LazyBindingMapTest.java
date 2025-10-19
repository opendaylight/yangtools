/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListBuilder;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class LazyBindingMapTest extends AbstractBindingCodecTest {
    private static Top TOP;

    @Mock
    private DataObject mockDataObject;
    @Mock
    private EntryObject<?, ?> mockEntryObject;

    @BeforeAll
    static void prepareTop() {
        final var map = new HashMap<TopLevelListKey, TopLevelList>();
        for (int i = 0; i < 2 * LazyBindingMap.LAZY_CUTOFF; i++) {
            final var item = new TopLevelListBuilder().setName(String.valueOf(i)).build();
            map.put(item.key(), item);
        }

        TOP = new TopBuilder().setTopLevelList(map).build();
    }

    @Test
    void testSimpleEquals() {
        final var actual = prepareData();
        assertInstanceOf(LazyBindingMap.class, actual.getTopLevelList());
        // AbstractMap.equals() goes through its entrySet and performs lookup for each key, hence it is excercising
        // primarily LookupState
        assertEquals(TOP, actual);
    }

    @Test
    void testEqualEntrySet() {
        final var actual = prepareData();
        // Check equality based on entry set. This primarily exercises IterState
        assertEquals(TOP.getTopLevelList().entrySet(), actual.getTopLevelList().entrySet());
    }

    @Test
    void testEqualKeySet() {
        final var actual = prepareData();
        // Check equality based on key set. This primarily exercises IterState
        assertEquals(TOP.getTopLevelList().keySet(), actual.getTopLevelList().keySet());
    }

    @Test
    void testIterKeySetLookup() {
        final var actual = prepareData();
        // Forces IterState but then switches to key lookups
        assertTrue(actual.getTopLevelList().keySet().containsAll(TOP.getTopLevelList().keySet()));
    }

    @Test
    void testIterEntrySetLookup() {
        final var actual = prepareData();
        // Forces IterState but then switches to value lookups
        assertTrue(actual.getTopLevelList().entrySet().containsAll(TOP.getTopLevelList().entrySet()));
    }

    @Test
    void testIterValueIteration() {
        assertSameIteratorObjects(prepareData().getTopLevelList().values());
    }

    @Test
    void testLookupValueIteration() {
        final var list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertSameIteratorObjects(list.values());
    }

    @Test
    void testIterKeysetIteration() {
        assertSameIteratorObjects(prepareData().getTopLevelList().keySet());
    }

    @Test
    void testLookupKeysetIteration() {
        final var list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertSameIteratorObjects(list.keySet());
    }

    private static void assertSameIteratorObjects(final Collection<?> collection) {
        final var iter2 = collection.iterator();

        for (Object element : collection) {
            // Both iterators should return same values
            assertSame(element, iter2.next());
        }
        assertFalse(iter2.hasNext());
    }

    @Test
    void testIterSameViews() {
        final var list = prepareData().getTopLevelList();
        assertSame(list.values(), list.values());
        assertSame(list.keySet(), list.keySet());
        assertSame(list.entrySet(), list.entrySet());
    }

    @Test
    void testLookupSameViews() {
        final var list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        // Careful now ... first compare should  end up changing the iteration of keyset/entryset
        final var keySet1 = list.keySet();
        final var keySet2 = list.keySet();
        final var entrySet1 = list.entrySet();
        final var entrySet2 = list.entrySet();

        // .. right here ...
        assertSame(list.values(), list.values());
        // ... so this should end up iterating slightly differently
        assertEquals(new HashSet<>(list.values()), new HashSet<>(list.values()));

        // ... and as we do not reuse keyset/entryset, we need to run full compare
        assertEquals(keySet1, keySet2);
        assertEquals(keySet1, new HashSet<>(keySet2));
        assertEquals(entrySet1, entrySet2);
        assertEquals(entrySet1, new HashSet<>(entrySet2));
    }

    @Test
    void testIterSameSize() {
        final var list = prepareData().getTopLevelList();
        // Force lookup state instantiation
        assertFalse(list.containsKey(new TopLevelListKey("blah")));

        assertEquals(list.size(), list.entrySet().size());
        assertEquals(list.size(), list.size());
        assertEquals(list.size(), list.size());
    }

    @Test
    void testLookupSameSize() {
        final var list = prepareData().getTopLevelList();
        assertEquals(list.size(), list.entrySet().size());
        assertEquals(list.size(), list.size());
        assertEquals(list.size(), list.size());
    }

    @Test
    void testImmutableThrows() {
        final var list = prepareData().getTopLevelList();
        // Various asserts for completeness' sake
        assertThrows(UnsupportedOperationException.class, () -> list.clear());
        assertThrows(UnsupportedOperationException.class, () -> list.remove(null));
        assertThrows(UnsupportedOperationException.class, () -> list.putAll(null));
    }

    @Test
    void testLookupContainsValueThrows() {
        final var list = prepareData().getTopLevelList();
        assertThrows(NullPointerException.class, () -> list.containsValue(null));
        assertThrows(ClassCastException.class, () -> list.containsValue(mockDataObject));
    }

    @Test
    @SuppressWarnings("CollectionIncompatibleType")
    void testLookupContainsKeyThrows() {
        final var list = prepareData().getTopLevelList();
        assertThrows(NullPointerException.class, () -> list.containsKey(null));
        assertThrows(ClassCastException.class, () -> list.containsKey(mockEntryObject));
    }

    @Test
    void testLookupKey() {
        final var list = prepareData().getTopLevelList();
        for (var key : TOP.getTopLevelList().keySet()) {
            assertTrue(list.containsKey(key));
        }

        assertFalse(list.containsKey(new TopLevelListKey("blah")));
    }

    @Test
    void testLookupValue() {
        final var list = prepareData().getTopLevelList();
        for (var val : TOP.getTopLevelList().values()) {
            assertTrue(list.containsValue(val));
        }

        assertFalse(list.containsValue(new TopLevelListBuilder().setName("blah").build()));

        // We checked this key, but this is a different object
        assertFalse(list.containsValue(new TopLevelListBuilder(TOP.getTopLevelList().values().iterator().next())
            .setNestedList(List.of(new NestedListBuilder().setName("foo").build()))
            .build()));
    }

    private Top prepareData() {
        return thereAndBackAgain(InstanceIdentifier.create(Top.class), TOP);
    }
}

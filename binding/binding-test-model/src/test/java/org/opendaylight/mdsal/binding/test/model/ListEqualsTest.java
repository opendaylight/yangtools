/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.Root;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.RootBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystem;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUser;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUserBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUserKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.NestedListContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.NestedListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.NestedListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system.DoubleNestedListContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystem;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListByUserBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListNoKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.by.user.DoubleNestedListNoKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.by.user.DoubleNestedListNoKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedList;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedListBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedListKey;
import org.opendaylight.yangtools.binding.util.BindingMap;

class ListEqualsTest {
    private static final String VALUE_1 = "Value1";
    private static final String VALUE_2 = "Value2";
    private static final String VALUE_3 = "Value3";

    private static final DoubleNestedListNoKey DOUBLE_NL_NO_KEY_1 = getDoubleNestedListNoKey(VALUE_1);
    private static final DoubleNestedListNoKey DOUBLE_NL_NO_KEY_2 = getDoubleNestedListNoKey(VALUE_2);
    private static final DoubleNestedListNoKey DOUBLE_NL_NO_KEY_3 = getDoubleNestedListNoKey(VALUE_3);

    private static final DoubleNestedListBySystem DOUBLE_NL_BY_SYSTEM_1 = getDoubleNestedListBySystem(VALUE_1);
    private static final DoubleNestedListBySystem DOUBLE_NL_BY_SYSTEM_2 = getDoubleNestedListBySystem(VALUE_2);
    private static final DoubleNestedListBySystem DOUBLE_NL_BY_SYSTEM_3 = getDoubleNestedListBySystem(VALUE_3);

    private static final DoubleNestedMixedList DOUBLE_NESTED_MIXED_LIST_1 = getDoubleNestedMixedList(VALUE_1);
    private static final DoubleNestedMixedList DOUBLE_NESTED_MIXED_LIST_2 = getDoubleNestedMixedList(VALUE_2);
    private static final DoubleNestedMixedList DOUBLE_NESTED_MIXED_LIST_3 = getDoubleNestedMixedList(VALUE_3);

    @Test
    void testTripleUnorderedList() {
        // Same elements test
        final var sameRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final var sameRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final var unorderedRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_2, DOUBLE_NL_NO_KEY_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final var differentRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testTripleOrderedListMixedWithContainers() {
        // Same elements test
        final var sameRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final var sameRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final var unorderedRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_2, DOUBLE_NL_BY_SYSTEM_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final var differentRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testMixedUnorderedOrderedListWithOrderedListAtTheEnd() {
        // Same elements test
        final var sameRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final var sameRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final var unorderedRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_2, DOUBLE_NESTED_MIXED_LIST_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final var differentRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testBySystemLeafList() {
        // Same elements test
        final var sameRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final var sameRootList2 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRootList1, sameRootList2);

        // Unordered elements test
        final var unorderedRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final var unorderedRootList2 = getRootListByUserWithLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRootList1, unorderedRootList2);

        // Different elements
        final var differentRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final var differentRootList2 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRootList1, differentRootList2);
    }

    @Test
    void testByUserLeafList() {
        // Same elements test
        final var sameRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final var sameRootList2 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRootList1, sameRootList2);

        // Unordered elements test
        final var unorderedRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final var unorderedRootList2 = getRootListByUserWithLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRootList1, unorderedRootList2);

        // Different elements
        final var differentRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final var differentRootList2 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRootList1, differentRootList2);
    }

    @Test
    void testNestedBySystemLeafList() {
        // Same elements test
        final var sameRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var sameRoot2 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var unorderedRoot2 = getNestedLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var differentRoot2 = getNestedLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testNestedByUserLeafList() {
        // Same elements test
        final var sameRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final var sameRoot2 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final var unorderedRoot2 = getNestedLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final var differentRoot2 = getNestedLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testBySystemLeafListUnderContainer() {
        // Same elements test
        final var sameRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var sameRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var unorderedRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final var differentRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    void testByUserLeafListUnderContainer() {
        // Same elements test
        final var sameRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final var sameRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final var unorderedRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final var unorderedRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final var differentRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final var differentRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    private static Root getRootTripleList(final DoubleNestedListNoKey doubleNestedListNoKeysVal1,
            final DoubleNestedListNoKey doubleNestedListNoKeysVal2) {
        final var nestedListByUser1 = new NestedListByUserBuilder()
            .setName("nested1")
            .setDoubleNestedListNoKey(List.of(doubleNestedListNoKeysVal1, doubleNestedListNoKeysVal2))
            .build();
        final var nestedListByUser2 = new NestedListByUserBuilder()
            .setName("nested2")
            .setDoubleNestedListNoKey(List.of(doubleNestedListNoKeysVal2, doubleNestedListNoKeysVal1))
            .build();

        return new RootBuilder()
            .setRootListByUser(List.of(
                new RootListByUserBuilder()
                .setName("list1")
                .setNestedListByUser(List.of(nestedListByUser1, nestedListByUser2))
                .build(),
                new RootListByUserBuilder()
                .setName("list1")
                .setNestedListByUser(List.of(nestedListByUser2, nestedListByUser1))
                .build()))
            .build();
    }

    private static DoubleNestedListNoKey getDoubleNestedListNoKey(final String name) {
        return new DoubleNestedListNoKeyBuilder().setName(name).setType(name + "_type").build();
    }

    private static Root getRootTripleOrderedList(final DoubleNestedListBySystem doubleNestedListBySystemVal1,
            final DoubleNestedListBySystem doubleNestedListBySystemVal2) {
        // Create the NestedListBySystem instance
        final var nestedListBySystem1 = new NestedListBySystemBuilder()
            .withKey(new NestedListBySystemKey("nested-list-by-system-1"))
            .setName("nested-list-by-system-1")
            .setType("nested-list-by-system-type-1")
            .setDoubleNestedListContainer(new DoubleNestedListContainerBuilder()
                .setDoubleNestedListBySystem(BindingMap.of(doubleNestedListBySystemVal1, doubleNestedListBySystemVal2))
                .build())
            .build();
        final var nestedListBySystem2 = new NestedListBySystemBuilder()
            .withKey(new NestedListBySystemKey("nested-list-by-system-2"))
            .setName("nested-list-by-system-2")
            .setType("nested-list-by-system-type-2")
            .setDoubleNestedListContainer(new DoubleNestedListContainerBuilder()
                .setDoubleNestedListBySystem(BindingMap.of(doubleNestedListBySystemVal2, doubleNestedListBySystemVal1))
                .build())
            .build();

        return new RootBuilder()
            .setRootListBySystem(BindingMap.of(
                new RootListBySystemBuilder()
                    .withKey(new RootListBySystemKey("root-list-by-system-1"))
                    .setName("root-list-by-system-1")
                    .setType("root-list-by-system-type-1")
                    .setNestedListContainer(new NestedListContainerBuilder()
                        .setNestedListBySystem(BindingMap.of(nestedListBySystem1, nestedListBySystem2))
                        .build())
                    .build(),
                new RootListBySystemBuilder()
                    .withKey(new RootListBySystemKey("root-list-by-system-2"))
                    .setName("root-list-by-system-2")
                    .setType("root-list-by-system-type-2")
                    .setNestedListContainer(new NestedListContainerBuilder()
                        .setNestedListBySystem(BindingMap.of(nestedListBySystem2, nestedListBySystem1))
                        .build())
                    .build()))
            .build();
    }

    private static DoubleNestedListBySystem getDoubleNestedListBySystem(final String keyVal) {
        return new DoubleNestedListBySystemBuilder()
            .withKey(new DoubleNestedListBySystemKey(keyVal))
            .setName(keyVal)
            .setType(keyVal + "_type")
            .build();
    }

    private static Root getMixedRoot(final DoubleNestedMixedList doubleNestedMixedListVal1,
            final DoubleNestedMixedList doubleNestedMixedListVal2) {
        // add DoubleNestedMixedList object to NestedListNoKey
        final var nestedListNoKey1 = new NestedListNoKeyBuilder()
            .setName("Nested List 1")
            .setType("Type 1")
            .setDoubleNestedMixedList(BindingMap.of(doubleNestedMixedListVal1, doubleNestedMixedListVal2))
            .build();

        // add DoubleNestedMixedList object to NestedListNoKey
        final var nestedListNoKey2 = new NestedListNoKeyBuilder()
            .setName("Nested List 2")
            .setType("Type 2")
            .setDoubleNestedMixedList(BindingMap.of(doubleNestedMixedListVal2, doubleNestedMixedListVal1))
            .build();

        return new RootBuilder()
            .setRootListByUser(List.of(
                new RootListByUserBuilder()
                    .withKey(new RootListByUserKey("list1"))
                    .setName("list1")
                    .setType("Type 1")
                    .setNestedListNoKey(List.of(nestedListNoKey1, nestedListNoKey2))
                    .build(),
                new RootListByUserBuilder()
                    .withKey(new RootListByUserKey("list2"))
                    .setName("list2")
                    .setType("Type 2")
                    .setNestedListNoKey(List.of(nestedListNoKey2, nestedListNoKey1))
                    .build()))
            .build();
    }

    private static DoubleNestedMixedList getDoubleNestedMixedList(final String keyVal) {
        // create DoubleNestedMixedList object
        return new DoubleNestedMixedListBuilder()
            .withKey(new DoubleNestedMixedListKey(keyVal))
            .setName(keyVal)
            .setType(keyVal + "_type")
            .build();
    }

    private static RootListByUser getRootListByUserWithLeafListBySystem(final String val1, final String val2) {
        return new RootListByUserBuilder()
            .setName("list1")
            .setType("type1")
            .setListByUserLeafListBySystem(Set.of(val1, val2))
            .build();
    }

    private static RootListByUser getRootListByUserWithLeafListByUser(final String val1, final String val2) {
        return new RootListByUserBuilder()
            .setName("list1")
            .setType("type1")
            .setListByUserLeafListByUser(List.of(val1, val2))
            .build();
    }

    private static RootListBySystem getRootListBySystemWithLeafListBySystem(final String key, final String val1,
            final String val2) {
        return new RootListBySystemBuilder()
            .withKey(new RootListBySystemKey(key))
            .setName(key)
            .setType("type1")
            .setNestedListContainer(new NestedListContainerBuilder()
                .setListBySystemLeafListBySystem(Set.of(val1, val2))
                .build())
            .build();
    }

    private static RootListBySystem getRootListBySystemWithLeafListByUser(final String key, final String val1,
            final String val2) {
        return new RootListBySystemBuilder()
            .withKey(new RootListBySystemKey(key))
            .setName(key)
            .setType("type1")
            .setNestedListContainer(new NestedListContainerBuilder()
                .setListBySystemLeafListByUser(List.of(val1, val2))
                .build())
            .build();
    }

    private static Root getRootListBySystemWithNestedLeafListBySystem(final String val1, final String val2) {
        return new RootBuilder()
            .setRootListBySystem(BindingMap.of(
                getRootListBySystemWithLeafListBySystem(VALUE_2, val1, val2),
                getRootListBySystemWithLeafListBySystem(VALUE_3, val2, val1)))
            .build();
    }

    private static Root getRootListBySystemWithNestedLeafListByUser(final String val1, final String val2) {
        return new RootBuilder()
            .setRootListBySystem(BindingMap.of(
                getRootListBySystemWithLeafListByUser(VALUE_2, val1, val2),
                getRootListBySystemWithLeafListByUser(VALUE_3, val2, val1)))
            .build();
    }


    private static Root getNestedLeafListBySystem(final String val1, final String val2) {
        return new RootBuilder()
            .setRootListByUser(List.of(
                getRootListByUserWithLeafListBySystem(val1, val2),
                getRootListByUserWithLeafListBySystem(val2, val1)))
            .build();
    }

    private static Root getNestedLeafListByUser(final String val1, final String val2) {
        return new RootBuilder()
            .setRootListByUser(List.of(
                getRootListByUserWithLeafListByUser(val1, val2),
                getRootListByUserWithLeafListByUser(val2, val1)))
            .build();
    }
}

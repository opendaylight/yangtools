/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.Root;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.RootBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystem;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUser;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUserBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.RootListByUserKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.NestedListContainer;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.NestedListContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.NestedListBySystem;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.NestedListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.NestedListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system.DoubleNestedListContainer;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system.DoubleNestedListContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystem;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystemBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.system.nested.list.container.nested.list.by.system._double.nested.list.container.DoubleNestedListBySystemKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListByUser;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListByUserBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListNoKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.NestedListNoKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.by.user.DoubleNestedListNoKey;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.by.user.DoubleNestedListNoKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedList;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedListBuilder;
import org.opendaylight.yang.gen.v1.urn.yang.equals.rev230424.root.root.list.by.user.nested.list.no.key.DoubleNestedMixedListKey;

public class TestListEquals {

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
    public void testTripleUnorderedList() {
        // Same elements test
        final Root sameRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final Root sameRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final Root unorderedRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_2, DOUBLE_NL_NO_KEY_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_2);
        final Root differentRoot2 = getRootTripleList(DOUBLE_NL_NO_KEY_1, DOUBLE_NL_NO_KEY_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testTripleOrderedListMixedWithContainers() {
        // Same elements test
        final Root sameRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final Root sameRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final Root unorderedRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_2, DOUBLE_NL_BY_SYSTEM_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_2);
        final Root differentRoot2 = getRootTripleOrderedList(DOUBLE_NL_BY_SYSTEM_1, DOUBLE_NL_BY_SYSTEM_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testMixedUnorderedOrderedListWithOrderedListAtTheEnd() {
        // Same elements test
        final Root sameRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final Root sameRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final Root unorderedRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_2, DOUBLE_NESTED_MIXED_LIST_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_2);
        final Root differentRoot2 = getMixedRoot(DOUBLE_NESTED_MIXED_LIST_1, DOUBLE_NESTED_MIXED_LIST_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testBySystemLeafList() {
        // Same elements test
        final RootListByUser sameRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final RootListByUser sameRootList2 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRootList1, sameRootList2);

        // Unordered elements test
        final RootListByUser unorderedRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final RootListByUser unorderedRootList2 = getRootListByUserWithLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRootList1, unorderedRootList2);

        // Different elements
        final RootListByUser differentRootList1 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_2);
        final RootListByUser differentRootList2 = getRootListByUserWithLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRootList1, differentRootList2);
    }

    @Test
    public void testByUserLeafList() {
        // Same elements test
        final RootListByUser sameRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final RootListByUser sameRootList2 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRootList1, sameRootList2);

        // Unordered elements test
        final RootListByUser unorderedRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final RootListByUser unorderedRootList2 = getRootListByUserWithLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRootList1, unorderedRootList2);

        // Different elements
        final RootListByUser differentRootList1 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_2);
        final RootListByUser differentRootList2 = getRootListByUserWithLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRootList1, differentRootList2);
    }

    @Test
    public void testNestedBySystemLeafList() {
        // Same elements test
        final Root sameRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root sameRoot2 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root unorderedRoot2 = getNestedLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root differentRoot2 = getNestedLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testNestedByUserLeafList() {
        // Same elements test
        final Root sameRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root sameRoot2 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root unorderedRoot2 = getNestedLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root differentRoot2 = getNestedLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testBySystemLeafListUnderContainer() {
        // Same elements test
        final Root sameRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root sameRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root unorderedRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_2, VALUE_1);
        assertEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_2);
        final Root differentRoot2 = getRootListBySystemWithNestedLeafListBySystem(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }

    @Test
    public void testByUserLeafListUnderContainer() {
        // Same elements test
        final Root sameRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root sameRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        assertEquals(sameRoot1, sameRoot2);

        // Unordered elements test
        final Root unorderedRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root unorderedRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_2, VALUE_1);
        assertNotEquals(unorderedRoot1, unorderedRoot2);

        // Different elements
        final Root differentRoot1 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_2);
        final Root differentRoot2 = getRootListBySystemWithNestedLeafListByUser(VALUE_1, VALUE_3);
        assertNotEquals(differentRoot1, differentRoot2);
    }


    private static Root getRootTripleList(final DoubleNestedListNoKey doubleNestedListNoKeysVal1,
            final DoubleNestedListNoKey doubleNestedListNoKeysVal2) {
        final NestedListByUser nestedListByUser1 = new NestedListByUserBuilder()
                .setName("nested1")
                .setDoubleNestedListNoKey(List.of(doubleNestedListNoKeysVal1, doubleNestedListNoKeysVal2))
                .build();
        final NestedListByUser nestedListByUser2 = new NestedListByUserBuilder()
                .setName("nested2")
                .setDoubleNestedListNoKey(List.of(doubleNestedListNoKeysVal2, doubleNestedListNoKeysVal1))
                .build();

        final RootListByUser rootListByUser1 = new RootListByUserBuilder()
                .setName("list1")
                .setNestedListByUser(List.of(nestedListByUser1, nestedListByUser2))
                .build();
        final RootListByUser rootListByUser2 = new RootListByUserBuilder()
                .setName("list1")
                .setNestedListByUser(List.of(nestedListByUser2, nestedListByUser1))
                .build();

        return new RootBuilder()
                .setRootListByUser(List.of(rootListByUser1, rootListByUser2))
                .build();
    }

    private static DoubleNestedListNoKey getDoubleNestedListNoKey(final String name) {
        return new DoubleNestedListNoKeyBuilder()
                .setName(name)
                .setType(name + "_type")
                .build();
    }

    private static Root getRootTripleOrderedList(final DoubleNestedListBySystem doubleNestedListBySystemVal1,
            final DoubleNestedListBySystem doubleNestedListBySystemVal2) {
        // Create the DoubleNestedListContainer instance
        final DoubleNestedListContainer doubleNestedListContainer1 = new DoubleNestedListContainerBuilder()
                .setDoubleNestedListBySystem(Map.of(doubleNestedListBySystemVal1.key(), doubleNestedListBySystemVal1,
                        doubleNestedListBySystemVal2.key(), doubleNestedListBySystemVal2))
                .build();
        final DoubleNestedListContainer doubleNestedListContainer2 = new DoubleNestedListContainerBuilder()
                .setDoubleNestedListBySystem(Map.of(doubleNestedListBySystemVal2.key(), doubleNestedListBySystemVal2,
                        doubleNestedListBySystemVal1.key(), doubleNestedListBySystemVal1))
                .build();

        // Create the NestedListBySystem instance
        final NestedListBySystem nestedListBySystem1 = new NestedListBySystemBuilder()
                .withKey(new NestedListBySystemKey("nested-list-by-system-1"))
                .setName("nested-list-by-system-1")
                .setType("nested-list-by-system-type-1")
                .setDoubleNestedListContainer(doubleNestedListContainer1)
                .build();
        final NestedListBySystem nestedListBySystem2 = new NestedListBySystemBuilder()
                .withKey(new NestedListBySystemKey("nested-list-by-system-2"))
                .setName("nested-list-by-system-2")
                .setType("nested-list-by-system-type-2")
                .setDoubleNestedListContainer(doubleNestedListContainer2)
                .build();

        // Create the NestedListContainer instance
        final NestedListContainer nestedListContainer1 = new NestedListContainerBuilder()
                .setNestedListBySystem(Map.of(nestedListBySystem1.key(), nestedListBySystem1,
                        nestedListBySystem2.key(), nestedListBySystem2))
                .build();
        final NestedListContainer nestedListContainer2 = new NestedListContainerBuilder()
                .setNestedListBySystem(Map.of(nestedListBySystem2.key(), nestedListBySystem2,
                        nestedListBySystem1.key(), nestedListBySystem1))
                .build();

        // Create the RootListBySystem instance
        final RootListBySystem rootListBySystem1 = new RootListBySystemBuilder()
                .withKey(new RootListBySystemKey("root-list-by-system-1"))
                .setName("root-list-by-system-1")
                .setType("root-list-by-system-type-1")
                .setNestedListContainer(nestedListContainer1)
                .build();
        final RootListBySystem rootListBySystem2 = new RootListBySystemBuilder()
                .withKey(new RootListBySystemKey("root-list-by-system-2"))
                .setName("root-list-by-system-2")
                .setType("root-list-by-system-type-2")
                .setNestedListContainer(nestedListContainer2)
                .build();

        return new RootBuilder()
                .setRootListBySystem(Map.of(rootListBySystem1.key(), rootListBySystem1,
                        rootListBySystem2.key(), rootListBySystem2))
                .build();
    }

    private static DoubleNestedListBySystem getDoubleNestedListBySystem(final String keyVal) {
        DoubleNestedListBySystem doubleNestedListBySystem = new DoubleNestedListBySystemBuilder()
                .withKey(new DoubleNestedListBySystemKey(keyVal))
                .setName(keyVal)
                .setType(keyVal + "_type")
                .build();
        return doubleNestedListBySystem;
    }

    private static Root getMixedRoot(final DoubleNestedMixedList doubleNestedMixedListVal1,
            final DoubleNestedMixedList doubleNestedMixedListVal2) {

        // add DoubleNestedMixedList object to NestedListNoKey
        final NestedListNoKey nestedListNoKey1 = new NestedListNoKeyBuilder()
                .setName("Nested List 1")
                .setType("Type 1")
                .setDoubleNestedMixedList(Map.of(doubleNestedMixedListVal1.key(), doubleNestedMixedListVal1,
                        doubleNestedMixedListVal2.key(), doubleNestedMixedListVal2))
                .build();

        // add DoubleNestedMixedList object to NestedListNoKey
        final NestedListNoKey nestedListNoKey2 = new NestedListNoKeyBuilder()
                .setName("Nested List 2")
                .setType("Type 2")
                .setDoubleNestedMixedList(Map.of(doubleNestedMixedListVal2.key(), doubleNestedMixedListVal2,
                        doubleNestedMixedListVal1.key(), doubleNestedMixedListVal1))
                .build();

        // create RootListByUser
        final RootListByUser rootListByUser1 = new RootListByUserBuilder()
                .withKey(new RootListByUserKey("list1"))
                .setName("list1")
                .setType("Type 1")
                .setNestedListNoKey(List.of(nestedListNoKey1, nestedListNoKey2))
                .build();

        // create RootListByUser
        final RootListByUser rootListByUser2 = new RootListByUserBuilder()
                .withKey(new RootListByUserKey("list2"))
                .setName("list2")
                .setType("Type 2")
                .setNestedListNoKey(List.of(nestedListNoKey2, nestedListNoKey1))
                .build();

        return new RootBuilder()
                .setRootListByUser(List.of(rootListByUser1, rootListByUser2))
                .build();
    }

    private static DoubleNestedMixedList getDoubleNestedMixedList(final String keyVal) {
        // create DoubleNestedMixedList object
        final DoubleNestedMixedList doubleNestedMixedList = new DoubleNestedMixedListBuilder()
                .withKey(new DoubleNestedMixedListKey(keyVal))
                .setName(keyVal)
                .setType(keyVal + "_type")
                .build();
        return doubleNestedMixedList;
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
                .setListByUserLeafListByUser((List.of(val1, val2)))
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
        final RootListBySystem rootListBySystem1 = getRootListBySystemWithLeafListBySystem(VALUE_2, val1, val2);
        final RootListBySystem rootListBySystem2 = getRootListBySystemWithLeafListBySystem(VALUE_3, val2, val1);
        return new RootBuilder()
                .setRootListBySystem(Map.of(rootListBySystem1.key(), rootListBySystem1,
                        rootListBySystem2.key(), rootListBySystem1))
                .build();
    }

    private static Root getRootListBySystemWithNestedLeafListByUser(final String val1, final String val2) {
        final RootListBySystem rootListBySystem1 = getRootListBySystemWithLeafListByUser(VALUE_2, val1, val2);
        final RootListBySystem rootListBySystem2 = getRootListBySystemWithLeafListByUser(VALUE_3, val2, val1);
        return new RootBuilder()
                .setRootListBySystem(Map.of(rootListBySystem1.key(), rootListBySystem1,
                        rootListBySystem2.key(), rootListBySystem1))
                .build();
    }


    private static Root getNestedLeafListBySystem(final String val1, final String val2) {
        final RootListByUser rootListByUser1 = getRootListByUserWithLeafListBySystem(val1, val2);
        final RootListByUser rootListByUser2 = getRootListByUserWithLeafListBySystem(val2, val1);
        return new RootBuilder()
                .setRootListByUser(List.of(rootListByUser1, rootListByUser2))
                .build();
    }

    private static Root getNestedLeafListByUser(final String val1, final String val2) {
        final RootListByUser rootListByUser1 = getRootListByUserWithLeafListByUser(val1, val2);
        final RootListByUser rootListByUser2 = getRootListByUserWithLeafListByUser(val2, val1);
        return new RootBuilder()
                .setRootListByUser(List.of(rootListByUser1, rootListByUser2))
                .build();
    }
}

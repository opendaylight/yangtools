/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.Cont;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.MyType;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

class CachingCodecTest extends AbstractBindingCodecTest {
    private static final NodeIdentifier TOP_LEVEL_LIST_ARG = new NodeIdentifier(TopLevelList.QNAME);
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final Map<TopLevelListKey, TopLevelList> TWO_LIST = createList(2);
    private static final Map<TopLevelListKey, TopLevelList> THREE_LIST = createList(3);

    private static final Top TOP_TWO_LIST_DATA = new TopBuilder().setTopLevelList(TWO_LIST).build();
    private static final Top TOP_THREE_LIST_DATA = new TopBuilder().setTopLevelList(THREE_LIST).build();

    private static final NodeIdentifier LEAF_ARG = new NodeIdentifier(QName.create(Cont.QNAME, "caching"));
    private static final InstanceIdentifier<Cont> CONT_PATH = InstanceIdentifier.create(Cont.class);

    private static final Cont CONT_DATA = new ContBuilder().setCaching(new MyType(dataValue())).setNonCaching("test")
            .build();
    private static final Cont CONT2_DATA = new ContBuilder().setCaching(new MyType(dataValue())).setNonCaching("test2")
            .build();

    private BindingDataObjectCodecTreeNode<Top> topNode;
    private BindingDataObjectCodecTreeNode<Cont> contNode;

    private static String dataValue() {
        // We are battling interning here
        return new StringBuilder("foo").toString();
    }

    @BeforeEach
    void beforeEach() {
        topNode = codecContext.getDataObjectCodec(TOP_PATH);
        contNode = codecContext.getDataObjectCodec(CONT_PATH);
    }

    private static Map<TopLevelListKey, TopLevelList> createList(final int num) {
        final var builder = ImmutableMap.<TopLevelListKey, TopLevelList>builderWithExpectedSize(num);
        for (int i = 0; i < num; i++) {
            final var key = new TopLevelListKey("test-" + i);
            builder.put(key, new TopLevelListBuilder().withKey(key).build());
        }
        return builder.build();
    }

    @Test
    void testListCache() {
        final var cachingCodec = createCachingCodec(TopLevelList.class);
        final var first = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_TWO_LIST_DATA));
        final var second = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_TWO_LIST_DATA));

        assertNotSame(first, second);
        assertEquals(first, second);
        verifyListItemSame(first, second);

        final var third = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_THREE_LIST_DATA));
        verifyListItemSame(first, third);
        verifyListItemSame(second, third);
    }

    @Test
    void testTopAndListCache() {
        final var cachingCodec = createCachingCodec(Top.class, TopLevelList.class);
        final var first = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_TWO_LIST_DATA));
        final var second = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_TWO_LIST_DATA));

        assertEquals(first, second);
        assertSame(first, second);

        final var third = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(TOP_THREE_LIST_DATA));
        verifyListItemSame(first, third);
    }

    @Test
    void testLeafCache() {
        // The integers should be distinct
        assertNotSame(CONT_DATA.getCaching().getValue(), CONT2_DATA.getCaching().getValue());

        final var cachingCodec = createContCachingCodec(Cont.class, MyType.class);
        final var firstCont = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(CONT_DATA));
        final var secondCont = assertInstanceOf(ContainerNode.class, cachingCodec.serialize(CONT2_DATA));

        assertNotEquals(firstCont, secondCont);

        final var first = assertInstanceOf(LeafNode.class, firstCont.childByArg(LEAF_ARG));
        final var second = assertInstanceOf(LeafNode.class, secondCont.childByArg(LEAF_ARG));

        // The leaf nodes are transient, but the values should be the same
        assertEquals(first, second);
        assertSame(first.body(), second.body());
    }

    @Test
    void testDefaultInvocation() {
        final var cachingCodec = createCachingCodec(Top.class, TopLevelList.class);

        final var input = new TopBuilder().build();
        assertNull(input.getTopLevelList());
        assertEquals(Map.of(), input.nonnullTopLevelList());

        final var dom = cachingCodec.serialize(input);
        final var output = cachingCodec.deserialize(dom);
        assertEquals(input, output);
        assertEquals(output, input);

        assertNull(output.getTopLevelList());
        assertEquals(Map.of(), output.nonnullTopLevelList());
    }

    @SafeVarargs
    private final BindingNormalizedNodeCachingCodec<Top> createCachingCodec(
            final Class<? extends DataObject>... classes) {
        return topNode.createCachingCodec(ImmutableSet.copyOf(classes));
    }

    @SafeVarargs
    private final BindingNormalizedNodeCachingCodec<Cont> createContCachingCodec(
            final Class<? extends BindingObject>... classes) {
        return contNode.createCachingCodec(ImmutableSet.copyOf(classes));
    }

    private static void verifyListItemSame(final ContainerNode firstTop, final ContainerNode secondTop) {
        final var initialNodes = getListItems(firstTop).body();
        final var secondMap = getListItems(secondTop);

        for (var initial : initialNodes) {
            final var second = secondMap.childByArg(initial.name());
            assertEquals(initial, second);
            assertSame(initial, second);
        }
    }

    private static MapNode getListItems(final ContainerNode top) {
        return assertInstanceOf(MapNode.class, top.getChildByArg(TOP_LEVEL_LIST_ARG));
    }
}

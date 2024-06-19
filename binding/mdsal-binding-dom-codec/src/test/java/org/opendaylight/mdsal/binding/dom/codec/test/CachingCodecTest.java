/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.Cont;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.leaf.caching.codec.rev190201.MyType;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class CachingCodecTest extends AbstractBindingCodecTest {

    private static final NodeIdentifier TOP_LEVEL_LIST_ARG = new NodeIdentifier(TopLevelList.QNAME);
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);
    private static final List<TopLevelList> TWO_LIST = createList(2);
    private static final List<TopLevelList> THREE_LIST = createList(3);

    private static final Top TOP_TWO_LIST_DATA = new TopBuilder().setTopLevelList(TWO_LIST).build();
    private static final Top TOP_THREE_LIST_DATA = new TopBuilder().setTopLevelList(THREE_LIST).build();

    private static final NodeIdentifier LEAF_ARG = new NodeIdentifier(QName.create(Cont.QNAME, "caching"));
    private static final InstanceIdentifier<Cont> CONT_PATH = InstanceIdentifier.create(Cont.class);
    private static final Cont CONT_DATA = new ContBuilder().setCaching(new MyType(1)).setNonCaching("test").build();
    private static final Cont CONT2_DATA = new ContBuilder().setCaching(new MyType(1)).setNonCaching("test2").build();

    private BindingDataObjectCodecTreeNode<Top> topNode;
    private BindingDataObjectCodecTreeNode<Cont> contNode;

    @Override
    @Before
    public void before() {
        super.before();
        topNode = registry.getCodecContext().getSubtreeCodec(TOP_PATH);
        contNode = registry.getCodecContext().getSubtreeCodec(CONT_PATH);
    }

    private static List<TopLevelList> createList(final int num) {

        final ImmutableList.Builder<TopLevelList> builder = ImmutableList.builder();
        for (int i = 0; i < num; i++) {
            final TopLevelListKey key = new TopLevelListKey("test-" + i);
            builder.add(new TopLevelListBuilder().withKey(key).build());
        }
        return builder.build();
    }

    @Test
    public void testListCache() {
        final BindingNormalizedNodeCachingCodec<Top> cachingCodec = createCachingCodec(TopLevelList.class);
        final NormalizedNode<?, ?> first = cachingCodec.serialize(TOP_TWO_LIST_DATA);
        final NormalizedNode<?, ?> second = cachingCodec.serialize(TOP_TWO_LIST_DATA);

        assertNotSame(first, second);
        assertEquals(first, second);
        verifyListItemSame(first, second);

        final NormalizedNode<?, ?> third = cachingCodec.serialize(TOP_THREE_LIST_DATA);
        verifyListItemSame(first, third);
        verifyListItemSame(second, third);
    }

    @Test
    public void testTopAndListCache() {
        final BindingNormalizedNodeCachingCodec<Top> cachingCodec = createCachingCodec(Top.class, TopLevelList.class);
        final NormalizedNode<?, ?> first = cachingCodec.serialize(TOP_TWO_LIST_DATA);
        final NormalizedNode<?, ?> second = cachingCodec.serialize(TOP_TWO_LIST_DATA);

        assertEquals(first, second);
        assertSame(first, second);

        final NormalizedNode<?, ?> third = cachingCodec.serialize(TOP_THREE_LIST_DATA);
        verifyListItemSame(first, third);
    }

    @Test
    public void testLeafCache() {
        final BindingNormalizedNodeCachingCodec<Cont> cachingCodec = createContCachingCodec(Cont.class, MyType.class);
        final NormalizedNode<?, ?> first = cachingCodec.serialize(CONT_DATA);
        final NormalizedNode<?, ?> second = cachingCodec.serialize(CONT2_DATA);

        assertNotEquals(first, second);
        verifyLeafItemSame(first, second);
    }

    @Test
    public void testDefaultInvocation() {
        final BindingNormalizedNodeCachingCodec<Top> cachingCodec = createCachingCodec(Top.class, TopLevelList.class);

        final Top input = new TopBuilder().build();
        assertNull(input.getTopLevelList());
        assertEquals(ImmutableList.of(), input.nonnullTopLevelList());

        final NormalizedNode<?, ?> dom = cachingCodec.serialize(input);
        final Top output = cachingCodec.deserialize(dom);
        assertTrue(input.equals(output));
        assertTrue(output.equals(input));

        assertNull(output.getTopLevelList());
        assertEquals(ImmutableList.of(), output.nonnullTopLevelList());
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

    private static void verifyListItemSame(final NormalizedNode<?, ?> firstTop, final NormalizedNode<?, ?> secondTop) {
        final Collection<MapEntryNode> initialNodes = getListItems(firstTop).getValue();
        final MapNode secondMap = getListItems(secondTop);

        for (final MapEntryNode initial : initialNodes) {
            final MapEntryNode second = secondMap.getChild(initial.getIdentifier()).get();
            assertEquals(initial, second);
            assertSame(initial, second);
        }
    }

    private static MapNode getListItems(final NormalizedNode<?, ?> top) {
        return (MapNode) ((DataContainerNode<?>) top).getChild(TOP_LEVEL_LIST_ARG).get();
    }

    private static void verifyLeafItemSame(final NormalizedNode<?, ?> firstCont,
            final NormalizedNode<?, ?> secondCont) {
        assertSame(((DataContainerNode<?>) firstCont).getChild(LEAF_ARG).get(),
                ((DataContainerNode<?>) secondCont).getChild(LEAF_ARG).get());
    }
}

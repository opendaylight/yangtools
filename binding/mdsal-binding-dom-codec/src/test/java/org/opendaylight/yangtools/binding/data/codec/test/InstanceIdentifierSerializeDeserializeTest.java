/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class InstanceIdentifierSerializeDeserializeTest extends AbstractBindingRuntimeTest{
    public static final String TOP_LEVEL_LIST_KEY_VALUE = "foo";

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier
            .builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES =
            BA_TOP_LEVEL_LIST.augmentation(TreeComplexUsesAugment.class);

    public static final QName TOP_QNAME = Top.QNAME;
    public static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    public static final QName TOP_LEVEL_LIST_KEY = QName.create(TOP_QNAME, "name");
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    public static final YangInstanceIdentifier BI_TOP_PATH = YangInstanceIdentifier.builder().node(TOP_QNAME).build();
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_PATH = BI_TOP_PATH.node(TOP_LEVEL_LIST_QNAME);
    public static final YangInstanceIdentifier BI_TOP_LEVEL_LIST_1_PATH = BI_TOP_LEVEL_LIST_PATH
            .node(new YangInstanceIdentifier.NodeIdentifierWithPredicates(TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY, TOP_LEVEL_LIST_KEY_VALUE));

    private BindingNormalizedNodeCodecRegistry registry;

    @Override
    @Before
    public void setup() {
        super.setup();
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }

    @Test
    public void testYangIIToBindingAwareII() {
        final InstanceIdentifier<?> instanceIdentifier = registry.fromYangInstanceIdentifier(BI_TOP_PATH);
        assertEquals(Top.class, instanceIdentifier.getTargetType());
    }

    @Test
    public void testYangIIToBindingAwareIIListWildcarded() {
        final InstanceIdentifier<?> instanceIdentifier = registry.fromYangInstanceIdentifier(BI_TOP_LEVEL_LIST_PATH);
        assertNull(instanceIdentifier);
    }

    @Test
    public void testYangIIToBindingAwareIIListWithKey() {
        final InstanceIdentifier<?> instanceIdentifier = registry.fromYangInstanceIdentifier(BI_TOP_LEVEL_LIST_1_PATH);
        final InstanceIdentifier.PathArgument last = Iterables.getLast(instanceIdentifier.getPathArguments());
        assertEquals(TopLevelList.class, instanceIdentifier.getTargetType());
        assertFalse(instanceIdentifier.isWildcarded());
        assertTrue(last instanceof InstanceIdentifier.IdentifiableItem);
        final Identifier<?> key = ((InstanceIdentifier.IdentifiableItem<?, ?>) last).getKey();
        assertEquals(TopLevelListKey.class, key.getClass());
        assertEquals(TOP_LEVEL_LIST_KEY_VALUE, ((TopLevelListKey)key).getName());
    }

    @Test
    public void testBindingAwareIIToYangIContainer() {
        final YangInstanceIdentifier yangInstanceIdentifier = registry.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final YangInstanceIdentifier.PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof YangInstanceIdentifier.NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIWildcard() {
        final YangInstanceIdentifier yangInstanceIdentifier = registry.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class));
        final YangInstanceIdentifier.PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof YangInstanceIdentifier.NodeIdentifier);
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIListWithKey() {
        final YangInstanceIdentifier yangInstanceIdentifier = registry.toYangInstanceIdentifier(
                InstanceIdentifier.create(Top.class).child(TopLevelList.class, TOP_FOO_KEY));
        final YangInstanceIdentifier.PathArgument lastPathArgument = yangInstanceIdentifier.getLastPathArgument();
        assertTrue(lastPathArgument instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates);
        assertTrue(((YangInstanceIdentifier.NodeIdentifierWithPredicates) lastPathArgument).getKeyValues().containsValue(TOP_LEVEL_LIST_KEY_VALUE));
        assertEquals(TopLevelList.QNAME, lastPathArgument.getNodeType());
    }

    @Test
    public void testBindingAwareIIToYangIIAugmentation() {
        final YangInstanceIdentifier.PathArgument lastArg = registry.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES).getLastPathArgument();
        assertTrue(lastArg instanceof YangInstanceIdentifier.AugmentationIdentifier);
    }

    @Test
    public void testBindingAwareIIToYangIILeafOnlyAugmentation() {
        final YangInstanceIdentifier.PathArgument leafOnlyLastArg = registry.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY).getLastPathArgument();
        assertTrue(leafOnlyLastArg instanceof YangInstanceIdentifier.AugmentationIdentifier);
        assertTrue(((YangInstanceIdentifier.AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
    }
}

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBrief;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBriefKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class InstanceIdentifierTest extends AbstractBindingCodecTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
            .augmentation(TreeLeafOnlyAugment.class);
    private static final InstanceIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST
            .augmentation(TreeComplexUsesAugment.class);
    private static final QName SIMPLE_VALUE_QNAME = QName.create(TreeComplexUsesAugment.QNAME, "simple-value");

    @Test
    public void testComplexAugmentationSerialization() {
        final YangInstanceIdentifier yangII = registry.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES);
        final PathArgument lastArg = yangII.getLastPathArgument();
        assertTrue("Last argument should be AugmentationIdentifier", lastArg instanceof AugmentationIdentifier);
        final InstanceIdentifier<?> bindingII = registry.fromYangInstanceIdentifier(yangII);
        assertEquals(BA_TREE_COMPLEX_USES, bindingII);
    }

    @Test
    public void testLeafOnlyAugmentationSerialization() {
        final PathArgument leafOnlyLastArg = registry.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY).getLastPathArgument();
        assertTrue("Last argument should be AugmentationIdentifier", leafOnlyLastArg instanceof AugmentationIdentifier);
        assertTrue(((AugmentationIdentifier) leafOnlyLastArg).getPossibleChildNames().contains(SIMPLE_VALUE_QNAME));
    }

    @Test
    public void testCamelCaseKeys() {
        final InstanceIdentifier<?> result = registry.fromYangInstanceIdentifier(YangInstanceIdentifier.create(
            NodeIdentifier.create(OspfStatLsdbBrief.QNAME),
            NodeIdentifierWithPredicates.of(OspfStatLsdbBrief.QNAME, ImmutableMap.of(
                QName.create(OspfStatLsdbBrief.QNAME, "AreaIndex"), 1,
                QName.create(OspfStatLsdbBrief.QNAME, "LsaType"), Uint8.valueOf(2),
                QName.create(OspfStatLsdbBrief.QNAME, "LsId"), 3,
                QName.create(OspfStatLsdbBrief.QNAME, "AdvRtr"), "foo"))));
        assertTrue(result instanceof KeyedInstanceIdentifier);
        final Identifier<?> key = ((KeyedInstanceIdentifier<?, ?>) result).getKey();
        assertEquals(new OspfStatLsdbBriefKey("foo", 1, 3, Uint8.valueOf(2)), key);
    }
}

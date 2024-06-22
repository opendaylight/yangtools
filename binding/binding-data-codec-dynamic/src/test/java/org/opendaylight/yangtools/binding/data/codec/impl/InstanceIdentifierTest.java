/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBrief;
import org.opendaylight.yang.gen.v1.mdsal._355.norev.OspfStatLsdbBriefKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.DataObjectWildcard;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public class InstanceIdentifierTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final DataObjectWildcard<TopLevelList> BA_TOP_LEVEL_LIST = DataObjectWildcard.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final DataObjectWildcard<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST
            .augmentation(TreeLeafOnlyAugment.class);
    private static final DataObjectWildcard<ListViaUses> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST
            .augmentation(TreeComplexUsesAugment.class).child(ListViaUses.class, new ListViaUsesKey("bar"));

    @Test
    public void testComplexAugmentationSerialization() {
        // augmentation child pointer fully recoverable after reverse transformation
        final YangInstanceIdentifier yii = codecContext.toYangInstanceIdentifier(BA_TREE_COMPLEX_USES);
        final var converted = codecContext.fromYangInstanceIdentifier(yii);
        assertEquals(BA_TREE_COMPLEX_USES, converted);
    }

    @Test
    public void testLeafOnlyAugmentationSerialization() {
        // augmentation only pointer translated to parent node being augmented,
        // because of augmentation only have no corresponding yang identifier
        final YangInstanceIdentifier yii = codecContext.toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
        final var converted = codecContext.fromYangInstanceIdentifier(yii);
        assertEquals(BA_TOP_LEVEL_LIST, converted);
    }

    @Test
    public void testCamelCaseKeys() {
        final var result = codecContext.fromYangInstanceIdentifier(YangInstanceIdentifier.of(
            NodeIdentifier.create(OspfStatLsdbBrief.QNAME),
            NodeIdentifierWithPredicates.of(OspfStatLsdbBrief.QNAME, ImmutableMap.of(
                QName.create(OspfStatLsdbBrief.QNAME, "AreaIndex"), 1,
                QName.create(OspfStatLsdbBrief.QNAME, "LsaType"), Uint8.valueOf(2),
                QName.create(OspfStatLsdbBrief.QNAME, "LsId"), 3,
                QName.create(OspfStatLsdbBrief.QNAME, "AdvRtr"), "foo"))));
        assertTrue(result instanceof DataObjectWildcard.WithKey);
        final var key = ((DataObjectWildcard.WithKey<?, ?>) result).getKey();
        assertEquals(new OspfStatLsdbBriefKey("foo", 1, 3, Uint8.valueOf(2)), key);
    }
}

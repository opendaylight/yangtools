/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public class InstanceIdentifierTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final DataObjectReference<TopLevelList> BA_TOP_LEVEL_LIST =
        DataObjectReference.builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final DataObjectReference<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST.toBuilder()
        .augmentation(TreeLeafOnlyAugment.class)
        .build();
    private static final DataObjectReference<ListViaUses> BA_TREE_COMPLEX_USES = BA_TOP_LEVEL_LIST.toBuilder()
        .augmentation(TreeComplexUsesAugment.class)
        .child(ListViaUses.class, new ListViaUsesKey("bar"))
        .build();

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
        final var key = assertInstanceOf(WithKey.class, result).getKey();
        assertEquals(new OspfStatLsdbBriefKey("foo", 1, 3, Uint8.valueOf(2)), key);
    }

    /**
     * @param class1
     * @param result
     * @return
     */
    private @NonNull KeyedInstanceIdentifier<TopLevelList, TopLevelListKey> assertInstanceOf(final Class<WithKey> class1,
        @Nullable
        final
        DataObjectReference<DataObject> result) {
        // TODO Auto-generated method stub
        return null;
    }
}

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.ThirdParty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.put.top.input.choice.list.choice.in.choice.list.ComplexViaUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.choice.list.choice.in.choice.list.ComplexViaUsesWithDifferentNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.ChoiceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.ChoiceListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.ChoiceListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.QName;

class CaseSubstitutionTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final ChoiceListKey CHOICE_FOO_KEY = new ChoiceListKey("foo");

    private static final DataObjectIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = DataObjectIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final DataObjectIdentifier<ChoiceList> BA_CHOICE_LIST = DataObjectIdentifier.builder(Top.class)
            .child(ChoiceList.class, CHOICE_FOO_KEY).build();
    private static final DataObjectIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY = BA_TOP_LEVEL_LIST.toBuilder()
            .augmentation(TreeLeafOnlyAugment.class)
            .build();
    private static final DataObjectIdentifier<TreeComplexUsesAugment> BA_TREE_COMPLEX_USES =
        BA_TOP_LEVEL_LIST.toBuilder().augmentation(TreeComplexUsesAugment.class).build();
    private static final QName SIMPLE_VALUE_QNAME = QName.create(ThirdParty.QNAME, "simple-value");

    @Test
    void choiceInGroupingSubstituted() {
        final var baRpc = new ChoiceListBuilder()
            .withKey(CHOICE_FOO_KEY)
            .setChoiceInChoiceList(new ComplexViaUsesWithDifferentNameBuilder(createComplexData()).build())
            .build();
        final var baTree = new ChoiceListBuilder()
            .withKey(CHOICE_FOO_KEY)
            .setChoiceInChoiceList(new ComplexViaUsesBuilder(createComplexData()).build())
            .build();
        final var domTreeEntry = codecContext.toNormalizedDataObject(BA_CHOICE_LIST, baTree).node();
        final var domRpcEntry = codecContext.toNormalizedDataObject(BA_CHOICE_LIST, baRpc).node();
        assertEquals(domTreeEntry, domRpcEntry);
    }

    private static RpcComplexUsesAugment createComplexData() {
        return new RpcComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build())
            .setListViaUses(Map.of())
            .build();
    }
}

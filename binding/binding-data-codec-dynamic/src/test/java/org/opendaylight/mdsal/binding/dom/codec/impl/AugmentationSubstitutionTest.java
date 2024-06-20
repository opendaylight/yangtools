/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.lib.InstanceIdentifier;

public class AugmentationSubstitutionTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = InstanceIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY).build();

    @Test
    public void augmentationInGroupingSubstituted() {
        final TopLevelList baRpc = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(new RpcComplexUsesAugmentBuilder(createComplexData()).build())
            .build();
        final TopLevelList baTree = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(new TreeComplexUsesAugmentBuilder(createComplexData()).build())
            .build();
        final var domTreeEntry = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, baTree).node();
        final var domRpcEntry = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, baRpc).node();
        assertEquals(domTreeEntry, domRpcEntry);
    }

    @Test
    public void copyBuilderWithAugmenationsTest() {
        final TopLevelList manuallyConstructed = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .addAugmentation(new TreeComplexUsesAugmentBuilder(createComplexData()).build())
            .build();

        final var result = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, manuallyConstructed);
        final TopLevelList deserialized =
            (TopLevelList) codecContext.fromNormalizedNode(result.path(), result.node()).getValue();
        assertEquals(manuallyConstructed, deserialized);
        final TopLevelList copiedFromDeserialized = new TopLevelListBuilder(deserialized).build();
        assertEquals(manuallyConstructed, copiedFromDeserialized);
    }

    private static RpcComplexUsesAugment createComplexData() {
        return new RpcComplexUsesAugmentBuilder()
                .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build())
                .build();
    }
}

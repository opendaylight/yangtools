/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.RpcComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ContainerWithUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.top.level.list.choice.in.list.EmptyLeaf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.top.top.level.list.choice.in.list.EmptyLeafBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;

class EmptyLeafTest extends AbstractBindingCodecTest {
    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final DataObjectIdentifier<TopLevelList> BA_TOP_LEVEL_LIST = DataObjectIdentifier.builder(Top.class)
            .child(TopLevelList.class, TOP_FOO_KEY)
            .build();

    @Test
    void testCaseWithEmptyLeafType() {
        final var withEmptyCase = new TopLevelListBuilder()
            .withKey(TOP_FOO_KEY)
            .setChoiceInList(new EmptyLeafBuilder().setEmptyType(Empty.value()).build())
            .build();
        final var dom = codecContext.toNormalizedDataObject(BA_TOP_LEVEL_LIST, withEmptyCase);
        final var readed = codecContext.fromNormalizedNode(dom.path(), dom.node());
        final var list = assertInstanceOf(EmptyLeaf.class, ((TopLevelList) readed.getValue()).getChoiceInList());
        assertNotNull(list.getEmptyType());
    }

    // FIXME: either remove this method or take advantage of it
    private static RpcComplexUsesAugment createComplexData() {
        return new RpcComplexUsesAugmentBuilder()
            .setContainerWithUses(new ContainerWithUsesBuilder().setLeafFromGrouping("foo").build())
            .setListViaUses(Map.of())
            .build();
    }
}

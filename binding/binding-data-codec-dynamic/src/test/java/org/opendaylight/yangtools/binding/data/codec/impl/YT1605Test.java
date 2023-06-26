/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainer;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.YangDataWithContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.container.ContainerFromYangData;
import org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.rev220222.yang.data.with.container.ContainerFromYangDataBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class YT1605Test extends AbstractBindingCodecTest {
    private static final NormalizedYangData NORMALIZED = ImmutableNodes.newYangDataBuilder(YangDataWithContainer.NAME)
        .setChild(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(ContainerFromYangData.QNAME))
            .withChild(ImmutableNodes.leafNode(QName.create(ContainerFromYangData.QNAME, "str"), "some string"))
            .build())
        .build();
    private static final YangDataWithContainer BINDING = new YangDataWithContainerBuilder()
        .setContainerFromYangData(new ContainerFromYangDataBuilder().setStr("some string").build())
        .build();

    @Test
    void testFromBinding() {
        assertEquals(NORMALIZED, codecContext.getYangDataCodec(BINDING.implementedInterface()).fromBinding(BINDING));
    }

    @Test
    void testToBinding() {
        assertEquals(BINDING, codecContext.getYangDataCodec(YangDataWithContainer.NAME).toBinding(NORMALIZED));
    }
}

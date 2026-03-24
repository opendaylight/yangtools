/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.yt1826.norev.Bar;
import org.opendaylight.yang.gen.v1.yt1826.norev.Baz;
import org.opendaylight.yang.gen.v1.yt1826.norev.BazBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer.NodeResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class YT1648Test extends AbstractBindingCodecTest {
    @Test
    void testToNormalized() throws Exception {
        final var baz = new BazBuilder()
            .setXyzzy(new Bar(Uint32.TEN))
            .build();

        final var result = assertInstanceOf(NodeResult.class,
            codecContext.toNormalizedNode(DataObjectIdentifier.builder(Baz.class).build(), baz));
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(Baz.QNAME))
            .withChild(ImmutableNodes.leafNode(QName.create(Baz.QNAME, "xyzzy"), Uint32.TEN))
            .build(), result.node());
    }
}

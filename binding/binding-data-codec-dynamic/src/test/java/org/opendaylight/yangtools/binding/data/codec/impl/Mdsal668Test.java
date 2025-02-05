/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.Bar;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

class Mdsal668Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);
    private static final DataObjectIdentifier<Foo> FOO_IID = InstanceIdentifier.create(Foo.class).toIdentifier();

    @Test
    void testLeaflistLeafref() {
        assertEquals(ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(FOO)
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                    // FIXME: YANGTOOLS-1602: these should get translated to YangInstanceIdentifier.of(FOO)
                    .withChild(ImmutableNodes.leafSetEntry(Bar.QNAME, FOO_IID))
                    .build())
                .build())
            .build(),
            codecContext.toNormalizedDataObject(FOO_IID,
                new FooBuilder().setBar(new BarBuilder().setBar(Set.of(FOO_IID)).build()).build()).node());
    }
}

/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer.NodeResult;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.Bar;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

public class Mdsal668Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);
    private static final InstanceIdentifier<Foo> FOO_IID = InstanceIdentifier.create(Foo.class);

    @Test
    public void testLeaflistLeafref() {
        assertEquals(Builders.containerBuilder()
            .withNodeIdentifier(FOO)
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                .withChild(Builders.leafSetBuilder()
                    .withNodeIdentifier(new NodeIdentifier(Bar.QNAME))
                    .withChild(Builders.leafSetEntryBuilder()
                        // FIXME: MDSAL-670: these should get translated to YangInstanceIdentifier.create(FOO)
                        .withNodeIdentifier(new NodeWithValue<>(Bar.QNAME, FOO_IID))
                        .withValue(FOO_IID)
                        .build())
                    .build())
                .build())
            .build(),
            ((NodeResult) codecContext.toNormalizedNode(FOO_IID,
                new FooBuilder().setBar(new BarBuilder().setBar(Set.of(FOO_IID)).build()).build())).node());
    }
}

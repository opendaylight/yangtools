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

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal670.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal670.norev.FooBuilder;
import org.opendaylight.yang.gen.v1.mdsal670.norev.One;
import org.opendaylight.yang.gen.v1.mdsal670.norev.OneBuilder;
import org.opendaylight.yang.gen.v1.mdsal670.norev.bar.Bar1Builder;
import org.opendaylight.yang.gen.v1.mdsal670.norev.bar.bar1.BarBuilder;
import org.opendaylight.yang.gen.v1.mdsal670.norev.baz.BazBuilder;
import org.opendaylight.yang.gen.v1.mdsal670.norev.foo.A1Builder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Mdsal670Test extends AbstractBindingCodecTest {
    private static final DataObjectIdentifier<Foo> FOO_IID = InstanceIdentifier.create(Foo.class).toIdentifier();
    private static final DataObjectIdentifier<One> ONE_IID = InstanceIdentifier.create(One.class).toIdentifier();

    @Test
    public void testLeafListLeafRefSame() {
        final var expected = codecContext.toNormalizedDataObject(FOO_IID, new FooBuilder()
            .setA1(new A1Builder()
                .setBar1(new Bar1Builder()
                    .setBar(new BarBuilder().setBar(Set.of(FOO_IID)).build())
                    .build())
                .build())
            .build());
        final var foo = assertInstanceOf(Foo.class,
            codecContext.fromNormalizedNode(expected.path(), expected.node()).getValue());
        assertEquals(foo.getA1().getBar1().getBar().getBar(), Set.of(FOO_IID));
    }

    @Test
    public void testLeafListLeafRefDifferent() {
        // This ideally wouldn't work and the codec would fail with string,
        // as the real referenced type is InstanceIdentifier
        // This shows that NOOP_CODEC was chosen since the referenced types in instances are different
        final var expected = codecContext.toNormalizedDataObject(ONE_IID,
                new OneBuilder().setBaz(new BazBuilder().setBaz(Set.of("ONE_IID")).build()).build());
        final var foo = (One) codecContext.fromNormalizedNode(expected.path(), expected.node()).getValue();
        assertEquals(foo.getBaz().getBaz(), Set.of("ONE_IID"));
    }
}

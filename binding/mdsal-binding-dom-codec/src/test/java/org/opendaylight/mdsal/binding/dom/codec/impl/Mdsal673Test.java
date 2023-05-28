/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.Bar;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

public class Mdsal673Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);
    private static final NodeIdentifier BAR = new NodeIdentifier(Bar.QNAME);

    /**
     * Test when BAR is not initialized (its {@code null}) the {@code nonnullBar} method returns its empty instance.
     */
    @Test
    public void testNonnullContainer() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO),
            Builders.containerBuilder().withNodeIdentifier(FOO).build());
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var obj = entry.getValue();
        assertThat(obj, instanceOf(Foo.class));
        final var foo = (Foo) obj;
        assertNull(foo.getBar());
        // We check if nonnullBar() returns empty Bar.
        // But we don't want to rely on provided builder in codec so the objects are not same.
        assertEquals(BarBuilder.empty(), foo.nonnullBar());
    }

    /**
     * Test when BAR is empty container the {@code getBar} and {@code nonnullBar} returns the same BAR instance.
     */
    @Test
    public void testEmptyContainer() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO),
            Builders.containerBuilder()
                .withNodeIdentifier(FOO)
                .withChild(Builders.containerBuilder().withNodeIdentifier(BAR).build())
                .build());
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var obj = entry.getValue();
        assertThat(obj, instanceOf(Foo.class));
        final var foo = (Foo) obj;
        final var bar = foo.getBar();
        assertNotNull(bar);
        assertSame(bar, foo.nonnullBar());
    }

    /**
     * Test when BAR is not empty container the {@code getBar} and {@code nonnullBar} returns the same BAR instance.
     */
    @Test
    public void testNotEmptyContainer() {
        // FIXME: MDSAL-670: these should get translated to YangInstanceIdentifier.of(FOO)
        final var identifier = new YangInstanceIdentifier.NodeWithValue<>(Bar.QNAME, FOO);
        final var data = Builders.containerBuilder()
            .withNodeIdentifier(FOO)
            .withChild(Builders.containerBuilder()
                .withNodeIdentifier(BAR)
                .withChild(Builders.leafSetBuilder()
                    .withNodeIdentifier(BAR)
                    .withChild(Builders.leafSetEntryBuilder().withNodeIdentifier(identifier).withValue(FOO).build())
                    .build())
                .build())
            .build();
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO), data);
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var obj = entry.getValue();
        assertThat(obj, instanceOf(Foo.class));
        final var foo = (Foo) obj;
        final var bar = foo.getBar();
        assertNotNull(bar);
        assertSame(bar, foo.nonnullBar());
    }
}

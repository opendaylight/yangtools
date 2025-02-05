/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal668.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.Bar;
import org.opendaylight.yang.gen.v1.mdsal668.norev.bar.BarBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class Mdsal673Test extends AbstractBindingCodecTest {
    private static final NodeIdentifier FOO = new NodeIdentifier(Foo.QNAME);
    private static final NodeIdentifier BAR = new NodeIdentifier(Bar.QNAME);

    /**
     * Test when BAR is not initialized (its {@code null}) the {@code nonnullBar} method returns its empty instance.
     */
    @Test
    void testNonnullContainer() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(FOO).build());
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var foo = assertInstanceOf(Foo.class, entry.getValue());
        assertNull(foo.getBar());
        // We check if nonnullBar() returns empty Bar.
        // But we don't want to rely on provided builder in codec so the objects are not same.
        assertEquals(BarBuilder.empty(), foo.nonnullBar());
    }

    /**
     * Test when BAR is empty container the {@code getBar} and {@code nonnullBar} returns the same BAR instance.
     */
    @Test
    void testEmptyContainer() {
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO),
            ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(FOO)
                .withChild(ImmutableNodes.newContainerBuilder().withNodeIdentifier(BAR).build())
                .build());
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var foo = assertInstanceOf(Foo.class, entry.getValue());
        final var bar = foo.getBar();
        assertNotNull(bar);
        assertSame(bar, foo.nonnullBar());
    }

    /**
     * Test when BAR is not empty container the {@code getBar} and {@code nonnullBar} returns the same BAR instance.
     */
    @Test
    public void testNotEmptyContainer() {
        // FIXME: YANGTOOLS-1602: these should get translated to YangInstanceIdentifier.of(FOO)
        final var data = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(FOO)
            .withChild(ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(BAR)
                .withChild(ImmutableNodes.newSystemLeafSetBuilder()
                    .withNodeIdentifier(BAR)
                    .withChild(ImmutableNodes.leafSetEntry(Bar.QNAME, FOO))
                    .build())
                .build())
            .build();
        final var entry = codecContext.fromNormalizedNode(YangInstanceIdentifier.of(FOO), data);
        assertNotNull(entry);
        assertEquals(InstanceIdentifier.create(Foo.class), entry.getKey());

        final var foo = assertInstanceOf(Foo.class, entry.getValue());
        final var bar = foo.getBar();
        assertNotNull(bar);
        assertSame(bar, foo.nonnullBar());
    }
}

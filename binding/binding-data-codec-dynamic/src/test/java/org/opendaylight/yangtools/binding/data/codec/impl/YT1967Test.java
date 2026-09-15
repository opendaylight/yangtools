/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.yt1967.bar.norev.bar.foo.choice.BarCaseBuilder;
import org.opendaylight.yang.gen.v1.yt1967.baz.norev.Baz;
import org.opendaylight.yang.gen.v1.yt1967.baz.norev.BazBuilder;
import org.opendaylight.yang.gen.v1.yt1967.foo.norev.grp.FooBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer.NodeResult;

class YT1967Test extends AbstractBindingCodecTest {
    @Test
    void testToNormalized() throws Exception {
        final var baz = new BazBuilder()
            .setFoo(new FooBuilder()
                .setChoice(new BarCaseBuilder()
                    .setBarLef("bar-value")
                    .build())
                .build())
            .build();

        final var result = assertInstanceOf(NodeResult.class,
            codecContext.toNormalizedNode(DataObjectIdentifier.builder(Baz.class).build(), baz));
        assertEquals("""
            containerNode (yt1967-baz)baz = {
                containerNode foo = {
                    choiceNode choice = {
                        leafNode bar-lef = "bar-value"
                    }
                }
            }""", result.node().prettyTree().toString());
    }
}

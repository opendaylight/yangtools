/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.Bar;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.BarBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.Foo;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint64;

public class IdentityKeyTest extends AbstractBindingCodecTest {
    @Test
    public void testIdentityKey() {
        final var expected = new BarBuilder()
            .setOne(Uint64.ONE)
            .setTwo(Foo.VALUE)
            .build();

        final var actual = thereAndBackAgain(InstanceIdentifier.builder(Bar.class, expected.key()).build(), expected);
        assertEquals(expected, actual);
        assertEquals(expected.key(), actual.key());
    }
}

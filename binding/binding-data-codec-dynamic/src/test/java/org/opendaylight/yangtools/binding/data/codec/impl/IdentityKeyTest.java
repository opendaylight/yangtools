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
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.Bar;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.BarBuilder;
import org.opendaylight.yang.gen.v1.urn.test.opendaylight.mdsal483.norev.Foo;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint64;

class IdentityKeyTest extends AbstractBindingCodecTest {
    @Test
    void testIdentityKey() {
        final var expected = new BarBuilder().setOne(Uint64.ONE).setTwo(Foo.VALUE).build();
        final var actual = thereAndBackAgain(DataObjectIdentifier.builder(Bar.class, expected.key()).build(), expected);
        assertEquals(expected, actual);
        assertEquals(expected.key(), actual.key());
    }
}

/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalUnsignedInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.OpendaylightDefaultValueTestData;
import org.opendaylight.yangtools.yang.common.Uint32;

class YT1677Test {
    @Test
    void testUnsafeAccessBypassesPatterns() {


    }

    @Test
    void testUnsafeAccessBypassesRange() {
        final var badValue = Uint32.valueOf(42);
        assertThrows(IllegalArgumentException.class, () -> new NormalUnsignedInteger(badValue));

        final var factory = OpendaylightDefaultValueTestData.META.unsafeAccess()
            .getUnsafeScalarTypeObjectFactory(new NormalUnsignedInteger(Uint32.valueOf(100_000)));

        final var untrusted = factory.newInstance(badValue);
        assertSame(badValue, untrusted.getValue());
    }
}

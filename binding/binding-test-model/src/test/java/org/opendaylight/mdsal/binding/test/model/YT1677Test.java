/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.MyString;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.NormalUnsignedInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.OpendaylightDefaultValueTestData;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.yang.common.Uint32;

class YT1677Test {
    private static final @NonNull UnsafeAccess UNSAFE_ACCESS = OpendaylightDefaultValueTestData.META.unsafeAccess();

    @Test
    void testUnsafeAccessBypassesLength() {
        final var badValue = "";
        final var ex = assertThrows(IllegalArgumentException.class, () -> new MyString(badValue));
        assertEquals("Invalid length: , expected: [[1..30]].", ex.getMessage());

        final var factory = UNSAFE_ACCESS.getUnsafeScalarTypeObjectFactory(new MyString(" "));
        final var untrusted = factory.newInstance(badValue);
        assertSame(badValue, untrusted.getValue());
    }

    @Test
    void testUnsafeAccessBypassesPattern() {
        final var badValue = "0";
        final var ex = assertThrows(IllegalArgumentException.class, () -> new MyString(badValue));
        assertEquals("Supplied value \"0\" does not match required pattern \"[a-zA-Z ]+\"", ex.getMessage());

        final var factory = UNSAFE_ACCESS.getUnsafeScalarTypeObjectFactory(new MyString(" "));
        final var untrusted = factory.newInstance(badValue);
        assertSame(badValue, untrusted.getValue());
    }

    @Test
    void testUnsafeAccessBypassesRange() {
        final var badValue = Uint32.valueOf(42);
        final var ex = assertThrows(IllegalArgumentException.class, () -> new NormalUnsignedInteger(badValue));
        assertEquals("Invalid range: 42, expected: [[70000..4200000000]].", ex.getMessage());

        final var factory = UNSAFE_ACCESS.getUnsafeScalarTypeObjectFactory(
            new NormalUnsignedInteger(Uint32.valueOf(100_000)));
        final var untrusted = factory.newInstance(badValue);
        assertSame(badValue, untrusted.getValue());
    }
}

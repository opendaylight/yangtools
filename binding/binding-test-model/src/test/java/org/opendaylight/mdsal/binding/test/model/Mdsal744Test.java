/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.MyBits;

public class Mdsal744Test {
    @ParameterizedTest
    @MethodSource("values")
    public void testBitsToString(final String expected, final boolean zero, final boolean one) {
        assertEquals(expected, new MyBits(one, false, zero).toString());
    }

    static Stream<Arguments> values() {
        return Stream.of(
            Arguments.of("MyBits{}", false, false),
            Arguments.of("MyBits{bitOne}", false, true),
            Arguments.of("MyBits{bitZero}", true, false),
            Arguments.of("MyBits{bitZero, bitOne}", true, true));
    }
}

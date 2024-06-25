/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Bug1862Test {
    @Test
    void restrictedTypedefTransformationTest() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(Bug1862Test.class,
            "/base-yang-types.yang", "/test-type-provider.yang"));
        assertEquals(35, types.size());
        final var fooGetter = types.stream()
            .filter(type -> type.getFullyQualifiedName().equals(
                "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.Foo"))
            .findFirst().orElseThrow()
            .getMethodDefinitions().stream()
            .filter(method -> method.getName().equals("getBug1862RestrictedTypedef"))
            .findFirst().orElseThrow();

        final var returnType = assertInstanceOf(GeneratedTransferObject.class, fooGetter.getReturnType());
        assertEquals(ImmutableRangeSet.of(Range.closed((byte) 1, (byte) 100)),
            returnType.getRestrictions().getRangeConstraint().orElseThrow().getAllowedRanges());
    }
}
/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug1862Test {
    @Test
    public void restrictedTypedefTransformationTest() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResources(Bug1862Test.class,
            "/base-yang-types.yang", "/test-type-provider.yang"));
        assertEquals(35, types.size());
        final MethodSignature fooGetter = types.stream()
            .filter(type -> type.getFullyQualifiedName().equals(
                "org.opendaylight.yang.gen.v1.urn.opendaylight.org.test.type.provider.model.rev140912.Foo"))
            .findFirst().orElseThrow()
            .getMethodDefinitions().stream()
            .filter(method -> method.getName().equals("getBug1862RestrictedTypedef"))
            .findFirst().orElseThrow();

        final Type returnType = fooGetter.getReturnType();
        assertThat(returnType, instanceOf(GeneratedTransferObject.class));
        final RangeSet<?> range = ((GeneratedTransferObject) returnType).getRestrictions().getRangeConstraint()
            .orElseThrow().getAllowedRanges();
        assertEquals(ImmutableRangeSet.of(Range.closed((byte) 1, (byte) 100)), range);
    }
}
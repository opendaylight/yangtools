/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesStringTest {
    @Test
    void constantGenerationTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-string-demo.yang"));

        final var genTO = assertInstanceOf(ScalarTypeObjectArchetype.class, genTypes.stream()
            .filter(type -> type.simpleName().equals("TypedefString"))
            .findFirst()
            .orElseThrow());

        final var restrictions = genTO.getRestrictions();
        assertFalse(restrictions.isEmpty());

        final var length = restrictions.getLengthConstraint().orElseThrow();
        assertEquals(ImmutableRangeSet.of(Range.closed(40, 40)), length.getAllowedRanges());

        final var patterns = restrictions.getPatternConstraints();
        assertEquals(3, patterns.size());
    }
}

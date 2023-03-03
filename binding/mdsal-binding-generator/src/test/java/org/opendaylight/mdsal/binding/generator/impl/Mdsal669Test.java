/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal669Test {
    private static final BindingRuntimeTypes RUNTIME_TYPES = new DefaultBindingRuntimeGenerator()
        .generateTypeMapping(YangParserTestUtils.parseYangResource("/mdsal669.yang"));

    @Test
    void barIsUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Bar"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Foo"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Target1"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev.used.augmented", "ToBeAugmented1"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev.used.augmented.indirect",
                "ToBeAugmented1"));
    }

    @Test
    void bazIsUsedByOneAndTwo() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Baz"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "One"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Two"));
    }

    @Test
    void unusedIsNotUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "Unused"));
    }

    @Test
    void fooAsStringIsNotUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "FooAsString"));
    }

    @Test
    void unusedBarIsNotUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedBar"));
    }

    @Test
    void unusedAugmendIsNotUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedAugmented"));
    }

    @Test
    void unusedIntermediateAugmentedIsNotUsed() {
        assertInstances(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedIntermediateAugmentedUser"));
        assertInstances(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedIntermediateAugmented"));
    }

    @Test
    void usedAugmentedIndirectIsUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectGrp"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"));
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirect"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"));
    }

    @Test
    void usedAugmentedIsUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmented"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedUser"));
    }

    @Test
    void toBeAugmentedIsUsed() {
        assertInstances(JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "ToBeAugmented"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedUser"));
    }

    private static void assertInstances(final JavaTypeName groupingTypeName, final JavaTypeName... instanceTypeNames) {
        assertEquals(
            Arrays.stream(instanceTypeNames).map(Mdsal669Test::assertType).collect(Collectors.toSet()),
            Set.copyOf(assertGrouping(groupingTypeName).instantiations()));
    }

    private static GroupingRuntimeType assertGrouping(final JavaTypeName typeName) {
        return assertInstanceOf(GroupingRuntimeType.class, assertType(typeName));
    }

    private static RuntimeType assertType(final JavaTypeName typeName) {
        return RUNTIME_TYPES.findSchema(typeName).orElseThrow();
    }
}

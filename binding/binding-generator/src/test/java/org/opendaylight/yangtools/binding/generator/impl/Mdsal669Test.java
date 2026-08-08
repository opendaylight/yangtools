/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal669Test {
    private static final BindingRuntimeTypes RUNTIME_TYPES = new DefaultBindingRuntimeGenerator()
        .generateTypeMapping(YangParserTestUtils.parseYangResource("/mdsal669.yang"));

    @Test
    void barIsUsed() {
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Bar"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Foo"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Target1"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev.used.augmented", "ToBeAugmented1"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev.used.augmented.indirect", "ToBeAugmented1"));
    }

    @Test
    void bazIsUsedByOneAndTwo() {
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Baz"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "One"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Two"));
    }

    @Test
    void unusedIsNotUsed() {
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "Unused"));
    }

    @Test
    void fooAsStringIsNotUsed() {
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "FooAsString"));
    }

    @Test
    void unusedBarIsNotUsed() {
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedBar"));
    }

    @Test
    void unusedAugmendIsNotUsed() {
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedAugmented"));
    }

    @Test
    void unusedIntermediateAugmentedIsNotUsed() {
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedIntermediateAugmentedUser"));
        assertInstances(TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UnusedIntermediateAugmented"));
    }

    @Test
    void usedAugmentedIndirectIsUsed() {
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectGrp"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"));
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirect"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"));
    }

    @Test
    void usedAugmentedIsUsed() {
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmented"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedUser"));
    }

    @Test
    void toBeAugmentedIsUsed() {
        assertInstances(
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "ToBeAugmented"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedIndirectUser"),
            TypeName.of("org.opendaylight.yang.gen.v1.mdsal669.norev", "UsedAugmentedUser"));
    }

    @NonNullByDefault
    private static void assertInstances(final TypeName groupingTypeName, final TypeName... instanceTypeNames) {
        assertEquals(
            Arrays.stream(instanceTypeNames).map(Mdsal669Test::assertType).collect(Collectors.toSet()),
            Set.copyOf(assertGrouping(groupingTypeName).instantiations()));
    }

    @NonNullByDefault
    private static GroupingRuntimeType assertGrouping(final TypeName typeName) {
        return assertInstanceOf(GroupingRuntimeType.class, assertType(typeName));
    }

    @NonNullByDefault
    private static RuntimeType assertType(final TypeName typeName) {
        final var ret = RUNTIME_TYPES.lookupRuntimeType(typeName);
        assertNotNull(ret);
        return ret;
    }
}

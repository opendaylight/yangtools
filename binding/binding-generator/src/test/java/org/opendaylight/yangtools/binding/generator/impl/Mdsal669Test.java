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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ModulePackageName;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.yangtools.binding.runtime.api.GroupingRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal669Test {
    private static final @NonNull BindingRuntimeTypes RUNTIME_TYPES = new DefaultBindingRuntimeGenerator()
        .generateTypeMapping(YangParserTestUtils.parseYangResource("/mdsal669.yang"));
    private static final @NonNull ModulePackageName MDSAL669_PKG = ModulePackageName.of(QNameModule.of("mdsal669"));

    @Test
    void ensureRightPackage() {
        assertEquals("org.opendaylight.yang.gen.v1.mdsal669.norev", MDSAL669_PKG.toString());
    }

    @Test
    void barIsUsed() {
        assertInstances(
            TypeName.of(MDSAL669_PKG, "Bar"),
            TypeName.of(MDSAL669_PKG, "Foo"),
            TypeName.of(MDSAL669_PKG, "Target1"),
            TypeName.of(MDSAL669_PKG.subPackage("used.augmented"), "ToBeAugmented1"),
            TypeName.of(MDSAL669_PKG.subPackage("used.augmented.indirect"), "ToBeAugmented1"));
    }

    @Test
    void bazIsUsedByOneAndTwo() {
        assertInstances(
            TypeName.of(MDSAL669_PKG, "Baz"),
            TypeName.of(MDSAL669_PKG, "One"),
            TypeName.of(MDSAL669_PKG, "Two"));
    }

    @Test
    void unusedIsNotUsed() {
        assertInstances(TypeName.of(MDSAL669_PKG, "Unused"));
    }

    @Test
    void fooAsStringIsNotUsed() {
        assertInstances(TypeName.of(MDSAL669_PKG, "FooAsString"));
    }

    @Test
    void unusedBarIsNotUsed() {
        assertInstances(TypeName.of(MDSAL669_PKG, "UnusedBar"));
    }

    @Test
    void unusedAugmendIsNotUsed() {
        assertInstances(TypeName.of(MDSAL669_PKG, "UnusedAugmented"));
    }

    @Test
    void unusedIntermediateAugmentedIsNotUsed() {
        assertInstances(TypeName.of(MDSAL669_PKG, "UnusedIntermediateAugmentedUser"));
        assertInstances(TypeName.of(MDSAL669_PKG, "UnusedIntermediateAugmented"));
    }

    @Test
    void usedAugmentedIndirectIsUsed() {
        assertInstances(
            TypeName.of(MDSAL669_PKG, "UsedAugmentedIndirectGrp"),
            TypeName.of(MDSAL669_PKG, "UsedAugmentedIndirectUser"));
        assertInstances(
            TypeName.of(MDSAL669_PKG, "UsedAugmentedIndirect"),
            TypeName.of(MDSAL669_PKG, "UsedAugmentedIndirectUser"));
    }

    @Test
    void usedAugmentedIsUsed() {
        assertInstances(
            TypeName.of(MDSAL669_PKG, "UsedAugmented"),
            TypeName.of(MDSAL669_PKG, "UsedAugmentedUser"));
    }

    @Test
    void toBeAugmentedIsUsed() {
        assertInstances(
            TypeName.of(MDSAL669_PKG, "ToBeAugmented"),
            TypeName.of(MDSAL669_PKG, "UsedAugmentedIndirectUser"),
            TypeName.of(MDSAL669_PKG, "UsedAugmentedUser"));
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

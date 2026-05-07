/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.ModulePackageName;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
class Mdsal552Test {
    private static final ModulePackageName MDSAL552 = ModulePackageName.of(QNameModule.of("mdsal552"));
    private static final TypeName BAR_INPUT = TypeName.of(MDSAL552, "BarInput");
    private static final TypeName BAZ = TypeName.of(MDSAL552, "Baz");
    private static final TypeName ENUMERATION = TypeName.of(MDSAL552, "Mdsal552Data").createEnclosed("Foo");

    @Test
    void enumLeafrefTest() {
        final var types = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal552.yang"));
        assertNotNull(types);
        assertEquals(5, types.size());

        final var baz = types.stream()
            .filter(type -> BAZ.equals(type.name()))
            .findFirst()
            .map(ContainerObjectArchetype.class::cast)
            .orElseThrow();
        final var bazGetRef = baz.getters().stream()
            .filter(method -> method.suffix().equals("Ref"))
            .findFirst().orElseThrow();
        assertEquals(ENUMERATION, bazGetRef.returnType().name());

        final var input = types.stream()
            .filter(type -> BAR_INPUT.equals(type.name()))
            .findFirst()
            .map(RpcInputArchetype.class::cast)
            .orElseThrow();
        final var inputGetRef = input.getters().stream()
            .filter(method -> method.suffix().equals("Ref"))
            .findFirst().orElseThrow();
        assertEquals(ENUMERATION, inputGetRef.returnType().name());
    }
}

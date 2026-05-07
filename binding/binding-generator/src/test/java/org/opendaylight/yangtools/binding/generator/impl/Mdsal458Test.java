/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.ModulePackageName;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal458Test {
    private static final ModulePackageName MDSAL458 = ModulePackageName.of(QNameModule.of("mdsal458"));

    @Test
    void testNestedClassFallback() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal458.yang"));
        assertEquals(2, types.size());

        final var typeNames = types.stream().map(Archetype::name).collect(Collectors.toSet());
        assertEquals(Set.of(TypeName.of(MDSAL458, "ExportedTo"), TypeName.of(MDSAL458, "Mdsal458Data")), typeNames);
    }
}

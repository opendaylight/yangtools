/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal459Test {
    @Test
    void testAugmentedAction() {
        final var types = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/mdsal-459/"));
        assertNotNull(types);
        assertEquals(7, types.size());

        final var typeNames = types.stream().map(GeneratedType::getIdentifier).collect(Collectors.toSet());
        assertEquals(Set.of(
            JavaTypeName.create("org.opendaylight.yang.gen.v1.base.norev", "Foo"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.base.norev", "BaseData"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.aug.norev", "AugData"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.aug.norev", "Foo1"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.aug.norev.foo", "Bar"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.aug.norev.foo", "BarOutput"),
            JavaTypeName.create("org.opendaylight.yang.gen.v1.aug.norev.foo", "BarInput")), typeNames);
    }
}

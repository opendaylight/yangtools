/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal320Test {
    @Test
    void mdsal320Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal320.yang"));
        assertEquals(2, generateTypes.size());

        final var foo = generateTypes.stream()
            .filter(type -> type.canonicalName().equals("org.opendaylight.yang.gen.v1.urn.odl.yt320.norev.Foo"))
            .findFirst()
            .map(ContainerObjectArchetype.class::cast)
            .orElseThrow();

        assertEquals(List.of(), foo.partials());

        final var fooTypes = foo.typeObjects();
        assertEquals(1, fooTypes.size());

        final var bar = assertInstanceOf(UnionTypeObjectArchetype.class, fooTypes.getFirst());
        assertEquals("Bar", bar.simpleName());

        final var barTypes = bar.enclosedTypes();
        assertEquals(2, barTypes.size());
        final var enum1 = assertInstanceOf(EnumTypeObjectArchetype.class, barTypes.getFirst());
        assertEquals("Enumeration", enum1.simpleName());
        final var bar1 = assertInstanceOf(UnionTypeObjectArchetype.class, barTypes.getLast());
        assertEquals("Bar$1", bar1.simpleName());

        final var fooGetters = foo.getters();
        assertEquals(1, fooGetters.size());

        final var getBar = fooGetters.getFirst();
        final var getBarType = assertInstanceOf(UnionTypeObjectArchetype.class, getBar.returnType());
        assertEquals(bar, getBarType);

        assertEquals(List.of("enumeration", "string", "bar$1"), bar.typePropertyNames());
        assertEquals(List.of(enum1, BaseYangTypes.STRING_TYPE, bar1), bar.typePropertyTypes());
    }
}

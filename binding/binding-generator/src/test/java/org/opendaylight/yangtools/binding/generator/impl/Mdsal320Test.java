/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal320Test {
    @Test
    void mdsal320Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal320.yang"));
        assertEquals(2, generateTypes.size());

        final var foo = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.odl.yt320.norev.Foo")).findFirst().orElseThrow();

        final var fooTypes = foo.getEnclosedTypes();
        assertEquals(1, fooTypes.size());

        final var bar = assertInstanceOf(GeneratedTransferObject.class, fooTypes.getFirst());
        assertEquals("Bar", bar.getName());
        assertTrue(bar.isUnionType());

        final var barTypes = bar.getEnclosedTypes();
        assertEquals(1, barTypes.size());

        final var bar1 = assertInstanceOf(GeneratedTransferObject.class, barTypes.getFirst());
        assertEquals("Bar$1", bar1.getName());
        assertTrue(bar1.isUnionType());

        final var it = foo.getMethodDefinitions().iterator();
        assertTrue(it.hasNext());
        final var getImplIface = it.next();
        assertEquals("implementedInterface", getImplIface.getName());
        assertTrue(getImplIface.isDefault());
        assertTrue(it.hasNext());

//        final var bindingHashCode = it.next();
//        assertEquals(Naming.BINDING_HASHCODE_NAME, bindingHashCode.getName());
//        final var bindingEquals = it.next();
//        assertEquals(Naming.BINDING_EQUALS_NAME, bindingEquals.getName());
//        final var bindingToString = it.next();
//        assertEquals(Naming.BINDING_TO_STRING_NAME, bindingToString.getName());
        final var getBar = it.next();
        final var getBarType = assertInstanceOf(GeneratedTransferObject.class, getBar.getReturnType());
        assertTrue(getBarType.isUnionType());
        assertEquals(bar, getBarType);
        final var requireBar = it.next();
        assertThat(requireBar.getName()).startsWith(Naming.REQUIRE_PREFIX);
        assertFalse(it.hasNext());

        final var bar1Prop = bar.getProperties().stream().filter(prop -> "bar$1".equals(prop.getName()))
                .findFirst().orElseThrow();
        assertEquals(bar1, bar1Prop.getReturnType());
    }
}

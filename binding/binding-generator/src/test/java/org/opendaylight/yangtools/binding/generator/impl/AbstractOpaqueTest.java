/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.OpaqueObjectArchetype;
import org.opendaylight.yangtools.yang.common.QNameModule;

abstract class AbstractOpaqueTest {

    static final void assertOpaqueNode(final List<GeneratedType> types, final String ns, final String pkg,
            final String name) {
        final var typeName = JavaTypeName.create("org.opendaylight.yang.gen.v1." + ns + ".norev" + pkg, name);
        final var optType = types.stream().filter(t -> typeName.equals(t.name())).findFirst();
        assertTrue(optType.isPresent());
        final var archetype = assertInstanceOf(OpaqueObjectArchetype.class, optType.orElseThrow());
        assertEquals(QNameModule.of(ns), archetype.qnameConstant().getModule());
    }
}

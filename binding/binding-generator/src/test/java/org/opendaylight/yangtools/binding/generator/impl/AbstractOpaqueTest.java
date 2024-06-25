/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;

abstract class AbstractOpaqueTest {

    static void assertOpaqueNode(final List<GeneratedType> types, final String ns, final String pkg,
            final String name) {
        final var typeName = JavaTypeName.create("org.opendaylight.yang.gen.v1." + ns + ".norev" + pkg, name);
        final var optType = types.stream().filter(t -> typeName.equals(t.getIdentifier())).findFirst();
        assertTrue(optType.isPresent());
        final var genType = optType.orElseThrow();
        final var it = genType.getImplements().iterator();
        final var first = assertInstanceOf(ParameterizedType.class, it.next());
        assertEquals(JavaTypeName.create(OpaqueObject.class), first.getRawType().getIdentifier());

        final var second = assertInstanceOf(ParameterizedType.class, it.next());
        assertEquals(JavaTypeName.create(ChildOf.class), second.getRawType().getIdentifier());

        assertFalse(it.hasNext());
    }
}

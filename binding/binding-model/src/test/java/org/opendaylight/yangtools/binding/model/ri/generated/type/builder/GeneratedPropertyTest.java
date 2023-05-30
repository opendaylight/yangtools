/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedPropertyTest {
    @Test
    void testMethodsForGeneratedPropertyBuilderImpl() {
        final var propertyBuilderImpl = new GeneratedPropertyBuilderImpl("testProperty")
            .setReturnType(Types.STRING)
            .setValue("new value")
            .setReadOnly(true);

        final var genProperty = propertyBuilderImpl.toInstance();
        assertNotNull(genProperty);

        assertNotNull(propertyBuilderImpl.toString());
    }

    @Test
    void testMethodsForGeneratedPropertyImpl() {
        final var propertyImpl = new GeneratedPropertyImpl("Test", List.of(),
            TypeMemberComment.contractOf("test property"), AccessModifier.PRIVATE, Types.VOID, true, true, true,
            "test value");

        assertEquals("test value", propertyImpl.getValue());
        assertTrue(propertyImpl.isReadOnly());
        assertNotNull(propertyImpl.toString());
    }
}

/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.EnumPairBuilder;

@ExtendWith(MockitoExtension.class)
class EnumTypeObjectArchetypeTest {
    @Mock
    private TypeEffectiveStatement.MandatoryIn<?, ?> statement;

    @Test
    void valueToTypesTrivial() {
        final var definition = BaseTypes.enumerationTypeBuilder(QName.create("test", "test"))
            .addEnum(EnumPairBuilder.create("SomeName", 42)
                .setDescription("Some Other Description")
                .setReference("Some other reference")
                .build())
            .build();
        assertEquals(Map.of(definition.getValues().getFirst(), "SomeName"),
            new EnumTypeObjectArchetype(JavaTypeName.create("foo", "bar"), statement, definition).valueToConstant());
    }
}

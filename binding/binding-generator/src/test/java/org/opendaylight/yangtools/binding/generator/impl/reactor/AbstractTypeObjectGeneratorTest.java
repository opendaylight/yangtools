/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenScalarTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@ExtendWith(MockitoExtension.class)
class AbstractTypeObjectGeneratorTest {
    @Mock
    private TypeDefinition<?> typedef;

    private final CodegenGeneratedTOBuilder builder =
        new CodegenScalarTypeObjectArchetypeBuilder(JavaTypeName.create("test.package", "TestBuilder"));

    @Test
    void testAddUnits() {
        doReturn(Optional.of("125")).when(typedef).getUnits();

        AbstractTypeObjectGenerator.addUnits(builder, typedef);
        final var genTO = builder.build();
        final var constants = genTO.getConstantDefinitions();
        assertEquals(1, constants.size());
        final var constant = constants.getFirst();
        assertEquals("UNITS", constant.getName());
        assertEquals("\"125\"", constant.getValue());
    }

    @Test
    void testAddUnitsNonExistent() {
        doReturn(Optional.empty()).when(typedef).getUnits();

        AbstractTypeObjectGenerator.addUnits(builder, typedef);
        var genTO = builder.build();
        assertEquals(List.of(), genTO.getConstantDefinitions());

    }

    @Test
    void testAddUnitsEmpty() {
        doReturn(Optional.of("")).when(typedef).getUnits();

        AbstractTypeObjectGenerator.addUnits(builder, typedef);
        var genTO = builder.build();
        assertEquals(List.of(), genTO.getConstantDefinitions());
    }
}

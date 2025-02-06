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
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@ExtendWith(MockitoExtension.class)
class GeneratorTest {
    @Mock
    private TypeDefinition<?> typedef;

    private final GeneratedTOBuilder builder =
        new CodegenGeneratedTOBuilder(JavaTypeName.create("test.package", "TestBuilder"));

    @Test
    void testAddUnits() {
        doReturn(Optional.of("125")).when(typedef).getUnits();

        Generator.addUnits(builder, typedef);
        final var genTO = builder.build();
        assertEquals(1, genTO.getConstantDefinitions().size());
        assertEquals("_UNITS", genTO.getConstantDefinitions().get(0).getName());
        assertEquals(genTO.getConstantDefinitions().get(0).getValue(), "\"125\"");
    }

    @Test
    void testAddUnitsNonExistent() {
        doReturn(Optional.empty()).when(typedef).getUnits();

        Generator.addUnits(builder, typedef);
        var genTO = builder.build();
        assertEquals(List.of(), genTO.getConstantDefinitions());

    }

    @Test
    void testAddUnitsEmpty() {
        doReturn(Optional.of("")).when(typedef).getUnits();

        Generator.addUnits(builder, typedef);
        var genTO = builder.build();
        assertEquals(List.of(), genTO.getConstantDefinitions());
    }
}

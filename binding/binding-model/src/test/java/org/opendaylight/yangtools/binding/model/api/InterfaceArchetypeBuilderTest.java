/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@Deprecated(forRemoval = true)
@ExtendWith(MockitoExtension.class)
class InterfaceArchetypeBuilderTest {
    @Mock
    private ContainerEffectiveStatement statement;

    @Test
    @Deprecated(forRemoval = true)
    void addEnclosingTransferObjectIllegalArgumentTest2() {
        final var builder = ContainerObjectArchetype.builder(JavaTypeName.create("my.package", "MyName"), statement,
            JavaTypeName.create("my.package", "MyParent"), List.of());
        assertThrows(NullPointerException.class, () -> builder.addEnclosedType(null));
    }
}

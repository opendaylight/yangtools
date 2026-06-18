/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;

@ExtendWith(MockitoExtension.class)
class ClassTemplateTest {
    @Mock
    private ScalarTypeObjectArchetype enclosedType;
    @Mock
    private ScalarTypeObjectArchetype superType;
    @Mock
    private GeneratedProperty property;

    @Test
    void getPropertiesOfAllParentsTest() {
        doReturn(List.of(property)).when(enclosedType).getProperties();
        doReturn(Boolean.TRUE).when(property).isReadOnly();
        doReturn(enclosedType).when(superType).getSuperType();
        assertThat(UnionTypeObjectTemplate.propertiesOfAllParents(superType)).contains(property);
    }
}

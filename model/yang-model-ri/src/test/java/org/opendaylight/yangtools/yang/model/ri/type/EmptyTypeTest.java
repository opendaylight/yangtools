/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class EmptyTypeTest {
    @Test
    void canCreateEmptyType() {
        EmptyTypeDefinition emptyType = BaseTypes.emptyType();

        assertEquals(TypeDefinitions.EMPTY, emptyType.getQName(), "QName");
        assertNull(emptyType.getBaseType(), "BaseType");
        assertEquals(Optional.empty(), emptyType.getDefaultValue(), "DefaultValue");
        assertEquals(Status.CURRENT, emptyType.getStatus(), "Status");
        assertEquals(Optional.empty(), emptyType.getReference());
        assertEquals(Optional.empty(), emptyType.getUnits(), "Units");
        assertEquals(Optional.empty(), emptyType.getDescription());
        assertEquals(List.of(), emptyType.getUnknownSchemaNodes(), "UnknownSchemaNodes");
        assertThat(emptyType.toString(), containsString("name=(urn:ietf:params:xml:ns:yang:1)empty"));
    }
}

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
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;

class BooleanTypeTest {
    @Test
    void canCreateBooleanType() {
        final var boolType = BaseTypes.booleanType();

        assertEquals(TypeDefinitions.BOOLEAN, boolType.getQName(), "getQName gives BOOLEAN_QNAME");
        assertEquals(Optional.empty(), boolType.getDescription());

        assertThat(boolType.toString(), containsString("name=(urn:ietf:params:xml:ns:yang:1)boolean"));
        assertEquals(Optional.empty(), boolType.getUnits());
        assertNull(boolType.getBaseType(), "Base type is null");
        assertEquals(Optional.empty(), boolType.getDefaultValue());
        assertEquals(Status.CURRENT, boolType.getStatus(), "Status CURRENT");
        assertEquals(List.of(), boolType.getUnknownSchemaNodes(), "Should contain empty list");
    }
}

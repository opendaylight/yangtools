/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.booleanType;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

public class BooleanTypeTest {
    @Test
    public void canCreateBooleanType() {
        final BooleanTypeDefinition boolType = booleanType();

        assertEquals("getPath gives List of BOOLEAN_QNAME",
                Collections.singletonList(BaseTypes.BOOLEAN_QNAME), boolType.getPath().getPathFromRoot());
        assertEquals("getQName gives BOOLEAN_QNAME", BaseTypes.BOOLEAN_QNAME, boolType.getQName());
        assertFalse(boolType.getDescription().isPresent());

        assertThat(boolType.toString(), containsString("name=(urn:ietf:params:xml:ns:yang:1)boolean"));
        assertEquals(Optional.empty(), boolType.getUnits());
        assertEquals("Base type is null", null, boolType.getBaseType());
        assertEquals(Optional.empty(), boolType.getDefaultValue());
        assertEquals("Status CURRENT", Status.CURRENT, boolType.getStatus());
        assertEquals("Should contain empty list", Collections.emptyList(), boolType.getUnknownSchemaNodes());
    }
}

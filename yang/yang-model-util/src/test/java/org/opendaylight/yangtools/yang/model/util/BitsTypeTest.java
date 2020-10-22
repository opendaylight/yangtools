/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

@RunWith(MockitoJUnitRunner.class)
public class BitsTypeTest {
    @Mock
    public BitsTypeDefinition.Bit bit;

    @Test
    public void canCreateBitsType() {
        doReturn("test").when(bit).getName();
        doReturn(Uint32.ZERO).when(bit).getPosition();
        doReturn("toString").when(bit).toString();

        QName qname = QName.create("namespace", "localname");
        SchemaPath schemaPath = SchemaPath.create(true, qname);

        BitsTypeDefinition bitsType = BaseTypes.bitsTypeBuilder(schemaPath).addBit(bit).build();

        assertFalse(bitsType.getDescription().isPresent());
        assertEquals("QName", qname, bitsType.getQName());
        assertEquals(Optional.empty(), bitsType.getUnits());
        assertNotEquals("Description should not be null", null, bitsType.toString());
        assertFalse(bitsType.getReference().isPresent());
        assertNull("BaseType should be null", bitsType.getBaseType());
        assertEquals(Optional.empty(), bitsType.getDefaultValue());
        assertEquals("getPath should equal schemaPath", schemaPath, bitsType.getPath());
        assertEquals("Status should be CURRENT", Status.CURRENT, bitsType.getStatus());
        assertEquals("Should be empty list", Collections.emptyList(), bitsType.getUnknownSchemaNodes());
        assertEquals("Values should be [enumPair]", Collections.singletonList(bit), bitsType.getBits());

        assertEquals("Hash code of bitsType should be equal",
                bitsType.hashCode(), bitsType.hashCode());
        assertNotEquals("bitsType shouldn't equal to null", null, bitsType);
        assertEquals("bitsType should equals to itself", bitsType, bitsType);
        assertNotEquals("bitsType shouldn't equal to object of other type", "str", bitsType);
    }
}

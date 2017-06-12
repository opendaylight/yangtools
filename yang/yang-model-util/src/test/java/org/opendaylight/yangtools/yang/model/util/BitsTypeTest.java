/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public class BitsTypeTest {

    @Mock
    private BitsTypeDefinition.Bit bit;

    @Test
    public void canCreateBitsType() {
        MockitoAnnotations.initMocks(this);
        doReturn("test").when(bit).getName();

        QName qname = QName.create("TestQName");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qname), true);

        BitsTypeDefinition bitsType = BaseTypes.bitsTypeBuilder(schemaPath).addBit(bit).build();

        assertNull("Description is not null", bitsType.getDescription());
        assertEquals("QName", qname, bitsType.getQName());
        assertNull("Should be null", bitsType.getUnits());
        assertNotEquals("Description should not be null", null, bitsType.toString());
        assertNull("Reference is not null", bitsType.getReference());
        assertNull("BaseType should be null", bitsType.getBaseType());
        assertNull("Default value should be null", bitsType.getDefaultValue());
        assertEquals("getPath should equal schemaPath", schemaPath, bitsType.getPath());
        assertEquals("Status should be CURRENT", Status.CURRENT, bitsType.getStatus());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, bitsType.getUnknownSchemaNodes());
        assertEquals("Values should be [enumPair]", Collections.singletonList(bit), bitsType.getBits());

        assertEquals("Hash code of bitsType should be equal",
                bitsType.hashCode(), bitsType.hashCode());
        assertNotEquals("bitsType shouldn't equal to null", null, bitsType);
        assertEquals("bitsType should equals to itself", bitsType, bitsType);
        assertNotEquals("bitsType shouldn't equal to object of other type", "str", bitsType);

    }

}

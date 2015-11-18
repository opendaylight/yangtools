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

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

public class BitsTypeTest {

    @Mock
    private BitsTypeDefinition.Bit bit;

    @Test
    public void canCreateBitsType(){
        MockitoAnnotations.initMocks(this);

        QName qName = QName.create("TestQName");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qName), true);
        List<BitsTypeDefinition.Bit> listBit = Collections.singletonList(bit);
        BitsType bitsType = BitsType.create(schemaPath, listBit);

        assertNotEquals("Description is not null", null, bitsType.getDescription());
        assertEquals("QName", BaseTypes.BITS_QNAME, bitsType.getQName());
        assertEquals("Should be empty string", "", bitsType.getUnits());
        assertNotEquals("Description should not be null", null, bitsType.toString());
        assertNotEquals("Reference is not null", null, bitsType.getReference());
        assertEquals("BaseType should be null", null, bitsType.getBaseType());
        assertEquals("Default value should be null", null, bitsType.getDefaultValue());
        assertEquals("getPath should equal schemaPath", schemaPath, bitsType.getPath());
        assertEquals("Status should be CURRENT", Status.CURRENT, bitsType.getStatus());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, bitsType.getUnknownSchemaNodes());
        assertEquals("Values should be [enumPair]", listBit, bitsType.getBits());

        assertEquals("Hash code of bitsType should be equal",
                bitsType.hashCode(), bitsType.hashCode());
        assertNotEquals("bitsType shouldn't equal to null", null, bitsType);
        assertEquals("bitsType should equals to itself", bitsType, bitsType);
        assertNotEquals("bitsType shouldn't equal to object of other type", "str", bitsType);

    }

}
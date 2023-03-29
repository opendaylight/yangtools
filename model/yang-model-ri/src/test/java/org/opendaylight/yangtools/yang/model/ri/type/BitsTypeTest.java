/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

@ExtendWith(MockitoExtension.class)
public class BitsTypeTest {
    @Mock
    public BitsTypeDefinition.Bit bit;

    @Test
    void canCreateBitsType() {
        doReturn("test").when(bit).getName();
        doReturn(Uint32.ZERO).when(bit).getPosition();
        doReturn("toString").when(bit).toString();

        QName qname = QName.create("namespace", "localname");

        BitsTypeDefinition bitsType = BaseTypes.bitsTypeBuilder(qname).addBit(bit).build();

        assertFalse(bitsType.getDescription().isPresent());
        assertEquals(qname, bitsType.getQName(), "QName");
        assertEquals(Optional.empty(), bitsType.getUnits());
        assertNotEquals(null, bitsType.toString(), "Description should not be null");
        assertFalse(bitsType.getReference().isPresent());
        assertNull(bitsType.getBaseType(), "BaseType should be null");
        assertEquals(Optional.empty(), bitsType.getDefaultValue());
        assertEquals(Status.CURRENT, bitsType.getStatus(), "Status should be CURRENT");
        assertEquals(Collections.emptyList(), bitsType.getUnknownSchemaNodes(), "Should be empty list");
        assertEquals(Collections.singletonList(bit), bitsType.getBits(), "Values should be [enumPair]");

        assertEquals(bitsType.hashCode(), bitsType.hashCode(), "Hash code of bitsType should be equal");
        assertNotEquals(null, bitsType, "bitsType shouldn't equal to null");
        assertEquals(bitsType, bitsType, "bitsType should equals to itself");
        assertNotEquals("str", bitsType, "bitsType shouldn't equal to object of other type");
    }
}

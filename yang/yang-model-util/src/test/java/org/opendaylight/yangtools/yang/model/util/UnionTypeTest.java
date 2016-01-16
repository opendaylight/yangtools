/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.int32Type;
import static org.opendaylight.yangtools.yang.model.util.type.BaseTypes.unionTypeBuilder;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.UnionTypeBuilder;

public class UnionTypeTest {

    @Test
    public void canCreateUnion() {
        IntegerTypeDefinition int32 = int32Type();
        UnionTypeBuilder builder = unionTypeBuilder(SchemaPath.ROOT);
        builder.addType(int32);
        UnionTypeDefinition unionType = builder.build();

        assertEquals("GetUnits should be null", null, unionType.getUnits());
        assertTrue("String should contain int32", unionType.toString().contains("int32"));
        assertEquals("Should be empty list", Collections.EMPTY_LIST, unionType.getUnknownSchemaNodes());
        assertNotEquals("Description should not be null", null, unionType.getDescription());
        assertNotEquals("Ref should not be null", null, unionType.getReference());
        assertEquals("Should be CURRENT", Status.CURRENT, unionType.getStatus());
        assertEquals("Should be int32 in list", Collections.singletonList(int32), unionType.getTypes());
        assertEquals("Base type should be null", null, unionType.getBaseType());
        assertEquals("Default value should be null", null, unionType.getDefaultValue());
        assertEquals("Should be same as list of BaseTypes",
                Collections.singletonList(BaseTypes.UNION_QNAME), unionType.getPath().getPathFromRoot());
        assertEquals("Should be BaseTypes", BaseTypes.UNION_QNAME, unionType.getQName());

        assertEquals("unionType should equals to itself", unionType, unionType);
        assertFalse("unionType shouldn't equal to null", unionType.equals(null));
        assertTrue("Hash code of unionType should be equal to itself",
                unionType.hashCode() == unionType.hashCode());
    }

}
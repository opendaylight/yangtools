/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class EnumerationTypeTest {

    @Mock private EnumTypeDefinition.EnumPair enumPair;

    @Test
    public void canCreateEnumerationType(){
        MockitoAnnotations.initMocks(this);

        QName qName = QName.create("TestQName");
        SchemaPath schemaPath = SchemaPath.create(Collections.singletonList(qName), true);

        List<EnumTypeDefinition.EnumPair> listEnumPair = Collections.singletonList(enumPair);
        Optional<EnumTypeDefinition.EnumPair> defaultValue = Optional.of(enumPair);

        EnumerationType enumerationType = EnumerationType.create(schemaPath, listEnumPair, defaultValue);

        assertNotEquals("Description is not null", null, enumerationType.getDescription());
        assertEquals("QName", BaseTypes.ENUMERATION_QNAME, enumerationType.getQName());
        assertEquals("Should be empty string", "", enumerationType.getUnits());
        assertNotEquals("Description should not be null", null, enumerationType.toString());
        assertNotEquals("Reference is not null", null, enumerationType.getReference());
        assertEquals("BaseType should be null", null, enumerationType.getBaseType());
        assertEquals("Default value should be enumPair", enumPair, enumerationType.getDefaultValue());
        assertEquals("getPath should equal schemaPath", schemaPath, enumerationType.getPath());
        assertEquals("Status should be CURRENT", Status.CURRENT, enumerationType.getStatus());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, enumerationType.getUnknownSchemaNodes());
        assertEquals("Values should be [enumPair]", listEnumPair, enumerationType.getValues());

        assertTrue("Hash code of enumerationType should be equal",
                enumerationType.hashCode() == enumerationType.hashCode());
        assertFalse("EnumerationType shouldn't equal to null", enumerationType.equals(null));
        assertEquals("EnumerationType should equals to itself", enumerationType, enumerationType);
        assertFalse("EnumerationType shouldn't equal to object of other type", enumerationType.equals("str"));
    }
}
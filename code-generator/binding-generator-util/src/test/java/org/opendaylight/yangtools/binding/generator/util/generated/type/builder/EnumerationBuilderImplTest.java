/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;

public class EnumerationBuilderImplTest {

    private EnumerationBuilderImpl enumerationBuilder;
    private final String DESCRIPTION = "Test description of Enum";
    private final String packageName = "org.opendaylight.test";
    private final String name = "TestName";
    private final String moduleName = "TestModuleName";
    private final String reference = "TestRef";
    private Enumeration enumeration;
    private QName  qName = QName.create("TestQName", "10-10-2014", "TestLocalQName");

    @Before
    public void setup() {
        enumerationBuilder = new EnumerationBuilderImpl(packageName, name);
        enumerationBuilder.setDescription(DESCRIPTION);
        enumerationBuilder.setModuleName(moduleName);
        enumerationBuilder.setReference(reference);
        enumerationBuilder.setSchemaPath(Collections.singletonList(qName));
        enumerationBuilder.addValue("TestValue", 12, "Value used for test");
        enumerationBuilder.addAnnotation(packageName, "TestAnnotation");
        enumeration = enumerationBuilder.toInstance(enumerationBuilder);
    }

    @Test
    public void testGetters() {
        assertEquals(packageName + "." + name, enumerationBuilder.getFullyQualifiedName());
        assertEquals(name , enumerationBuilder.getName());
        assertEquals(packageName, enumerationBuilder.getPackageName());
    }

    @Test
    public void tesEnumerationGetters() {
        assertEquals(name, enumeration.getName());
        assertEquals(packageName, enumeration.getPackageName());
        assertEquals(null, enumeration.getComment());
        assertEquals(enumerationBuilder, enumeration.getParentType());
        assertEquals(DESCRIPTION, enumeration.getDescription());
        assertEquals(moduleName, enumeration.getModuleName());
        assertEquals(packageName + '.' + name, enumeration.getFullyQualifiedName());
        assertEquals(reference, enumeration.getReference());
        assertEquals(Collections.singletonList(qName), enumeration.getSchemaPath());
        assertEquals(Collections.EMPTY_LIST, enumeration.getEnclosedTypes());
        assertEquals(Collections.EMPTY_LIST, enumeration.getEnumerations());
        assertEquals(Collections.EMPTY_LIST, enumeration.getMethodDefinitions());
        assertEquals(Collections.EMPTY_LIST, enumeration.getConstantDefinitions());
        assertEquals(Collections.EMPTY_LIST, enumeration.getProperties());
        assertEquals(Collections.EMPTY_LIST, enumeration.getImplements());

        assertFalse(enumeration.isAbstract());

    }

    @Test
    public void testEnumerationToString() {
        String formattedString =
                "public enum " + name + " {\n" +
                "\t TestValue " + "(12 );\n" +
                "}";
        String s = "Enumeration [packageName="+packageName+", definingType="+packageName+"."+name+", name="+name+
                ", values=[EnumPair [name=TestValue, value=12]]]";

        assertEquals("", s, enumeration.toString());
        assertEquals("", formattedString, enumeration.toFormattedString());

    }

    @Test
    public void testEqualsAndHash() {
        Enumeration enumeration1 = enumerationBuilder.toInstance(enumerationBuilder);

        assertEquals(enumeration1, enumeration);
        assertEquals(enumeration, enumeration);
        assertNotEquals(null, enumeration);
        assertEquals(enumeration1.hashCode(), enumeration.hashCode());
        assertEquals(enumerationBuilder.hashCode(), enumerationBuilder.hashCode());
        assertEquals(enumerationBuilder, enumerationBuilder);
        assertNotEquals(null, enumerationBuilder);
    }
}
/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.EnumPairBuilder;

public class EnumerationBuilderImplTest {

    private final QName qname = QName.create("TestQName", "2014-10-10", "TestLocalQName");
    private static final String DESCRIPTION = "Test description of Enum";
    private final String packageName = "org.opendaylight.test";
    private final String name = "TestName";
    private final String moduleName = "TestModuleName";
    private final String reference = "TestRef";
    private final String valueName = "TestValue";
    private final String valueDescription = "Value used for test";
    private final int value = 12;
    private Enumeration enumeration;
    private CodegenEnumerationBuilder enumerationBuilder;
    private CodegenEnumerationBuilder enumerationBuilderSame;
    private CodegenEnumerationBuilder enumerationBuilderOtherName;
    private CodegenEnumerationBuilder enumerationBuilderOtherPackage;

    @Before
    public void setup() {
        enumerationBuilder = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, name));
        enumerationBuilder.setDescription(DESCRIPTION);
        enumerationBuilder.setModuleName(moduleName);
        enumerationBuilder.setReference(reference);
        enumerationBuilder.setSchemaPath(SchemaPath.create(true, qname));
        enumerationBuilder.addValue(valueName, valueName, value, Status.CURRENT, valueDescription, null);
        enumerationBuilder.addAnnotation(packageName, "TestAnnotation");
        enumerationBuilderSame = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, name));
        enumerationBuilderOtherName = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, "SomeOtherName"));
        enumerationBuilderOtherPackage = new CodegenEnumerationBuilder(JavaTypeName.create("org.opendaylight.other",
            name));
        enumeration = enumerationBuilder.toInstance();
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAnnotation() {
        assertNull(enumerationBuilder.addAnnotation(null));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAnnotationPackage() {
        assertNull(enumerationBuilder.addAnnotation(null, "test"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAnnotationName() {
        assertNull(enumerationBuilder.addAnnotation(packageName, null));
    }

    @Test
    public void testEnumerationBuilder() {
        assertEquals(packageName + "." + name, enumerationBuilder.getFullyQualifiedName());
        assertEquals(name , enumerationBuilder.getName());
        assertEquals(packageName, enumerationBuilder.getPackageName());

        assertNotEquals(enumerationBuilder, null);
        assertEquals(enumerationBuilder, enumerationBuilder);
        assertNotEquals(enumerationBuilder, "string");
        assertNotEquals(enumerationBuilder, enumerationBuilderOtherName);
        assertNotEquals(enumerationBuilder, enumerationBuilderOtherPackage);
        assertEquals(enumerationBuilder,enumerationBuilderSame);
    }

    @Test
    public void testEnumeration() {
        assertEquals(name, enumeration.getName());
        assertEquals(packageName, enumeration.getPackageName());
        assertEquals(null, enumeration.getComment());
        assertEquals(DESCRIPTION, enumeration.getDescription());
        assertEquals(moduleName, enumeration.getModuleName());
        assertEquals(packageName + '.' + name, enumeration.getFullyQualifiedName());
        assertEquals(reference, enumeration.getReference());
        assertEquals(Collections.singletonList(qname), enumeration.getSchemaPath());
        assertEquals(Collections.emptyList(), enumeration.getEnclosedTypes());
        assertEquals(Collections.emptyList(), enumeration.getEnumerations());
        assertEquals(Collections.emptyList(), enumeration.getMethodDefinitions());
        assertEquals(Collections.emptyList(), enumeration.getConstantDefinitions());
        assertEquals(Collections.emptyList(), enumeration.getProperties());
        assertEquals(Collections.emptyList(), enumeration.getImplements());
        assertNotNull(enumeration.getValues());
        assertNotNull(enumeration.getAnnotations());

        assertFalse(enumeration.isAbstract());
        assertNotEquals(enumeration, null);
        assertEquals(enumeration, enumeration);
        assertNotEquals(enumeration, "string");

        final Enumeration enumerationOtherPackage = enumerationBuilderOtherPackage.toInstance();
        assertNotEquals(enumeration, enumerationOtherPackage);

        final Enumeration enumerationOtherName = enumerationBuilderOtherName.toInstance();
        assertNotEquals(enumeration, enumerationOtherName);

        enumerationBuilderSame.addValue(valueName, valueName, value, Status.CURRENT, valueDescription, null);
        final Enumeration enumerationSame = enumerationBuilderSame.toInstance();
        assertEquals(enumeration, enumerationSame);

        final CodegenEnumerationBuilder enumerationBuilderSame1 = new CodegenEnumerationBuilder(
            JavaTypeName.create(packageName, name));
        final Enumeration enumerationSame1 = enumerationBuilderSame1.toInstance();
        enumerationBuilderSame1.addValue(valueName, valueName, 14, Status.CURRENT, valueDescription, null);
        // Enums are equal thanks to same package name and local name
        assertEquals(enumeration, enumerationSame1);
    }

    @Test
    public void testEnumerationToString() {
        assertEquals("EnumerationImpl{identifier=org.opendaylight.test.TestName, "
            + "values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]}", enumeration.toString());
        assertEquals("public enum " + name + " {\n"
            + "\t TestValue " + "(12 );\n"
            + "}", enumeration.toFormattedString());

        assertEquals("CodegenEnumerationBuilder{identifier=org.opendaylight.test.TestName, "
            + "values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]}", enumerationBuilder.toString());
    }

    @Test
    public void testUpdateEnumPairsFromEnumTypeDef() {
        final EnumTypeDefinition enumTypeDefinition = BaseTypes.enumerationTypeBuilder(QName.create("test", "test"))
                .addEnum(EnumPairBuilder.create("SomeName", 42).setDescription("Some Other Description")
                    .setReference("Some other reference").build()).build();
        enumerationBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDefinition);
    }
}

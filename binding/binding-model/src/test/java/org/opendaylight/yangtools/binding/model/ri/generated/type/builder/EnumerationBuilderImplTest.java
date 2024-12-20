/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.EnumPairBuilder;

class EnumerationBuilderImplTest {
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

    @BeforeEach
    void setup() {
        enumerationBuilder = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, name));
        enumerationBuilder.setDescription(DESCRIPTION);
        enumerationBuilder.setModuleName(moduleName);
        enumerationBuilder.setReference(reference);
        enumerationBuilder.addValue(valueName, valueName, value, Status.CURRENT, valueDescription, null);
        enumerationBuilder.addAnnotation(packageName, "TestAnnotation");
        enumerationBuilderSame = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, name));
        enumerationBuilderOtherName = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, "SomeOtherName"));
        enumerationBuilderOtherPackage = new CodegenEnumerationBuilder(JavaTypeName.create("org.opendaylight.other",
            name));
        enumeration = enumerationBuilder.toInstance();
    }

    @Test
    void testAddNullAnnotation() {
        assertThrows(NullPointerException.class, () -> enumerationBuilder.addAnnotation(null));
    }

    @Test
    void testAddNullAnnotationPackage() {
        assertThrows(NullPointerException.class, () -> enumerationBuilder.addAnnotation(null, "test"));
    }

    @Test
    void testAddNullAnnotationName() {
        assertThrows(NullPointerException.class, () -> enumerationBuilder.addAnnotation(packageName, null));
    }

    @Test
    void testEnumerationBuilder() {
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
    void testEnumeration() {
        assertEquals(name, enumeration.getName());
        assertEquals(packageName, enumeration.getPackageName());
        assertEquals(null, enumeration.getComment());
        assertEquals(DESCRIPTION, enumeration.getDescription());
        assertEquals(moduleName, enumeration.getModuleName());
        assertEquals(packageName + '.' + name, enumeration.getFullyQualifiedName());
        assertEquals(reference, enumeration.getReference());
        assertEquals(List.of(), enumeration.getEnclosedTypes());
        assertEquals(List.of(), enumeration.getEnumerations());
        assertEquals(List.of(), enumeration.getMethodDefinitions());
        assertEquals(List.of(), enumeration.getConstantDefinitions());
        assertEquals(List.of(), enumeration.getProperties());
        assertEquals(List.of(), enumeration.getImplements());
        assertNotNull(enumeration.getValues());
        assertNotNull(enumeration.getAnnotations());

        assertFalse(enumeration.isAbstract());
        assertNotEquals(enumeration, null);
        assertEquals(enumeration, enumeration);
        assertNotEquals(enumeration, "string");

        final var enumerationOtherPackage = enumerationBuilderOtherPackage.toInstance();
        assertNotEquals(enumeration, enumerationOtherPackage);

        final var enumerationOtherName = enumerationBuilderOtherName.toInstance();
        assertNotEquals(enumeration, enumerationOtherName);

        enumerationBuilderSame.addValue(valueName, valueName, value, Status.CURRENT, valueDescription, null);
        final var enumerationSame = enumerationBuilderSame.toInstance();
        assertEquals(enumeration, enumerationSame);

        final var enumerationBuilderSame1 = new CodegenEnumerationBuilder(JavaTypeName.create(packageName, name));
        final var enumerationSame1 = enumerationBuilderSame1.toInstance();
        enumerationBuilderSame1.addValue(valueName, valueName, 14, Status.CURRENT, valueDescription, null);
        // Enums are equal thanks to same package name and local name
        assertEquals(enumeration, enumerationSame1);
    }

    @Test
    void testEnumerationToString() {
        assertEquals("EnumerationImpl{identifier=org.opendaylight.test.TestName, "
            + "values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]}", enumeration.toString());
        assertEquals("public enum " + name + " {\n"
            + "\t TestValue " + "(12 );\n"
            + "}", enumeration.toFormattedString());

        assertEquals("CodegenEnumerationBuilder{identifier=org.opendaylight.test.TestName, "
            + "values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]}", enumerationBuilder.toString());
    }

    @Test
    void testUpdateEnumPairsFromEnumTypeDef() {
        enumerationBuilder.updateEnumPairsFromEnumTypeDef(BaseTypes.enumerationTypeBuilder(QName.create("test", "test"))
            .addEnum(EnumPairBuilder.create("SomeName", 42)
                .setDescription("Some Other Description")
                .setReference("Some other reference")
                .build())
            .build());
    }
}

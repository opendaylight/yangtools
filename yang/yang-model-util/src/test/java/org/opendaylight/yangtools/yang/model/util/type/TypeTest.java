/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;

public class TypeTest {
    private static final QName Q_NAME = QName.create("test.namespace", "2016-01-01", "test-name");
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, Q_NAME);
    private static final RevisionAwareXPath REVISION_AWARE_XPATH = new RevisionAwareXPathImpl("/test", true);

    @Test
    public void binaryTypeTest() {
        final BaseBinaryType baseBinaryType1 = BaseBinaryType.INSTANCE;
        final BaseBinaryType baseBinaryType2 = (BaseBinaryType)BaseTypes.binaryType();
        hashCodeEqualsToStringTest(baseBinaryType1, baseBinaryType2);
        assertEquals(baseBinaryType1.getLengthConstraints(), baseBinaryType2.getLengthConstraints());

        final DerivedBinaryType derivedBinaryType1 = (DerivedBinaryType)DerivedTypes.derivedTypeBuilder(baseBinaryType1,
                SCHEMA_PATH).build();
        final DerivedBinaryType derivedBinaryType2 = (DerivedBinaryType)DerivedTypes.derivedTypeBuilder(baseBinaryType2,
                SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedBinaryType1, derivedBinaryType2);

        final RestrictedBinaryType restrictedBinaryType1 = (RestrictedBinaryType)RestrictedTypes.newBinaryBuilder
                (baseBinaryType1, SCHEMA_PATH).buildType();
        final RestrictedBinaryType restrictedBinaryType2 = (RestrictedBinaryType)RestrictedTypes.newBinaryBuilder
                (baseBinaryType2, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedBinaryType1, restrictedBinaryType2);

        final LengthRestrictedTypeBuilder lengthRestrictedTypeBuilder = RestrictedTypes
                .newBinaryBuilder(baseBinaryType1, SCHEMA_PATH);
        final BaseBinaryType baseBinaryType = (BaseBinaryType)lengthRestrictedTypeBuilder.build();
        assertEquals(lengthRestrictedTypeBuilder.getLengthConstraints(baseBinaryType1),
                baseBinaryType.getLengthConstraints());
        assertEquals(baseBinaryType, baseBinaryType1);
    }

    @Test
    public void booleanTypeTest() {
        final BaseBooleanType baseBooleanType1 = BaseBooleanType.INSTANCE;
        final BaseBooleanType baseBooleanType2 = (BaseBooleanType)BaseTypes.booleanType();
        hashCodeEqualsToStringTest(baseBooleanType1, baseBooleanType2);

        final DerivedBooleanType derivedBooleanType1 = (DerivedBooleanType)DerivedTypes.derivedTypeBuilder
                (baseBooleanType1, SCHEMA_PATH).build();
        final DerivedBooleanType derivedBooleanType2 = (DerivedBooleanType)DerivedTypes.derivedTypeBuilder
                (baseBooleanType1, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedBooleanType1, derivedBooleanType2);

        restrictedBuilderTest(RestrictedTypes.newBooleanBuilder(baseBooleanType1, SCHEMA_PATH), RestrictedTypes
                .newBooleanBuilder(baseBooleanType2, SCHEMA_PATH));
    }

    @Test
    public void decimalTypeTest() {
        final BaseDecimalType baseDecimalType1 = (BaseDecimalType)BaseTypes.decimalTypeBuilder(SCHEMA_PATH)
                .setFractionDigits(1)
                .buildType();
        final BaseDecimalType baseDecimalType2 = (BaseDecimalType)BaseTypes.decimalTypeBuilder(SCHEMA_PATH)
                .setFractionDigits(1)
                .buildType();
        hashCodeEqualsToStringTest(baseDecimalType1, baseDecimalType2);
        assertEquals(baseDecimalType1.getFractionDigits(), baseDecimalType2.getFractionDigits());

        final DerivedDecimalType derivedDecimalType1 = (DerivedDecimalType)DerivedTypes
                .derivedTypeBuilder(baseDecimalType1, SCHEMA_PATH).build();
        final DerivedDecimalType derivedDecimalType2 = (DerivedDecimalType)DerivedTypes
                .derivedTypeBuilder(baseDecimalType1, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedDecimalType1, derivedDecimalType2);

        final RestrictedDecimalType restrictedDecimalType1 = (RestrictedDecimalType)
                RestrictedTypes.newDecima64Builder(baseDecimalType1, SCHEMA_PATH).buildType();
        final RestrictedDecimalType restrictedDecimalType2 = (RestrictedDecimalType)
                RestrictedTypes.newDecima64Builder(baseDecimalType2, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedDecimalType1, restrictedDecimalType2);
    }

    @Test
    public void emptyTypeTest() {
        final BaseEmptyType baseEmptyType1 = BaseEmptyType.INSTANCE;
        final BaseEmptyType baseEmptyType2 = (BaseEmptyType)BaseTypes.emptyType();
        hashCodeEqualsToStringTest(baseEmptyType1, baseEmptyType2);

        final DerivedEmptyType derivedEmptyType1 = (DerivedEmptyType)DerivedTypes.derivedTypeBuilder
                (baseEmptyType1, SCHEMA_PATH).build();
        final DerivedEmptyType derivedEmptyType2 = (DerivedEmptyType)DerivedTypes.derivedTypeBuilder
                (baseEmptyType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedEmptyType1, derivedEmptyType2);

        restrictedBuilderTest(RestrictedTypes.newEmptyBuilder(baseEmptyType1, SCHEMA_PATH),
                RestrictedTypes.newEmptyBuilder(baseEmptyType2, SCHEMA_PATH));
    }

    @Test
    public void instanceIdentifierTypeTest() {
        final BaseInstanceIdentifierType baseInstanceIdentifierType1 = BaseInstanceIdentifierType.INSTANCE;
        final BaseInstanceIdentifierType baseInstanceIdentifierType2 = (BaseInstanceIdentifierType)BaseTypes
                .instanceIdentifierType();
        hashCodeEqualsToStringTest(baseInstanceIdentifierType1, baseInstanceIdentifierType2);
        assertFalse(baseInstanceIdentifierType1.requireInstance());

        final DerivedInstanceIdentifierType derivedInstanceIdentifierType1 = (DerivedInstanceIdentifierType)
                DerivedTypes.derivedTypeBuilder(baseInstanceIdentifierType1, SCHEMA_PATH).build();
        final DerivedInstanceIdentifierType derivedInstanceIdentifierType2 = (DerivedInstanceIdentifierType)
                DerivedTypes.derivedTypeBuilder(baseInstanceIdentifierType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedInstanceIdentifierType1, derivedInstanceIdentifierType2);

        final InstanceIdentifierTypeDefinition restrictedInstanceIdentifierType1 = RestrictedTypes
                .newInstanceIdentifierBuilder(baseInstanceIdentifierType1, SCHEMA_PATH).buildType();
        final InstanceIdentifierTypeDefinition restrictedInstanceIdentifierType2 = RestrictedTypes
                .newInstanceIdentifierBuilder(baseInstanceIdentifierType2, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedInstanceIdentifierType1, restrictedInstanceIdentifierType2);
    }

    @Test
    public void integerTypeTest() {
        final IntegerTypeDefinition integerTypeDefinition8 = BaseTypes.int8Type();
        final IntegerTypeDefinition integerTypeDefinition16 = BaseTypes.int16Type();
        final IntegerTypeDefinition integerTypeDefinition32 = BaseTypes.int32Type();
        final IntegerTypeDefinition integerTypeDefinition64 = BaseTypes.int64Type();
        assertTrue(BaseTypes.isInt8(integerTypeDefinition8));
        assertTrue(BaseTypes.isInt16(integerTypeDefinition16));
        assertTrue(BaseTypes.isInt32(integerTypeDefinition32));
        assertTrue(BaseTypes.isInt64(integerTypeDefinition64));
        testInstance(BaseInt8Type.INSTANCE, integerTypeDefinition8);
        testInstance(BaseInt16Type.INSTANCE, integerTypeDefinition16);
        testInstance(BaseInt32Type.INSTANCE, integerTypeDefinition32);
        testInstance(BaseInt64Type.INSTANCE, integerTypeDefinition64);

        final RestrictedIntegerType restrictedIntegerType1 = (RestrictedIntegerType)RestrictedTypes.newIntegerBuilder
                (integerTypeDefinition8, SCHEMA_PATH).buildType();
        final RestrictedIntegerType restrictedIntegerType2 = (RestrictedIntegerType)RestrictedTypes.newIntegerBuilder
                (BaseInt8Type.INSTANCE, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedIntegerType1, restrictedIntegerType2);

        final UnsignedIntegerTypeDefinition integerTypeDefinitionu8 = BaseTypes.uint8Type();
        final UnsignedIntegerTypeDefinition integerTypeDefinitionu16 = BaseTypes.uint16Type();
        final UnsignedIntegerTypeDefinition integerTypeDefinitionu32 = BaseTypes.uint32Type();
        final UnsignedIntegerTypeDefinition integerTypeDefinitionu64 = BaseTypes.uint64Type();
        assertTrue(BaseTypes.isUint8(integerTypeDefinitionu8));
        assertTrue(BaseTypes.isUint16(integerTypeDefinitionu16));
        assertTrue(BaseTypes.isUint32(integerTypeDefinitionu32));
        assertTrue(BaseTypes.isUint64(integerTypeDefinitionu64));
        testInstance(BaseUint8Type.INSTANCE, integerTypeDefinitionu8);
        testInstance(BaseUint16Type.INSTANCE, integerTypeDefinitionu16);
        testInstance(BaseUint32Type.INSTANCE, integerTypeDefinitionu32);
        testInstance(BaseUint64Type.INSTANCE, BaseTypes.baseTypeOf(integerTypeDefinitionu64));

        final DerivedIntegerType derivedIntegerType1 = (DerivedIntegerType)DerivedTypes
                .derivedTypeBuilder(integerTypeDefinition8, SCHEMA_PATH).build();
        final DerivedIntegerType derivedIntegerType2 = (DerivedIntegerType)DerivedTypes
                .derivedTypeBuilder(BaseInt8Type.INSTANCE, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedIntegerType1, derivedIntegerType2);

        final RestrictedUnsignedType restrictedUnsignedType1 = (RestrictedUnsignedType)RestrictedTypes
                .newUnsignedBuilder(integerTypeDefinitionu8, SCHEMA_PATH).buildType();
        final RestrictedUnsignedType restrictedUnsignedType2 = (RestrictedUnsignedType)RestrictedTypes
                .newUnsignedBuilder(BaseUint8Type.INSTANCE, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedUnsignedType1, restrictedUnsignedType2);
    }

    @Test
    public void stringTypeTest() {
        final BaseStringType baseStringType1 = BaseStringType.INSTANCE;
        final BaseStringType baseStringType2 = (BaseStringType)BaseTypes.stringType();
        hashCodeEqualsToStringTest(baseStringType1, baseStringType2);
        assertEquals(baseStringType1.getLengthConstraints(), baseStringType2.getLengthConstraints());
        assertEquals(baseStringType1.getPatternConstraints(), baseStringType2.getPatternConstraints());

        final DerivedStringType derivedStringType1 = (DerivedStringType)
                DerivedTypes.derivedTypeBuilder(baseStringType1, SCHEMA_PATH).build();
        final DerivedStringType derivedStringType2 = (DerivedStringType)
                DerivedTypes.derivedTypeBuilder(baseStringType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedStringType1, derivedStringType2);

        final RestrictedStringType restrictedStringType1 = (RestrictedStringType)RestrictedTypes
                .newStringBuilder(baseStringType1, SCHEMA_PATH).buildType();
        final RestrictedStringType restrictedStringType2 = (RestrictedStringType)RestrictedTypes
                .newStringBuilder(baseStringType2, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedStringType1, restrictedStringType2);
    }

    @Test
    public void bitsTypeTest() {
        final BitsTypeBuilder bitsTypeBuilder = BaseTypes.bitsTypeBuilder(SCHEMA_PATH);
        final BitsTypeDefinition bitsTypeDefinition1 = bitsTypeBuilder.build();
        final BitsTypeDefinition bitsTypeDefinition2 = bitsTypeBuilder.build();
        hashCodeEqualsToStringTest(bitsTypeDefinition1, bitsTypeDefinition2);
        assertEquals(bitsTypeDefinition1.getBits(), bitsTypeDefinition1.getBits());

        final DerivedBitsType derivedBitsType1 = (DerivedBitsType)DerivedTypes
                .derivedTypeBuilder(bitsTypeDefinition1, SCHEMA_PATH).build();
        final DerivedBitsType derivedBitsType2 = (DerivedBitsType)DerivedTypes
                .derivedTypeBuilder(bitsTypeDefinition2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedBitsType1, derivedBitsType2);

        restrictedBuilderTest(RestrictedTypes.newBitsBuilder(bitsTypeDefinition1, SCHEMA_PATH),
                RestrictedTypes.newBitsBuilder(bitsTypeDefinition2, SCHEMA_PATH));
    }

    @Test
    public void enumerationTypeTest() {
        final BaseEnumerationType baseEnumerationType1 = (BaseEnumerationType)BaseTypes.enumerationTypeBuilder
                (SCHEMA_PATH).build();
        final BaseEnumerationType baseEnumerationType2 = (BaseEnumerationType)BaseTypes.enumerationTypeBuilder
                (SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(baseEnumerationType1, baseEnumerationType2);
        assertEquals(baseEnumerationType1.getValues(), baseEnumerationType2.getValues());

        final DerivedEnumerationType derivedEnumerationType1 = (DerivedEnumerationType)DerivedTypes
                .derivedTypeBuilder(baseEnumerationType1, SCHEMA_PATH).build();
        final DerivedEnumerationType derivedEnumerationType2 = (DerivedEnumerationType)DerivedTypes
                .derivedTypeBuilder(baseEnumerationType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedEnumerationType1, derivedEnumerationType2);

        restrictedBuilderTest(RestrictedTypes.newEnumerationBuilder(baseEnumerationType1, SCHEMA_PATH),
                RestrictedTypes.newEnumerationBuilder(baseEnumerationType2, SCHEMA_PATH));
    }

    @Test
    public void leafrefTypeTest() {
        final LeafrefTypeBuilder leafrefTypeBuilder1 = BaseTypes.leafrefTypeBuilder(SCHEMA_PATH);
        final LeafrefTypeBuilder leafrefTypeBuilder2 = BaseTypes.leafrefTypeBuilder(SCHEMA_PATH);
        leafrefTypeBuilder1.setPathStatement(REVISION_AWARE_XPATH);
        leafrefTypeBuilder2.setPathStatement(REVISION_AWARE_XPATH);
        final BaseLeafrefType baseLeafrefType1 = (BaseLeafrefType)leafrefTypeBuilder1.build();
        final BaseLeafrefType baseLeafrefType2 = (BaseLeafrefType)leafrefTypeBuilder1.build();
        hashCodeEqualsToStringTest(baseLeafrefType1, baseLeafrefType2);
        assertEquals(baseLeafrefType1.getPathStatement(), REVISION_AWARE_XPATH);

        final DerivedLeafrefType derivedLeafrefType1 = (DerivedLeafrefType)DerivedTypes
                .derivedTypeBuilder(baseLeafrefType1, SCHEMA_PATH).build();
        final DerivedLeafrefType derivedLeafrefType2 = (DerivedLeafrefType)DerivedTypes
                .derivedTypeBuilder(baseLeafrefType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedLeafrefType1, derivedLeafrefType2);

        restrictedBuilderTest(RestrictedTypes.newLeafrefBuilder(baseLeafrefType1, SCHEMA_PATH),
                RestrictedTypes.newLeafrefBuilder(baseLeafrefType2, SCHEMA_PATH));
    }

    @Test
    public void unionTypeTest() throws IllegalAccessException, InstantiationException {
        final BaseDecimalType baseDecimalType1 = (BaseDecimalType)BaseTypes.decimalTypeBuilder(SCHEMA_PATH)
                .setFractionDigits(1)
                .buildType();
        final BaseDecimalType baseDecimalType2 = (BaseDecimalType)BaseTypes.decimalTypeBuilder(SCHEMA_PATH)
                .setFractionDigits(1)
                .buildType();
        final UnionTypeBuilder unionTypeBuilder1 = BaseTypes.unionTypeBuilder(SCHEMA_PATH);
        final UnionTypeBuilder unionTypeBuilder2 = BaseTypes.unionTypeBuilder(SCHEMA_PATH);
        unionTypeBuilder1.addType(baseDecimalType1);
        unionTypeBuilder2.addType(baseDecimalType2);
        final BaseUnionType baseUnionType1 = (BaseUnionType)unionTypeBuilder1.build();
        final BaseUnionType baseUnionType2 = (BaseUnionType)unionTypeBuilder2.build();
        hashCodeEqualsToStringTest(baseUnionType1, baseUnionType2);
        assertEquals(baseUnionType1.getTypes(), baseUnionType2.getTypes());

        final DerivedUnionType derivedUnionType1 = (DerivedUnionType)DerivedTypes
                .derivedTypeBuilder(baseUnionType1, SCHEMA_PATH).build();
        final DerivedUnionType derivedUnionType2 = (DerivedUnionType)DerivedTypes
                .derivedTypeBuilder(baseUnionType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedUnionType1, derivedUnionType2);

        restrictedBuilderTest(RestrictedTypes.newUnionBuilder(baseUnionType1, SCHEMA_PATH),
                RestrictedTypes.newUnionBuilder(baseUnionType2, SCHEMA_PATH));
    }

    @Test(expected = NullPointerException.class)
    public void identityrefTypeBuilderException() {
        BaseTypes.identityrefTypeBuilder(SCHEMA_PATH).build();
    }

    private static void hashCodeEqualsToStringTest(final TypeDefinition type1, final TypeDefinition type2) {
        assertEquals(type1.hashCode(), type2.hashCode());
        assertEquals(type1.toString(), type2.toString());
        assertTrue(type1.equals(type2));
    }

    private static <T> void testInstance(final T type1, final T type2) {
        assertEquals(type1, type2);
    }

    private static void restrictedBuilderTest(final Builder typeBuilder1, final Builder typeBuilder2) {
        final TypeDefinition typeDefinition1 = ((AbstractRestrictedTypeBuilder) typeBuilder1).buildType();
        final TypeDefinition typeDefinition2 = ((AbstractRestrictedTypeBuilder) typeBuilder2).buildType();
        hashCodeEqualsToStringTest(typeDefinition1, typeDefinition2);
    }
}
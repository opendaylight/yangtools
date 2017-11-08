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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;

public class TypeTest {
    private static final QName Q_NAME = QName.create("test.namespace", "2016-01-01", "test-name");
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, Q_NAME);
    private static final RevisionAwareXPath REVISION_AWARE_XPATH = new RevisionAwareXPathImpl("/test", true);
    private static final Bit BIT_A = BitBuilder.create(SCHEMA_PATH, 55L).setDescription("description")
            .setReference("reference").build();
    private static final Optional<String> ABSENT = Optional.empty();

    @Test
    public void binaryTypeTest() {
        final BaseBinaryType baseBinaryType1 = BaseBinaryType.INSTANCE;
        final BaseBinaryType baseBinaryType2 = (BaseBinaryType)BaseTypes.binaryType();
        hashCodeEqualsToStringTest(baseBinaryType1, baseBinaryType2);
        assertEquals(baseBinaryType1.getLengthConstraint(), baseBinaryType2.getLengthConstraint());

        final DerivedBinaryType derivedBinaryType1 = (DerivedBinaryType)DerivedTypes.derivedTypeBuilder(baseBinaryType1,
                SCHEMA_PATH).build();
        final DerivedBinaryType derivedBinaryType2 = (DerivedBinaryType)DerivedTypes.derivedTypeBuilder(baseBinaryType2,
                SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedBinaryType1, derivedBinaryType2);

        final RestrictedBinaryType restrictedBinaryType1 = (RestrictedBinaryType)RestrictedTypes.newBinaryBuilder(
                baseBinaryType1, SCHEMA_PATH).buildType();
        final RestrictedBinaryType restrictedBinaryType2 = (RestrictedBinaryType)RestrictedTypes.newBinaryBuilder(
                baseBinaryType2, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedBinaryType1, restrictedBinaryType2);

        final LengthRestrictedTypeBuilder<BinaryTypeDefinition> lengthRestrictedTypeBuilder = RestrictedTypes
                .newBinaryBuilder(baseBinaryType1, SCHEMA_PATH);
        final BaseBinaryType baseBinaryType = (BaseBinaryType)lengthRestrictedTypeBuilder.build();
        assertEquals(baseBinaryType, baseBinaryType1);
        concreteBuilderTest(baseBinaryType1, derivedBinaryType1);
    }

    @Test
    public void booleanTypeTest() {
        final BaseBooleanType baseBooleanType1 = BaseBooleanType.INSTANCE;
        final BaseBooleanType baseBooleanType2 = (BaseBooleanType)BaseTypes.booleanType();
        hashCodeEqualsToStringTest(baseBooleanType1, baseBooleanType2);

        final DerivedBooleanType derivedBooleanType1 = (DerivedBooleanType)DerivedTypes.derivedTypeBuilder(
                baseBooleanType1, SCHEMA_PATH).build();
        final DerivedBooleanType derivedBooleanType2 = (DerivedBooleanType)DerivedTypes.derivedTypeBuilder(
                baseBooleanType1, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedBooleanType1, derivedBooleanType2);

        restrictedBuilderTest(RestrictedTypes.newBooleanBuilder(baseBooleanType1, SCHEMA_PATH), RestrictedTypes
                .newBooleanBuilder(baseBooleanType2, SCHEMA_PATH));
        concreteBuilderTest(baseBooleanType1, derivedBooleanType1);
    }

    @Test
    public void identityrefTypeTest() {
        final IdentityrefTypeBuilder identityrefTypeBuilder1 = BaseTypes.identityrefTypeBuilder(SCHEMA_PATH);
        final IdentitySchemaNode identitySchemaNode = mock(IdentitySchemaNode.class);
        identityrefTypeBuilder1.addIdentity(identitySchemaNode);
        final IdentityrefTypeDefinition identityrefTypeDefinition1 = identityrefTypeBuilder1.build();
        final IdentityrefTypeBuilder identityrefTypeBuilder2 = BaseTypes.identityrefTypeBuilder(SCHEMA_PATH);
        identityrefTypeBuilder2.addIdentity(identitySchemaNode);
        final IdentityrefTypeDefinition identityrefTypeDefinition2 = identityrefTypeBuilder2.build();
        hashCodeEqualsToStringTest(identityrefTypeDefinition1, identityrefTypeDefinition2);

        final DerivedIdentityrefType derivedIdentityrefType1 = (DerivedIdentityrefType)DerivedTypes.derivedTypeBuilder(
                identityrefTypeDefinition1, SCHEMA_PATH).build();
        final DerivedIdentityrefType derivedIdentityrefType2 = (DerivedIdentityrefType)DerivedTypes.derivedTypeBuilder(
                identityrefTypeDefinition2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedIdentityrefType1, derivedIdentityrefType2);
        concreteBuilderTest(identityrefTypeDefinition1, derivedIdentityrefType1);

        restrictedBuilderTest(RestrictedTypes.newIdentityrefBuilder(derivedIdentityrefType1, SCHEMA_PATH),
                RestrictedTypes.newIdentityrefBuilder(derivedIdentityrefType2, SCHEMA_PATH));
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
        concreteBuilderTest(baseDecimalType1, derivedDecimalType1);
    }

    @Test
    public void emptyTypeTest() {
        final BaseEmptyType baseEmptyType1 = BaseEmptyType.INSTANCE;
        final BaseEmptyType baseEmptyType2 = (BaseEmptyType)BaseTypes.emptyType();
        hashCodeEqualsToStringTest(baseEmptyType1, baseEmptyType2);

        final DerivedEmptyType derivedEmptyType1 = (DerivedEmptyType)DerivedTypes.derivedTypeBuilder(
                baseEmptyType1, SCHEMA_PATH).build();
        final DerivedEmptyType derivedEmptyType2 = (DerivedEmptyType)DerivedTypes.derivedTypeBuilder(
                baseEmptyType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedEmptyType1, derivedEmptyType2);

        restrictedBuilderTest(RestrictedTypes.newEmptyBuilder(baseEmptyType1, SCHEMA_PATH),
                RestrictedTypes.newEmptyBuilder(baseEmptyType2, SCHEMA_PATH));
        concreteBuilderTest(baseEmptyType1, derivedEmptyType1);
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

        final InstanceIdentifierTypeBuilder instanceIdentifierBuilder1 = RestrictedTypes
                .newInstanceIdentifierBuilder(baseInstanceIdentifierType1, SCHEMA_PATH);
        instanceIdentifierBuilder1.setRequireInstance(true);
        final InstanceIdentifierTypeDefinition instanceIdentifierTypeDefinition1 = instanceIdentifierBuilder1
                .buildType();
        final InstanceIdentifierTypeBuilder instanceIdentifierBuilder2 = RestrictedTypes
                .newInstanceIdentifierBuilder(baseInstanceIdentifierType1, SCHEMA_PATH);
        instanceIdentifierBuilder2.setRequireInstance(true);
        final InstanceIdentifierTypeDefinition instanceIdentifierTypeDefinition2 = instanceIdentifierBuilder2
                .buildType();
        hashCodeEqualsToStringTest(instanceIdentifierTypeDefinition2, instanceIdentifierTypeDefinition1);
        concreteBuilderTest(baseInstanceIdentifierType1, derivedInstanceIdentifierType1);
    }

    @Test
    public void integerTypeTest() {
        final Int8TypeDefinition integerTypeDefinition8 = BaseTypes.int8Type();
        final Int16TypeDefinition integerTypeDefinition16 = BaseTypes.int16Type();
        final Int32TypeDefinition integerTypeDefinition32 = BaseTypes.int32Type();
        final Int64TypeDefinition integerTypeDefinition64 = BaseTypes.int64Type();
        assertTrue(BaseTypes.isInt8(integerTypeDefinition8));
        assertTrue(BaseTypes.isInt16(integerTypeDefinition16));
        assertTrue(BaseTypes.isInt32(integerTypeDefinition32));
        assertTrue(BaseTypes.isInt64(integerTypeDefinition64));
        testInstance(BaseInt8Type.INSTANCE, integerTypeDefinition8);
        testInstance(BaseInt16Type.INSTANCE, integerTypeDefinition16);
        testInstance(BaseInt32Type.INSTANCE, integerTypeDefinition32);
        testInstance(BaseInt64Type.INSTANCE, integerTypeDefinition64);

        final RestrictedInt8Type restrictedIntegerType1 = (RestrictedInt8Type)RestrictedTypes.newInt8Builder(
                integerTypeDefinition8, SCHEMA_PATH).buildType();
        final RestrictedInt8Type restrictedIntegerType2 = (RestrictedInt8Type)RestrictedTypes.newInt8Builder(
                BaseInt8Type.INSTANCE, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedIntegerType1, restrictedIntegerType2);

        final Uint8TypeDefinition integerTypeDefinitionu8 = BaseTypes.uint8Type();
        final Uint16TypeDefinition integerTypeDefinitionu16 = BaseTypes.uint16Type();
        final Uint32TypeDefinition integerTypeDefinitionu32 = BaseTypes.uint32Type();
        final Uint64TypeDefinition integerTypeDefinitionu64 = BaseTypes.uint64Type();
        assertTrue(BaseTypes.isUint8(integerTypeDefinitionu8));
        assertTrue(BaseTypes.isUint16(integerTypeDefinitionu16));
        assertTrue(BaseTypes.isUint32(integerTypeDefinitionu32));
        assertTrue(BaseTypes.isUint64(integerTypeDefinitionu64));
        testInstance(BaseUint8Type.INSTANCE, integerTypeDefinitionu8);
        testInstance(BaseUint16Type.INSTANCE, integerTypeDefinitionu16);
        testInstance(BaseUint32Type.INSTANCE, integerTypeDefinitionu32);
        testInstance(BaseUint64Type.INSTANCE, BaseTypes.baseTypeOf(integerTypeDefinitionu64));

        final DerivedInt8Type derivedIntegerType1 = (DerivedInt8Type)DerivedTypes
                .derivedTypeBuilder(integerTypeDefinition8, SCHEMA_PATH).build();
        final DerivedInt8Type derivedIntegerType2 = (DerivedInt8Type)DerivedTypes
                .derivedTypeBuilder(BaseInt8Type.INSTANCE, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedIntegerType1, derivedIntegerType2);

        final DerivedUint8Type derivedUnsignedType1 = (DerivedUint8Type)DerivedTypes
                .derivedTypeBuilder(integerTypeDefinitionu8, SCHEMA_PATH).build();
        final DerivedUint8Type derivedUnsignedType2 = (DerivedUint8Type)DerivedTypes
                .derivedTypeBuilder(BaseUint8Type.INSTANCE, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedUnsignedType1, derivedUnsignedType2);

        final RestrictedUint8Type restrictedUnsignedType1 = (RestrictedUint8Type)RestrictedTypes
                .newUint8Builder(integerTypeDefinitionu8, SCHEMA_PATH).buildType();
        final RestrictedUint8Type restrictedUnsignedType2 = (RestrictedUint8Type)RestrictedTypes
                .newUint8Builder(BaseUint8Type.INSTANCE, SCHEMA_PATH).buildType();
        hashCodeEqualsToStringTest(restrictedUnsignedType1, restrictedUnsignedType2);
        concreteBuilderTest(integerTypeDefinition8, derivedIntegerType1);
        concreteBuilderTest(integerTypeDefinitionu8, derivedUnsignedType2);

        final DerivedTypeBuilder<?> derivedTypeBuilder = DerivedTypes.derivedTypeBuilder(integerTypeDefinition8,
                SCHEMA_PATH);
        derivedTypeBuilder.setDefaultValue(1);
        derivedTypeBuilder.setDescription("test-description");
        derivedTypeBuilder.setReference("test-reference");
        derivedTypeBuilder.setUnits("Int");
        derivedTypeBuilder.setStatus(Status.CURRENT);
        assertEquals(derivedTypeBuilder.getStatus(), Status.CURRENT);
        assertEquals(derivedTypeBuilder.getDescription(), "test-description");
        assertEquals(derivedTypeBuilder.getReference(), "test-reference");
        assertEquals(derivedTypeBuilder.getUnits(), "Int");
    }

    @Test
    public void stringTypeTest() {
        final BaseStringType baseStringType1 = BaseStringType.INSTANCE;
        final BaseStringType baseStringType2 = (BaseStringType)BaseTypes.stringType();
        hashCodeEqualsToStringTest(baseStringType1, baseStringType2);
        assertEquals(baseStringType1.getLengthConstraint(), baseStringType2.getLengthConstraint());
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
        concreteBuilderTest(baseStringType1, derivedStringType1);

        final StringTypeBuilder stringTypeBuilder = new StringTypeBuilder(baseStringType1, SCHEMA_PATH);
        final PatternConstraint patternConstraint = BaseConstraints.newPatternConstraint("pattern", ABSENT, ABSENT);
        stringTypeBuilder.addPatternConstraint(patternConstraint);
        final StringTypeDefinition stringTypeDefinition = stringTypeBuilder.buildType();
        assertNotNull(stringTypeDefinition);
    }

    @Test
    public void bitsTypeTest() {
        final BitsTypeBuilder bitsTypeBuilder = BaseTypes.bitsTypeBuilder(SCHEMA_PATH);
        bitsTypeBuilder.addBit(BIT_A);
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
        concreteBuilderTest(bitsTypeDefinition1, derivedBitsType1);
    }

    @Test
    public void enumerationTypeTest() {
        final BaseEnumerationType baseEnumerationType1 = (BaseEnumerationType)BaseTypes.enumerationTypeBuilder(
                SCHEMA_PATH).build();
        final BaseEnumerationType baseEnumerationType2 = (BaseEnumerationType)BaseTypes.enumerationTypeBuilder(
                SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(baseEnumerationType1, baseEnumerationType2);
        assertEquals(baseEnumerationType1.getValues(), baseEnumerationType2.getValues());

        final DerivedEnumerationType derivedEnumerationType1 = (DerivedEnumerationType)DerivedTypes
                .derivedTypeBuilder(baseEnumerationType1, SCHEMA_PATH).build();
        final DerivedEnumerationType derivedEnumerationType2 = (DerivedEnumerationType)DerivedTypes
                .derivedTypeBuilder(baseEnumerationType2, SCHEMA_PATH).build();
        hashCodeEqualsToStringTest(derivedEnumerationType1, derivedEnumerationType2);

        restrictedBuilderTest(RestrictedTypes.newEnumerationBuilder(baseEnumerationType1, SCHEMA_PATH),
                RestrictedTypes.newEnumerationBuilder(baseEnumerationType2, SCHEMA_PATH));
        concreteBuilderTest(baseEnumerationType1, derivedEnumerationType1);
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
        concreteBuilderTest(baseLeafrefType1, derivedLeafrefType1);
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
        concreteBuilderTest(baseUnionType1, derivedUnionType1);
    }

    @Test
    public void abstractTypeDefinitionQnameTest() {
        final AbstractTypeDefinition<?> abstractTypeDefinition = (AbstractTypeDefinition<?>)
            BaseTypes.decimalTypeBuilder(SCHEMA_PATH).setFractionDigits(1).buildType();
        assertEquals(abstractTypeDefinition.getQName(), Q_NAME);
    }

    @Test
    public void abstractDerivedTypeTest() {
        final BaseBinaryType baseBinaryType1 = BaseBinaryType.INSTANCE;
        final AbstractDerivedType<?> abstractDerivedType = (AbstractDerivedType<?>)
            DerivedTypes.derivedTypeBuilder(baseBinaryType1, SCHEMA_PATH).build();
        assertEquals(Optional.empty(), abstractDerivedType.getDescription());
        assertEquals(Optional.empty(), abstractDerivedType.getReference());
        assertEquals(Status.CURRENT, abstractDerivedType.getStatus());
    }

    @Test
    public void concreteTypeBuilderBuildTest() {
        final BaseEnumerationType baseEnumerationType1 = (BaseEnumerationType)
            BaseTypes.enumerationTypeBuilder(SCHEMA_PATH).build();
        final ConcreteTypeBuilder<?> concreteTypeBuilder = ConcreteTypes.concreteTypeBuilder(
                baseEnumerationType1, SCHEMA_PATH);
        final TypeDefinition<?> typeDefinition = concreteTypeBuilder.build();
        assertNotNull(typeDefinition);
    }

    @Test
    public void constraintTypeBuilderTest() throws InvalidLengthConstraintException {
        final BaseBinaryType baseBinaryType = (BaseBinaryType)BaseTypes.binaryType();
        final LengthRestrictedTypeBuilder<?> lengthRestrictedTypeBuilder = RestrictedTypes
                .newBinaryBuilder(baseBinaryType, SCHEMA_PATH);
        final Long min = Long.valueOf(0);
        final UnresolvedNumber max = UnresolvedNumber.max();
        final List<ValueRange> lengthArrayList = ImmutableList.of(ValueRange.of(min, max));
        lengthRestrictedTypeBuilder.setLengthConstraint(mock(ConstraintMetaDefinition.class), lengthArrayList);
        final TypeDefinition<?> typeDefinition = lengthRestrictedTypeBuilder.buildType();
        assertNotNull(typeDefinition);

        final Int8TypeDefinition integerTypeDefinition8 = BaseTypes.int8Type();
        final RangeRestrictedTypeBuilder<?, ?> rangeRestrictedTypeBuilder = RestrictedTypes.newInt8Builder(
            integerTypeDefinition8, SCHEMA_PATH);
        rangeRestrictedTypeBuilder.setRangeConstraint(mock(ConstraintMetaDefinition.class), lengthArrayList);
        final TypeDefinition<?> typeDefinition1 = rangeRestrictedTypeBuilder.buildType();
        assertNotNull(typeDefinition1);
    }

    @Test
    public void exceptionTest() {
        final UnresolvedNumber min = UnresolvedNumber.min();
        final UnresolvedNumber max = UnresolvedNumber.max();
        final EnumPair enumPair = EnumPairBuilder.create("enum1", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(mock(UnknownSchemaNode.class)).build();

        final RangeSet<Integer> rangeset = ImmutableRangeSet.of(Range.closed(1, 2));
        final InvalidRangeConstraintException invalidRangeConstraintException = new InvalidRangeConstraintException(
                rangeset, "error msg", "other important messages");
        assertSame(rangeset, invalidRangeConstraintException.getOffendingRanges());

        final InvalidBitDefinitionException invalidBitDefinitionException = new InvalidBitDefinitionException(
                BIT_A, "error msg", "other important messages");
        assertEquals(invalidBitDefinitionException.getOffendingBit(), BIT_A);

        final InvalidEnumDefinitionException invalidEnumDefinitionException = new InvalidEnumDefinitionException(
                enumPair, "error msg", "other important messages");
        assertEquals(invalidEnumDefinitionException.getOffendingEnum(), enumPair);
    }

    @Test(expected = IllegalStateException.class)
    public void identityrefTypeBuilderException() {
        BaseTypes.identityrefTypeBuilder(SCHEMA_PATH).build();
    }

    @Test(expected = InvalidBitDefinitionException.class)
    public void invalidBitDefinitionExceptionTest() {
        final BitsTypeBuilder bitsTypeBuilder = BaseTypes.bitsTypeBuilder(SCHEMA_PATH);
        final QName qName = QName.create("test.namespace.1", "2016-01-02", "test-name-1");
        final SchemaPath schemaPath = SchemaPath.create(true, qName);
        bitsTypeBuilder.addBit(BIT_A);
        bitsTypeBuilder.addBit(BitBuilder.create(schemaPath, 55L).build());
        bitsTypeBuilder.build();
    }

    @Test(expected = InvalidEnumDefinitionException.class)
    public void invalidEnumDefinitionExceptionTest() {
        final UnknownSchemaNode unknown = mock(UnknownSchemaNode.class);
        final EnumPair enumPair1 = EnumPairBuilder.create("enum1", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(unknown).build();
        final EnumPair enumPair2 = EnumPairBuilder.create("enum", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(unknown).build();
        final EnumerationTypeBuilder enumerationTypeBuilder = BaseTypes.enumerationTypeBuilder(SCHEMA_PATH);
        enumerationTypeBuilder.addEnum(enumPair1);
        enumerationTypeBuilder.addEnum(enumPair2);
        enumerationTypeBuilder.build();
    }

    private static void hashCodeEqualsToStringTest(final TypeDefinition<?> type1, final TypeDefinition<?> type2) {
        assertEquals(type1.hashCode(), type2.hashCode());
        assertEquals(type1.toString(), type2.toString());
        assertTrue(type1.equals(type2));
    }

    private static <T> void testInstance(final T type1, final T type2) {
        assertEquals(type1, type2);
    }

    private static void restrictedBuilderTest(final Builder<?> typeBuilder1, final Builder<?> typeBuilder2) {
        final TypeDefinition<?> typeDefinition1 = ((AbstractRestrictedTypeBuilder<?>) typeBuilder1).buildType();
        final TypeDefinition<?> typeDefinition2 = ((AbstractRestrictedTypeBuilder<?>) typeBuilder2).buildType();
        hashCodeEqualsToStringTest(typeDefinition1, typeDefinition2);
    }

    private static void concreteBuilderTest(final TypeDefinition<?> baseTypeDef,
            final TypeDefinition<?> derivedTypeDef) {
        final ConcreteTypeBuilder<?> concreteTypeBuilder = ConcreteTypes.concreteTypeBuilder(baseTypeDef, SCHEMA_PATH);
        final TypeDefinition<?> typeDefinition = concreteTypeBuilder.buildType();
        assertEquals(typeDefinition.getBaseType(), derivedTypeDef.getBaseType());
    }
}

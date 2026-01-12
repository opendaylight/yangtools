/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;

@ExtendWith(MockitoExtension.class)
class TypeTest {
    private static final @NonNull QName Q_NAME = QName.create("test.namespace", "2016-01-01", "test-name");
    private static final Bit BIT_A = BitBuilder.create(Q_NAME.getLocalName(), Uint32.valueOf(55L))
        .setDescription("description")
        .setReference("reference")
        .build();

    @Mock
    private IdentitySchemaNode identitySchemaNode;
    @Mock
    private PatternConstraint patternConstraint;

    @Test
    void binaryTypeTest() {
        final var baseBinaryType1 = BaseBinaryType.INSTANCE;
        final var baseBinaryType2 = assertInstanceOf(BaseBinaryType.class, BaseTypes.binaryType());
        hashCodeEqualsToStringTest(baseBinaryType1, baseBinaryType2);
        assertEquals(baseBinaryType1.getLengthConstraint(), baseBinaryType2.getLengthConstraint());

        final var derivedBinaryType1 = assertInstanceOf(DerivedBinaryType.class,
            DerivedTypes.derivedTypeBuilder(baseBinaryType1, Q_NAME).build());
        final var derivedBinaryType2 = assertInstanceOf(DerivedBinaryType.class,
            DerivedTypes.derivedTypeBuilder(baseBinaryType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedBinaryType1, derivedBinaryType2);

        final var restrictedBinaryType1 = assertInstanceOf(RestrictedBinaryType.class,
            RestrictedTypes.newBinaryBuilder(baseBinaryType1, Q_NAME).buildType());
        final RestrictedBinaryType restrictedBinaryType2 = assertInstanceOf(RestrictedBinaryType.class,
            RestrictedTypes.newBinaryBuilder(baseBinaryType2, Q_NAME).buildType());
        hashCodeEqualsToStringTest(restrictedBinaryType1, restrictedBinaryType2);

        final var lengthRestrictedTypeBuilder = RestrictedTypes.newBinaryBuilder(baseBinaryType1, Q_NAME);
        final var baseBinaryType = assertInstanceOf(BaseBinaryType.class, lengthRestrictedTypeBuilder.build());
        assertEquals(baseBinaryType, baseBinaryType1);
        concreteBuilderTest(baseBinaryType1, derivedBinaryType1);
    }

    @Test
    void booleanTypeTest() {
        final var baseBooleanType1 = BaseBooleanType.INSTANCE;
        final var baseBooleanType2 = assertInstanceOf(BaseBooleanType.class, BaseTypes.booleanType());
        hashCodeEqualsToStringTest(baseBooleanType1, baseBooleanType2);

        final var derivedBooleanType1 = assertInstanceOf(DerivedBooleanType.class,
            DerivedTypes.derivedTypeBuilder(baseBooleanType1, Q_NAME).build());
        final var derivedBooleanType2 = assertInstanceOf(DerivedBooleanType.class,
            DerivedTypes.derivedTypeBuilder(baseBooleanType1, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedBooleanType1, derivedBooleanType2);

        restrictedBuilderTest(RestrictedTypes.newBooleanBuilder(baseBooleanType1, Q_NAME),
            RestrictedTypes.newBooleanBuilder(baseBooleanType2, Q_NAME));
        concreteBuilderTest(baseBooleanType1, derivedBooleanType1);
    }

    @Test
    void identityrefTypeTest() {
        final var identityrefTypeBuilder1 = BaseTypes.identityrefTypeBuilder(Q_NAME);
        doReturn("identitySchemaNode").when(identitySchemaNode).toString();
        identityrefTypeBuilder1.addIdentity(identitySchemaNode);
        final var identityrefTypeDefinition1 = identityrefTypeBuilder1.build();
        final var identityrefTypeBuilder2 = BaseTypes.identityrefTypeBuilder(Q_NAME);
        identityrefTypeBuilder2.addIdentity(identitySchemaNode);
        final var identityrefTypeDefinition2 = identityrefTypeBuilder2.build();
        hashCodeEqualsToStringTest(identityrefTypeDefinition1, identityrefTypeDefinition2);

        final var derivedIdentityrefType1 = assertInstanceOf(DerivedIdentityrefType.class,
            DerivedTypes.derivedTypeBuilder(identityrefTypeDefinition1, Q_NAME).build());
        final var derivedIdentityrefType2 = assertInstanceOf(DerivedIdentityrefType.class,
            DerivedTypes.derivedTypeBuilder(identityrefTypeDefinition2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedIdentityrefType1, derivedIdentityrefType2);
        concreteBuilderTest(identityrefTypeDefinition1, derivedIdentityrefType1);

        restrictedBuilderTest(RestrictedTypes.newIdentityrefBuilder(derivedIdentityrefType1, Q_NAME),
                RestrictedTypes.newIdentityrefBuilder(derivedIdentityrefType2, Q_NAME));
    }

    @Test
    void decimalTypeTest() {
        final var baseDecimalType1 = assertInstanceOf(BaseDecimalType.class,
            BaseTypes.decimalTypeBuilder(Q_NAME).setFractionDigits(1).buildType());
        final var baseDecimalType2 = assertInstanceOf(BaseDecimalType.class,
            BaseTypes.decimalTypeBuilder(Q_NAME).setFractionDigits(1).buildType());
        hashCodeEqualsToStringTest(baseDecimalType1, baseDecimalType2);
        assertEquals(baseDecimalType1.getFractionDigits(), baseDecimalType2.getFractionDigits());

        final var derivedDecimalType1 = assertInstanceOf(DerivedDecimalType.class,
            DerivedTypes.derivedTypeBuilder(baseDecimalType1, Q_NAME).build());
        final var derivedDecimalType2 = assertInstanceOf(DerivedDecimalType.class,
            DerivedTypes.derivedTypeBuilder(baseDecimalType1, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedDecimalType1, derivedDecimalType2);

        final var restrictedDecimalType1 = assertInstanceOf(RestrictedDecimalType.class,
            RestrictedTypes.newDecima64Builder(baseDecimalType1, Q_NAME).buildType());
        final var restrictedDecimalType2 = assertInstanceOf(RestrictedDecimalType.class,
            RestrictedTypes.newDecima64Builder(baseDecimalType2, Q_NAME).buildType());
        hashCodeEqualsToStringTest(restrictedDecimalType1, restrictedDecimalType2);
        concreteBuilderTest(baseDecimalType1, derivedDecimalType1);
    }

    @Test
    void emptyTypeTest() {
        final var baseEmptyType1 = BaseEmptyType.INSTANCE;
        final var baseEmptyType2 = assertInstanceOf(BaseEmptyType.class, BaseTypes.emptyType());
        hashCodeEqualsToStringTest(baseEmptyType1, baseEmptyType2);

        final DerivedEmptyType derivedEmptyType1 = assertInstanceOf(DerivedEmptyType.class,
            DerivedTypes.derivedTypeBuilder(baseEmptyType1, Q_NAME).build());
        final DerivedEmptyType derivedEmptyType2 = assertInstanceOf(DerivedEmptyType.class,
            DerivedTypes.derivedTypeBuilder(baseEmptyType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedEmptyType1, derivedEmptyType2);

        restrictedBuilderTest(RestrictedTypes.newEmptyBuilder(baseEmptyType1, Q_NAME),
            RestrictedTypes.newEmptyBuilder(baseEmptyType2, Q_NAME));
        concreteBuilderTest(baseEmptyType1, derivedEmptyType1);
    }

    @Test
    void instanceIdentifierTypeTest() {
        final var baseInstanceIdentifierType1 = BaseInstanceIdentifierType.INSTANCE;
        final var baseInstanceIdentifierType2 = assertInstanceOf(BaseInstanceIdentifierType.class,
            BaseTypes.instanceIdentifierType());
        hashCodeEqualsToStringTest(baseInstanceIdentifierType1, baseInstanceIdentifierType2);
        assertFalse(baseInstanceIdentifierType1.requireInstance());

        final var derivedInstanceIdentifierType1 = assertInstanceOf(DerivedInstanceIdentifierType.class,
            DerivedTypes.derivedTypeBuilder(baseInstanceIdentifierType1, Q_NAME).build());
        final var derivedInstanceIdentifierType2 = assertInstanceOf(DerivedInstanceIdentifierType.class,
            DerivedTypes.derivedTypeBuilder(baseInstanceIdentifierType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedInstanceIdentifierType1, derivedInstanceIdentifierType2);

        final var instanceIdentifierBuilder1 =
            RestrictedTypes.newInstanceIdentifierBuilder(baseInstanceIdentifierType1, Q_NAME);
        instanceIdentifierBuilder1.setRequireInstance(true);
        final var instanceIdentifierTypeDefinition1 = instanceIdentifierBuilder1.buildType();
        final var instanceIdentifierBuilder2 =
            RestrictedTypes.newInstanceIdentifierBuilder(baseInstanceIdentifierType1, Q_NAME);
        instanceIdentifierBuilder2.setRequireInstance(true);
        final var instanceIdentifierTypeDefinition2 = instanceIdentifierBuilder2.buildType();
        hashCodeEqualsToStringTest(instanceIdentifierTypeDefinition2, instanceIdentifierTypeDefinition1);
        concreteBuilderTest(baseInstanceIdentifierType1, derivedInstanceIdentifierType1);
    }

    @Test
    void integerTypeTest() {
        final var integerTypeDefinition8 = BaseTypes.int8Type();
        final var integerTypeDefinition16 = BaseTypes.int16Type();
        final var integerTypeDefinition32 = BaseTypes.int32Type();
        final var integerTypeDefinition64 = BaseTypes.int64Type();
        testInstance(BaseInt8Type.INSTANCE, integerTypeDefinition8);
        testInstance(BaseInt16Type.INSTANCE, integerTypeDefinition16);
        testInstance(BaseInt32Type.INSTANCE, integerTypeDefinition32);
        testInstance(BaseInt64Type.INSTANCE, integerTypeDefinition64);

        final var restrictedIntegerType1 = assertInstanceOf(RestrictedInt8Type.class,
            RestrictedTypes.newInt8Builder(integerTypeDefinition8, Q_NAME).buildType());
        final var restrictedIntegerType2 = assertInstanceOf(RestrictedInt8Type.class,
            RestrictedTypes.newInt8Builder(BaseInt8Type.INSTANCE, Q_NAME).buildType());
        hashCodeEqualsToStringTest(restrictedIntegerType1, restrictedIntegerType2);

        final var integerTypeDefinitionu8 = BaseTypes.uint8Type();
        final var integerTypeDefinitionu16 = BaseTypes.uint16Type();
        final var integerTypeDefinitionu32 = BaseTypes.uint32Type();
        final var integerTypeDefinitionu64 = BaseTypes.uint64Type();
        testInstance(BaseUint8Type.INSTANCE, integerTypeDefinitionu8);
        testInstance(BaseUint16Type.INSTANCE, integerTypeDefinitionu16);
        testInstance(BaseUint32Type.INSTANCE, integerTypeDefinitionu32);
        testInstance(BaseUint64Type.INSTANCE, BaseTypes.baseTypeOf(integerTypeDefinitionu64));

        final var derivedIntegerType1 = assertInstanceOf(DerivedInt8Type.class,
            DerivedTypes.derivedTypeBuilder(integerTypeDefinition8, Q_NAME).build());
        final var derivedIntegerType2 = assertInstanceOf(DerivedInt8Type.class,
            DerivedTypes.derivedTypeBuilder(BaseInt8Type.INSTANCE, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedIntegerType1, derivedIntegerType2);

        final var derivedUnsignedType1 = assertInstanceOf(DerivedUint8Type.class,
            DerivedTypes.derivedTypeBuilder(integerTypeDefinitionu8, Q_NAME).build());
        final var derivedUnsignedType2 = assertInstanceOf(DerivedUint8Type.class,
            DerivedTypes.derivedTypeBuilder(BaseUint8Type.INSTANCE, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedUnsignedType1, derivedUnsignedType2);

        final var restrictedUnsignedType1 = assertInstanceOf(RestrictedUint8Type.class,
            RestrictedTypes.newUint8Builder(integerTypeDefinitionu8, Q_NAME).buildType());
        final var restrictedUnsignedType2 = assertInstanceOf(RestrictedUint8Type.class,
            RestrictedTypes.newUint8Builder(BaseUint8Type.INSTANCE, Q_NAME).buildType());
        hashCodeEqualsToStringTest(restrictedUnsignedType1, restrictedUnsignedType2);
        concreteBuilderTest(integerTypeDefinition8, derivedIntegerType1);
        concreteBuilderTest(integerTypeDefinitionu8, derivedUnsignedType2);

        final var derivedTypeBuilder = DerivedTypes.derivedTypeBuilder(integerTypeDefinition8, Q_NAME);
        derivedTypeBuilder.setDefaultValue(1);
        derivedTypeBuilder.setDescription("test-description");
        derivedTypeBuilder.setReference("test-reference");
        derivedTypeBuilder.setUnits("Int");
        derivedTypeBuilder.setStatus(Status.CURRENT);
        assertEquals(Status.CURRENT, derivedTypeBuilder.getStatus());
        assertEquals("test-description", derivedTypeBuilder.getDescription());
        assertEquals("test-reference", derivedTypeBuilder.getReference());
        assertEquals("Int", derivedTypeBuilder.getUnits());
    }

    @Test
    void stringTypeTest() {
        final var baseStringType1 = BaseStringType.INSTANCE;
        final var baseStringType2 = assertInstanceOf(BaseStringType.class, BaseTypes.stringType());
        hashCodeEqualsToStringTest(baseStringType1, baseStringType2);
        assertEquals(baseStringType1.getLengthConstraint(), baseStringType2.getLengthConstraint());
        assertEquals(baseStringType1.getPatternConstraints(), baseStringType2.getPatternConstraints());

        final var derivedStringType1 = assertInstanceOf(DerivedStringType.class,
            DerivedTypes.derivedTypeBuilder(baseStringType1, Q_NAME).build());
        final var derivedStringType2 = assertInstanceOf(DerivedStringType.class,
            DerivedTypes.derivedTypeBuilder(baseStringType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedStringType1, derivedStringType2);

        final var restrictedStringType1 = assertInstanceOf(RestrictedStringType.class,
            RestrictedTypes.newStringBuilder(baseStringType1, Q_NAME).buildType());
        final var restrictedStringType2 = assertInstanceOf(RestrictedStringType.class,
            RestrictedTypes.newStringBuilder(baseStringType2, Q_NAME).buildType());
        hashCodeEqualsToStringTest(restrictedStringType1, restrictedStringType2);
        concreteBuilderTest(baseStringType1, derivedStringType1);

        final var stringTypeBuilder = new StringTypeBuilder(baseStringType1, Q_NAME);
        stringTypeBuilder.addPatternConstraint(patternConstraint);
        assertNotNull(stringTypeBuilder.buildType());
    }

    @Test
    void bitsTypeTest() {
        final var bitsTypeBuilder = BaseTypes.bitsTypeBuilder(Q_NAME).addBit(BIT_A);
        final var bitsTypeDefinition1 = bitsTypeBuilder.build();
        final var bitsTypeDefinition2 = bitsTypeBuilder.build();
        hashCodeEqualsToStringTest(bitsTypeDefinition1, bitsTypeDefinition2);
        assertEquals(bitsTypeDefinition1.getBits(), bitsTypeDefinition1.getBits());

        final var derivedBitsType1 = assertInstanceOf(DerivedBitsType.class,
            DerivedTypes.derivedTypeBuilder(bitsTypeDefinition1, Q_NAME).build());
        final var derivedBitsType2 = assertInstanceOf(DerivedBitsType.class,
            DerivedTypes.derivedTypeBuilder(bitsTypeDefinition2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedBitsType1, derivedBitsType2);

        restrictedBuilderTest(RestrictedTypes.newBitsBuilder(bitsTypeDefinition1, Q_NAME),
                RestrictedTypes.newBitsBuilder(bitsTypeDefinition2, Q_NAME));
        concreteBuilderTest(bitsTypeDefinition1, derivedBitsType1);
    }

    @Test
    void enumerationTypeTest() {
        final var baseEnumerationType1 = assertInstanceOf(BaseEnumerationType.class,
            BaseTypes.enumerationTypeBuilder(Q_NAME).build());
        final var baseEnumerationType2 = assertInstanceOf(BaseEnumerationType.class,
            BaseTypes.enumerationTypeBuilder(Q_NAME).build());
        hashCodeEqualsToStringTest(baseEnumerationType1, baseEnumerationType2);
        assertEquals(baseEnumerationType1.getValues(), baseEnumerationType2.getValues());

        final var derivedEnumerationType1 = assertInstanceOf(DerivedEnumerationType.class,
            DerivedTypes.derivedTypeBuilder(baseEnumerationType1, Q_NAME).build());
        final var derivedEnumerationType2 = assertInstanceOf(DerivedEnumerationType.class,
            DerivedTypes.derivedTypeBuilder(baseEnumerationType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedEnumerationType1, derivedEnumerationType2);

        restrictedBuilderTest(RestrictedTypes.newEnumerationBuilder(baseEnumerationType1, Q_NAME),
                RestrictedTypes.newEnumerationBuilder(baseEnumerationType2, Q_NAME));
        concreteBuilderTest(baseEnumerationType1, derivedEnumerationType1);
    }

    @Test
    void leafrefTypeTest() {
        final var expr = new PathExpression.LocationPath("/", YangLocationPath.root());

        final var leafrefTypeBuilder1 = BaseTypes.leafrefTypeBuilder(Q_NAME);
        final var leafrefTypeBuilder2 = BaseTypes.leafrefTypeBuilder(Q_NAME);
        leafrefTypeBuilder1.setPathStatement(expr);
        leafrefTypeBuilder2.setPathStatement(expr);
        final var baseLeafrefType1 = assertInstanceOf(BaseLeafrefType.class, leafrefTypeBuilder1.build());
        final var baseLeafrefType2 = assertInstanceOf(BaseLeafrefType.class, leafrefTypeBuilder1.build());
        hashCodeEqualsToStringTest(baseLeafrefType1, baseLeafrefType2);
        assertEquals(expr, baseLeafrefType1.getPathStatement());

        final var derivedLeafrefType1 = assertInstanceOf(DerivedLeafrefType.class,
            DerivedTypes.derivedTypeBuilder(baseLeafrefType1, Q_NAME).build());
        final var derivedLeafrefType2 = assertInstanceOf(DerivedLeafrefType.class,
            DerivedTypes.derivedTypeBuilder(baseLeafrefType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedLeafrefType1, derivedLeafrefType2);

        restrictedBuilderTest(RestrictedTypes.newLeafrefBuilder(baseLeafrefType1, Q_NAME),
            RestrictedTypes.newLeafrefBuilder(baseLeafrefType2, Q_NAME));
        concreteBuilderTest(baseLeafrefType1, derivedLeafrefType1);
    }

    @Test
    void unionTypeTest() {
        final var baseDecimalType1 = assertInstanceOf(BaseDecimalType.class, BaseTypes.decimalTypeBuilder(Q_NAME)
                .setFractionDigits(1)
                .buildType());
        final var baseDecimalType2 = assertInstanceOf(BaseDecimalType.class, BaseTypes.decimalTypeBuilder(Q_NAME)
                .setFractionDigits(1)
                .buildType());
        final var unionTypeBuilder1 = BaseTypes.unionTypeBuilder(Q_NAME);
        final var unionTypeBuilder2 = BaseTypes.unionTypeBuilder(Q_NAME);
        unionTypeBuilder1.addType(baseDecimalType1);
        unionTypeBuilder2.addType(baseDecimalType2);
        final var baseUnionType1 = assertInstanceOf(BaseUnionType.class, unionTypeBuilder1.build());
        final var baseUnionType2 = assertInstanceOf(BaseUnionType.class, unionTypeBuilder2.build());
        hashCodeEqualsToStringTest(baseUnionType1, baseUnionType2);
        assertEquals(baseUnionType1.getTypes(), baseUnionType2.getTypes());

        final var derivedUnionType1 = assertInstanceOf(DerivedUnionType.class,
            DerivedTypes.derivedTypeBuilder(baseUnionType1, Q_NAME).build());
        final var derivedUnionType2 = assertInstanceOf(DerivedUnionType.class,
            DerivedTypes.derivedTypeBuilder(baseUnionType2, Q_NAME).build());
        hashCodeEqualsToStringTest(derivedUnionType1, derivedUnionType2);

        restrictedBuilderTest(RestrictedTypes.newUnionBuilder(baseUnionType1, Q_NAME),
            RestrictedTypes.newUnionBuilder(baseUnionType2, Q_NAME));
        concreteBuilderTest(baseUnionType1, derivedUnionType1);
    }

    @Test
    void abstractTypeDefinitionQnameTest() {
        final var abstractTypeDefinition = assertInstanceOf(AbstractTypeDefinition.class,
            BaseTypes.decimalTypeBuilder(Q_NAME).setFractionDigits(1).buildType());
        assertEquals(Q_NAME, abstractTypeDefinition.getQName());
    }

    @Test
    void abstractDerivedTypeTest() {
        final var baseBinaryType1 = BaseBinaryType.INSTANCE;
        final var abstractDerivedType = assertInstanceOf(AbstractDerivedType.class,
            DerivedTypes.derivedTypeBuilder(baseBinaryType1, Q_NAME).build());
        assertEquals(Optional.empty(), abstractDerivedType.getDescription());
        assertEquals(Optional.empty(), abstractDerivedType.getReference());
        assertEquals(Status.CURRENT, abstractDerivedType.getStatus());
    }

    @Test
    void concreteTypeBuilderBuildTest() {
        final var baseEnumerationType1 = assertInstanceOf(BaseEnumerationType.class,
            BaseTypes.enumerationTypeBuilder(Q_NAME).build());
        final var concreteTypeBuilder = ConcreteTypes.concreteTypeBuilder(baseEnumerationType1, Q_NAME);
        final var typeDefinition = concreteTypeBuilder.build();
        assertNotNull(typeDefinition);
    }

    @Test
    void constraintTypeBuilderTest() throws InvalidLengthConstraintException {
        final var baseBinaryType = assertInstanceOf(BaseBinaryType.class, BaseTypes.binaryType());
        final var lengthRestrictedTypeBuilder = RestrictedTypes.newBinaryBuilder(baseBinaryType, Q_NAME);
        final Long min = 0L;
        final var max = UnresolvedNumber.max();
        final var lengthArrayList = List.of(ValueRange.of(min, max));
        lengthRestrictedTypeBuilder.setLengthConstraint(mock(ConstraintMetaDefinition.class), lengthArrayList);
        final var typeDefinition = lengthRestrictedTypeBuilder.buildType();
        assertNotNull(typeDefinition);

        final var integerTypeDefinition8 = BaseTypes.int8Type();
        final var rangeRestrictedTypeBuilder = RestrictedTypes.newInt8Builder(integerTypeDefinition8, Q_NAME);
        rangeRestrictedTypeBuilder.setRangeConstraint(mock(ConstraintMetaDefinition.class), lengthArrayList);
        final var typeDefinition1 = rangeRestrictedTypeBuilder.buildType();
        assertNotNull(typeDefinition1);
    }

    @Test
    void exceptionTest() {
        final var enumPair = EnumPairBuilder.create("enum1", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(mock(UnknownSchemaNode.class)).build();

        final var rangeset = ImmutableRangeSet.of(Range.closed(1, 2));
        final var invalidRangeConstraintException = new InvalidRangeConstraintException(
                rangeset, "error msg", "other important messages");
        assertSame(rangeset, invalidRangeConstraintException.getOffendingRanges());

        final var invalidBitDefinitionException = new InvalidBitDefinitionException(
                BIT_A, "error msg", "other important messages");
        assertEquals(BIT_A, invalidBitDefinitionException.getOffendingBit());

        final InvalidEnumDefinitionException invalidEnumDefinitionException = new InvalidEnumDefinitionException(
                enumPair, "error msg", "other important messages");
        assertEquals(invalidEnumDefinitionException.getOffendingEnum(), enumPair);
    }

    @Test
    void identityrefTypeBuilderException() {
        final var builder = BaseTypes.identityrefTypeBuilder(Q_NAME);
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void invalidBitDefinitionExceptionTest() {
        final var bitsTypeBuilder = BaseTypes.bitsTypeBuilder(Q_NAME)
                .addBit(BIT_A)
                .addBit(BitBuilder.create("test-name-1", Uint32.valueOf(55)).build());

        assertThrows(InvalidBitDefinitionException.class, () -> bitsTypeBuilder.build());
    }

    @Test
    void invalidEnumDefinitionExceptionTest() {
        final var unknown = mock(UnknownSchemaNode.class);
        final var enumPair1 = EnumPairBuilder.create("enum1", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(unknown).build();
        final var enumPair2 = EnumPairBuilder.create("enum", 1).setDescription("description")
                .setReference("reference").setUnknownSchemaNodes(unknown).build();
        final var enumerationTypeBuilder = BaseTypes.enumerationTypeBuilder(Q_NAME);
        enumerationTypeBuilder.addEnum(enumPair1);
        enumerationTypeBuilder.addEnum(enumPair2);

        assertThrows(InvalidEnumDefinitionException.class, () -> enumerationTypeBuilder.build());
    }

    private static void hashCodeEqualsToStringTest(final TypeDefinition<?> type1, final TypeDefinition<?> type2) {
        assertEquals(type1.hashCode(), type2.hashCode());
        assertEquals(type1.toString(), type2.toString());
        assertEquals(type1, type2);
    }

    private static <T> void testInstance(final T type1, final T type2) {
        assertEquals(type1, type2);
    }

    private static void restrictedBuilderTest(final TypeBuilder<?> typeBuilder1, final TypeBuilder<?> typeBuilder2) {
        final var typeDefinition1 = assertInstanceOf(AbstractRestrictedTypeBuilder.class, typeBuilder1).buildType();
        final var typeDefinition2 = assertInstanceOf(AbstractRestrictedTypeBuilder.class, typeBuilder2).buildType();
        hashCodeEqualsToStringTest(typeDefinition1, typeDefinition2);
    }

    private static void concreteBuilderTest(final TypeDefinition<?> baseTypeDef,
            final TypeDefinition<?> derivedTypeDef) {
        final var concreteTypeBuilder = ConcreteTypes.concreteTypeBuilder(baseTypeDef, Q_NAME);
        final var typeDefinition = concreteTypeBuilder.buildType();
        assertEquals(typeDefinition.getBaseType(), derivedTypeDef.getBaseType());
    }
}

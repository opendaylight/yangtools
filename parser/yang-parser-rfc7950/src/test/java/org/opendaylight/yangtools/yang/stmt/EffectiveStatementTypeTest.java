/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Range;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionAware;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class EffectiveStatementTypeTest {
    private static SchemaContext effectiveSchemaContext;
    private static Module types;

    private LeafSchemaNode currentLeaf;

    @BeforeAll
    static void setup() throws ReactorException {
        effectiveSchemaContext = RFC7950Reactors.defaultReactor().newBuild()
            .addSource(sourceForResource("/type-tests/types.yang"))
            .buildEffective();
        types = effectiveSchemaContext.findModules("types").iterator().next();
        assertNotNull(types);
    }

    @AfterAll
    static void teardown() {
        effectiveSchemaContext = null;
        types = null;
    }

    @Test
    void testBinary() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-binary"));
        assertNotNull(currentLeaf.getType());

        final BinaryTypeDefinition binaryEff = (BinaryTypeDefinition)
            ((TypeEffectiveStatement<?>) ((LeafEffectiveStatement) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(binaryEff.getBaseType());
        assertEquals(Optional.empty(), binaryEff.getUnits());
        assertEquals(Optional.empty(), binaryEff.getDefaultValue());
        assertEquals("binary", binaryEff.getQName().getLocalName());
        assertFalse(binaryEff.getLengthConstraint().isPresent());
        assertEquals(Status.CURRENT, binaryEff.getStatus());
        assertNotNull(binaryEff.getUnknownSchemaNodes());
        assertFalse(binaryEff.getDescription().isPresent());
        assertFalse(binaryEff.getReference().isPresent());
    }

    @Test
    void testBits() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-bits"));
        assertNotNull(currentLeaf.getType());

        final Iterator<? extends Bit> bitsEffIter = ((BitsTypeDefinition) currentLeaf.getType()).getBits().iterator();
        final Bit bitEff = bitsEffIter.next();
        final Bit bitEffSecond = bitsEffIter.next();

        final BitsTypeDefinition bitsEff = (BitsTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertNull(bitsEff.getBaseType());
        assertNotNull(bitsEff.getQName());
        assertEquals("bits", bitsEff.getQName().getLocalName());
        assertNotNull(bitsEff.getUnknownSchemaNodes());
        assertFalse(bitsEff.getDescription().isPresent());
        assertFalse(bitsEff.getReference().isPresent());
        assertEquals(Status.CURRENT, bitsEff.getStatus());
        assertEquals(Optional.empty(), bitsEff.getUnits());
        assertNotNull(bitsEff.toString());
        assertNotNull(bitsEff.hashCode());
        assertNotEquals(null, bitsEff);
        assertNotEquals("test", bitsEff);
        assertEquals(bitsEff, bitsEff);
        assertEquals(3, bitsEff.getBits().size());
        assertEquals(Optional.empty(), bitsEff.getDefaultValue());

        assertNotNull(bitEff.getUnknownSchemaNodes());
        assertEquals(Optional.of("test bit"), bitEff.getDescription());
        assertEquals(Optional.of("test bit ref"), bitEff.getReference());
        assertEquals(Status.CURRENT, bitEff.getStatus());
        assertNotNull(bitEff.hashCode());
        assertNotEquals(null, bitEff);
        assertNotEquals("test", bitEff);
        assertNotEquals(bitEff, bitEffSecond);
        assertNotNull(bitEff.toString());
        assertEquals("one", bitEff.getName());
        assertEquals(Uint32.ZERO, bitEff.getPosition());
    }

    @Test
    void testBoolean() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-boolean"));
        assertNotNull(currentLeaf.getType());
        final BooleanTypeDefinition booleanEff = (BooleanTypeDefinition) ((TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertNull(booleanEff.getBaseType());
        assertEquals(Optional.empty(), booleanEff.getUnits());
        assertEquals(Optional.empty(), booleanEff.getDefaultValue());
        assertEquals("boolean", booleanEff.getQName().getLocalName());
        assertNotNull(booleanEff.getUnknownSchemaNodes());
        assertFalse(booleanEff.getDescription().isPresent());
        assertFalse(booleanEff.getReference().isPresent());
        assertEquals(Status.CURRENT, booleanEff.getStatus());
        assertNotNull(booleanEff.toString());
    }

    @Test
    void testDecimal64() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-decimal64"));
        assertNotNull(currentLeaf.getType());
        final DecimalTypeDefinition decimal64Eff = (DecimalTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertNull(decimal64Eff.getBaseType());
        assertEquals(Optional.empty(), decimal64Eff.getUnits());
        assertEquals(Optional.empty(), decimal64Eff.getDefaultValue());
        assertEquals("decimal64", decimal64Eff.getQName().getLocalName());
        assertNotNull(decimal64Eff.getUnknownSchemaNodes());

        // FIXME: The yang model api is wrong: description/reference/status are not allowed under 'type', how come we
        // parse it?
        // allowed under 'type', how come we parse it?
        assertFalse(decimal64Eff.getDescription().isPresent());
        assertFalse(decimal64Eff.getReference().isPresent());
        assertEquals(Status.CURRENT, decimal64Eff.getStatus());

        assertEquals(3, decimal64Eff.getRangeConstraint().get().getAllowedRanges().asRanges().size());
        assertNotNull(decimal64Eff.toString());
        assertNotNull(decimal64Eff.hashCode());
        assertEquals(2, decimal64Eff.getFractionDigits());
        assertNotEquals(null, decimal64Eff);
        assertNotEquals("test", decimal64Eff);
        assertEquals(decimal64Eff, decimal64Eff);
    }

    @Test
    void testEmpty() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-empty"));
        assertNotNull(currentLeaf.getType());
        final EmptyTypeDefinition emptyEff = (EmptyTypeDefinition) ((TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals(Optional.empty(), emptyEff.getUnits());
        assertEquals(Optional.empty(), emptyEff.getDefaultValue());
        assertNull(emptyEff.getBaseType());
        assertEquals("empty", emptyEff.getQName().getLocalName());
        assertNotNull(emptyEff.getUnknownSchemaNodes());
        assertEquals(Optional.empty(), emptyEff.getDescription());
        assertEquals(Optional.empty(), emptyEff.getReference());
        assertEquals("CURRENT", emptyEff.getStatus().toString());
        assertNotNull(emptyEff.toString());
    }

    @Test
    void testEnum() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-enum"));
        assertNotNull(currentLeaf.getType());
        final List<EnumTypeDefinition.EnumPair> enumEffIter = ((EnumTypeDefinition) currentLeaf.getType()).getValues();
        final EnumPair enumEff = enumEffIter.iterator().next();

        final EnumTypeDefinition enumSpecEff = (EnumTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals("enumeration", enumSpecEff.getQName().getLocalName());
        assertEquals(Optional.empty(), enumSpecEff.getDefaultValue());
        assertEquals(3, enumSpecEff.getValues().size());
        assertNull(enumSpecEff.getBaseType());
        assertNotNull(enumSpecEff.getUnknownSchemaNodes());
        assertEquals(Status.CURRENT, enumSpecEff.getStatus());
        assertFalse(enumSpecEff.getDescription().isPresent());
        assertFalse(enumSpecEff.getReference().isPresent());
        assertEquals(Optional.empty(), enumSpecEff.getUnits());
        assertNotNull(enumSpecEff.toString());
        assertNotNull(enumSpecEff.hashCode());
        assertNotEquals(null, enumSpecEff);
        assertNotEquals("test", enumSpecEff);
        assertEquals(enumSpecEff, enumSpecEff);

        assertEquals("zero", enumEff.getName());
        assertNotNull(enumEff.getUnknownSchemaNodes());
        assertEquals(Optional.of("test enum"), enumEff.getDescription());
        assertEquals(Optional.of("test enum ref"), enumEff.getReference());
        assertEquals(Status.CURRENT, enumEff.getStatus());
        assertEquals(0, enumEff.getValue());
    }

    @Test
    void testIdentityRef() {
        currentLeaf = (LeafSchemaNode) types
            .getDataChildByName(QName.create(types.getQNameModule(), "leaf-identityref"));
        assertNotNull(currentLeaf.getType());
        final IdentityrefTypeDefinition identityRefEff = (IdentityrefTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals(Optional.empty(), identityRefEff.getDefaultValue());
        assertEquals("identityref", identityRefEff.getQName().getLocalName());
        assertNull(identityRefEff.getBaseType());
        assertNotNull(identityRefEff.getUnknownSchemaNodes());
        assertEquals(Status.CURRENT, identityRefEff.getStatus());
        assertEquals("test-identity", identityRefEff.getIdentities().iterator().next().getQName().getLocalName());
        assertFalse(identityRefEff.getDescription().isPresent());
        assertFalse(identityRefEff.getReference().isPresent());
        assertNotNull(identityRefEff.toString());

        // FIXME: the model is wrong, but we accept units in 'type' statement
        assertEquals(Optional.empty(), identityRefEff.getUnits());
    }

    @Test
    void testInstanceIdentifier() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
            "leaf-instance-identifier"));
        assertNotNull(currentLeaf.getType());
        final InstanceIdentifierTypeDefinition instanceIdentEff = (InstanceIdentifierTypeDefinition)
            ((TypeEffectiveStatement<?>) ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements()
                .iterator().next()).getTypeDefinition();
        assertNotNull(instanceIdentEff.toString());

        assertFalse(instanceIdentEff.requireInstance());
        assertEquals("instance-identifier", instanceIdentEff.getQName().getLocalName());
        assertNull(instanceIdentEff.getBaseType());
        assertEquals(Optional.empty(), instanceIdentEff.getDefaultValue());
        assertNotNull(instanceIdentEff.getUnknownSchemaNodes());
        assertFalse(instanceIdentEff.getDescription().isPresent());
        assertFalse(instanceIdentEff.getReference().isPresent());
        assertEquals(Optional.empty(), instanceIdentEff.getUnits());
        assertEquals(Status.CURRENT, instanceIdentEff.getStatus());
        assertNotNull(instanceIdentEff.hashCode());
        assertNotEquals(null, instanceIdentEff);
        assertNotEquals("test", instanceIdentEff);
        assertEquals(instanceIdentEff, instanceIdentEff);
    }

    @Test
    void testLeafref() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-leafref"));
        assertNotNull(currentLeaf.getType());

        final LeafrefTypeDefinition leafrefEff = (LeafrefTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals("/container-test/leaf-test", leafrefEff.getPathStatement().getOriginalString());
        assertNull(leafrefEff.getBaseType());
        assertEquals(Optional.empty(), leafrefEff.getUnits());
        assertEquals(Optional.empty(), leafrefEff.getDefaultValue());
        assertNotNull(leafrefEff.toString());
        assertEquals("leafref", leafrefEff.getQName().getLocalName());
        assertEquals(Status.CURRENT, leafrefEff.getStatus());
        assertNotNull(leafrefEff.getUnknownSchemaNodes());
        assertFalse(leafrefEff.getDescription().isPresent());
        assertFalse(leafrefEff.getReference().isPresent());
        assertNotNull(leafrefEff.hashCode());
        assertNotEquals(null, leafrefEff);
        assertNotEquals("test", leafrefEff);
        assertEquals(leafrefEff, leafrefEff);
    }

    @Test
    void testLeafrefWithDeref() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName
            .create(types.getQNameModule(), "leaf-leafref-deref"));
        assertNotNull(currentLeaf.getType());

        final LeafrefTypeDefinition leafrefEff = (LeafrefTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals("deref(../container-test)/leaf-test",
            leafrefEff.getPathStatement().getOriginalString());
        assertNull(leafrefEff.getBaseType());
        assertEquals(Optional.empty(), leafrefEff.getUnits());
        assertEquals(Optional.empty(), leafrefEff.getDefaultValue());
        assertNotNull(leafrefEff.toString());
        assertEquals("leafref", leafrefEff.getQName().getLocalName());
        assertEquals(Status.CURRENT, leafrefEff.getStatus());
        assertNotNull(leafrefEff.getUnknownSchemaNodes());
        assertFalse(leafrefEff.getDescription().isPresent());
        assertFalse(leafrefEff.getReference().isPresent());
        assertNotNull(leafrefEff.hashCode());
        assertNotEquals(null, leafrefEff);
        assertNotEquals("test", leafrefEff);
        assertEquals(leafrefEff, leafrefEff);
    }

    @Test
    void testIntAll() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int8"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int8Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(int8Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int16"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int16Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(int16Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int32"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int32Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(int32Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int64"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int64Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(int64Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint8"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint8Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(uint8Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint16"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint16Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(uint16Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint32"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint32Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(uint32Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint64"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint64Eff = (TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next();
        assertNotNull(uint64Eff.toString());
    }

    @Test
    void testUnion() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-union"));
        assertNotNull(currentLeaf.getType());
        final UnionTypeDefinition unionEff = (UnionTypeDefinition) ((TypeDefinitionAware)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals(2, unionEff.getTypes().size());
        assertEquals("union", unionEff.getQName().getLocalName());
        assertEquals("CURRENT", unionEff.getStatus().toString());
        assertNotNull(unionEff.getUnknownSchemaNodes());
        assertNull(unionEff.getBaseType());
        assertEquals(Optional.empty(), unionEff.getUnits());
        assertEquals(Optional.empty(), unionEff.getDefaultValue());
        assertFalse(unionEff.getDescription().isPresent());
        assertFalse(unionEff.getReference().isPresent());
        assertNotNull(unionEff.toString());
        assertNotNull(unionEff.hashCode());
        assertNotEquals(null, unionEff);
        assertNotEquals("test", unionEff);
        assertEquals(unionEff, unionEff);
    }

    @Test
    void testLengthConstraint() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
            "leaf-length-pattern"));

        final StringTypeDefinition leafType = (StringTypeDefinition) currentLeaf.getType();
        assertNotNull(leafType);
        final LengthConstraint lengthConstraint = leafType.getLengthConstraint().get();

        final Range<Integer> span = lengthConstraint.getAllowedRanges().span();
        assertEquals(1, span.lowerEndpoint().intValue());
        assertEquals(255, span.upperEndpoint().intValue());
        assertFalse(lengthConstraint.getReference().isPresent());
        assertFalse(lengthConstraint.getDescription().isPresent());
        assertFalse(lengthConstraint.getErrorMessage().isPresent());
        assertFalse(lengthConstraint.getErrorAppTag().isPresent());
        assertNotNull(lengthConstraint.toString());
        assertNotNull(lengthConstraint.hashCode());
        assertNotEquals(null, lengthConstraint);
        assertNotEquals("test", lengthConstraint);

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
            "leaf-length-pattern-second"));
        assertNotNull(currentLeaf.getType());
        final LengthConstraint lengthConstraintSecond = ((StringTypeDefinition) currentLeaf.getType())
            .getLengthConstraint().get();
        assertNotEquals(lengthConstraint, lengthConstraintSecond);
    }

    @Test
    void testPatternConstraint() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
            "leaf-length-pattern"));
        assertNotNull(currentLeaf.getType());
        final PatternConstraint patternConstraint = ((StringTypeDefinition) currentLeaf.getType())
            .getPatternConstraints().get(0);
        final PatternConstraint patternConstraintThird = ((StringTypeDefinition) currentLeaf.getType())
            .getPatternConstraints().get(0);
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
            "leaf-length-pattern-second"));
        assertNotNull(currentLeaf.getType());
        final PatternConstraint patternConstraintSecond = ((StringTypeDefinition) currentLeaf.getType())
            .getPatternConstraints().get(0);

        assertEquals("^(?:[0-9a-fA-F]*)$", patternConstraint.getJavaPatternString());
        assertFalse(patternConstraint.getReference().isPresent());
        assertFalse(patternConstraint.getDescription().isPresent());
        assertEquals(Optional.empty(), patternConstraint.getErrorMessage());
        assertEquals(Optional.empty(), patternConstraint.getErrorAppTag());
        assertNotNull(patternConstraint.toString());
        assertNotNull(patternConstraint.hashCode());
        assertNotEquals(null, patternConstraint);
        assertNotEquals("test", patternConstraint);
        assertNotEquals(patternConstraint, patternConstraintSecond);
        assertEquals(patternConstraint, patternConstraintThird);
    }

    @Test
    void testString() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-string"));
        assertNotNull(currentLeaf.getType());
        final StringTypeDefinition stringEff = (StringTypeDefinition) ((TypeEffectiveStatement<?>)
            ((LeafEffectiveStatement) currentLeaf).effectiveSubstatements().iterator().next())
            .getTypeDefinition();

        assertEquals("string", stringEff.getQName().getLocalName());
        assertEquals(Status.CURRENT, stringEff.getStatus());
        assertEquals(Optional.empty(), stringEff.getUnits());
        assertEquals(Optional.empty(), stringEff.getDefaultValue());
        assertNotNull(stringEff.getUnknownSchemaNodes());
        assertNull(stringEff.getBaseType());
        assertFalse(stringEff.getDescription().isPresent());
        assertFalse(stringEff.getReference().isPresent());
        assertNotNull(stringEff.toString());
        assertNotNull(stringEff.hashCode());
        assertNotEquals(null, stringEff);
        assertNotEquals("test", stringEff);
        assertEquals(stringEff, stringEff);
        assertFalse(stringEff.getLengthConstraint().isPresent());
        assertNotNull(stringEff.getPatternConstraints());
    }
}

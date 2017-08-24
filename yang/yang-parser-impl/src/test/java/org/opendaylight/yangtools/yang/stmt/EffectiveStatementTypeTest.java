/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Range;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
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
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Decimal64SpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IdentityRefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LeafrefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnionSpecificationEffectiveStatementImpl;

public class EffectiveStatementTypeTest {

    private static final StatementStreamSource IMPORTED_MODULE = sourceForResource("/type-tests/types.yang");
    private static SchemaContext effectiveSchemaContext;
    private static LeafSchemaNode currentLeaf;
    private static Module types;

    @Before
    public void setup() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(IMPORTED_MODULE);
        effectiveSchemaContext = reactor.buildEffective();
        types = effectiveSchemaContext.findModuleByName("types", null);
        assertNotNull(types);
    }

    @Test
    public void testBinary() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-binary"));
        assertNotNull(currentLeaf.getType());

        final BinaryTypeDefinition binaryEff = (BinaryTypeDefinition) ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(binaryEff.getBaseType());
        assertNull(binaryEff.getUnits());
        assertNull(binaryEff.getDefaultValue());
        assertEquals("binary", binaryEff.getQName().getLocalName());
        assertEquals(0, binaryEff.getLengthConstraints().asMapOfRanges().size());
        assertEquals("CURRENT", binaryEff.getStatus().toString());
        assertEquals("binary", binaryEff.getPath().getPathFromRoot().iterator().next().getLocalName());
        assertNotNull(binaryEff.getUnknownSchemaNodes());
        assertNull(binaryEff.getDescription());
        assertNull(binaryEff.getReference());
    }

    @Test
    public void testBits() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-bits"));
        assertNotNull(currentLeaf.getType());

        final List<BitsTypeDefinition.Bit> bitsEffIter = ((BitsTypeDefinition) currentLeaf.getType()).getBits();
        final Bit bitEff = bitsEffIter.get(0);
        final Bit bitEffSecond = bitsEffIter.get(1);

        final BitsTypeDefinition bitsEff = ((BitsSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(bitsEff.getBaseType());
        assertNotNull(bitsEff.getQName());
        assertEquals("bits", bitsEff.getQName().getLocalName());
        assertEquals("bits", bitsEff.getPath().getLastComponent().getLocalName());
        assertNotNull(bitsEff.getUnknownSchemaNodes());
        assertNull(bitsEff.getDescription());
        assertNull(bitsEff.getReference());
        assertEquals("CURRENT", bitsEff.getStatus().toString());
        assertNull(bitsEff.getUnits());
        assertNotNull(bitsEff.toString());
        assertNotNull(bitsEff.hashCode());
        assertFalse(bitsEff.equals(null));
        assertFalse(bitsEff.equals("test"));
        assertTrue(bitsEff.equals(bitsEff));
        assertEquals(3, bitsEff.getBits().size());
        assertNull(bitsEff.getDefaultValue());

        assertNotNull(bitEff.getPath());
        assertNotNull(bitEff.getUnknownSchemaNodes());
        assertEquals("test bit", bitEff.getDescription());
        assertEquals("test bit ref", bitEff.getReference());
        assertEquals("CURRENT", bitEff.getStatus().toString());
        assertNotNull(bitEff.hashCode());
        assertFalse(bitEff.equals(null));
        assertFalse(bitEff.equals("test"));
        assertFalse(bitEff.equals(bitEffSecond));
        assertNotNull(bitEff.toString());
        assertEquals("one", bitEff.getName());
        assertNotNull(bitEff.getQName());
        assertEquals(0, bitEff.getPosition());
    }

    @Test
    public void testBoolean() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-boolean"));
        assertNotNull(currentLeaf.getType());
        final BooleanTypeDefinition booleanEff = (BooleanTypeDefinition) ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(booleanEff.getBaseType());
        assertNull(booleanEff.getUnits());
        assertNull(booleanEff.getDefaultValue());
        assertEquals("boolean", booleanEff.getQName().getLocalName());
        assertNull(booleanEff.getPath().getParent().getParent());
        assertNotNull(booleanEff.getUnknownSchemaNodes());
        assertNull(booleanEff.getDescription());
        assertNull(booleanEff.getReference());
        assertEquals("CURRENT", booleanEff.getStatus().toString());
        assertNotNull(booleanEff.toString());
    }

    @Test
    public void testDecimal64() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-decimal64"));
        assertNotNull(currentLeaf.getType());
        final DecimalTypeDefinition decimal64Eff = ((Decimal64SpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(decimal64Eff.getBaseType());
        assertNull(decimal64Eff.getUnits());
        assertNull(decimal64Eff.getDefaultValue());
        assertEquals("decimal64", decimal64Eff.getQName().getLocalName());
        assertNotNull(decimal64Eff.getUnknownSchemaNodes());

        // FIXME: The yang model api is wrong: description/reference/status are not allowed under 'type', how come we parse it?
        // allowed under 'type', how come we parse it?
        assertNull(decimal64Eff.getDescription());
        assertNull(decimal64Eff.getReference());
        assertEquals("CURRENT", decimal64Eff.getStatus().toString());

        assertEquals(3, decimal64Eff.getRangeConstraints().size());
        assertNotNull(decimal64Eff.toString());
        assertNotNull(decimal64Eff.hashCode());
        assertTrue(decimal64Eff.getFractionDigits().equals(2));
        assertFalse(decimal64Eff.equals(null));
        assertFalse(decimal64Eff.equals("test"));
        assertTrue(decimal64Eff.equals(decimal64Eff));
        assertEquals("decimal64", decimal64Eff.getPath().getLastComponent().getLocalName());
    }

    @Test
    public void testEmpty() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-empty"));
        assertNotNull(currentLeaf.getType());
        final EmptyTypeDefinition emptyEff = (EmptyTypeDefinition) ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(emptyEff.getUnits());
        assertNull(emptyEff.getDefaultValue());
        assertNull(emptyEff.getBaseType());
        assertEquals("empty", emptyEff.getQName().getLocalName());
        assertNull(emptyEff.getPath().getParent().getParent());
        assertNotNull(emptyEff.getUnknownSchemaNodes());
        assertNull(emptyEff.getDescription());
        assertNull(emptyEff.getReference());
        assertEquals("CURRENT", emptyEff.getStatus().toString());
        assertNotNull(emptyEff.toString());
    }

    @Test
    public void testEnum() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-enum"));
        assertNotNull(currentLeaf.getType());
        final List<EnumTypeDefinition.EnumPair> enumEffIter = ((EnumTypeDefinition) currentLeaf.getType()).getValues();
        final EnumPair enumEff = enumEffIter.iterator().next();

        final EnumTypeDefinition enumSpecEff = ((EnumSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertEquals("enumeration", enumSpecEff.getQName().getLocalName());
        assertEquals("enumeration", enumSpecEff.getPath().getLastComponent().getLocalName());
        assertNull(enumSpecEff.getDefaultValue());
        assertEquals(3, enumSpecEff.getValues().size());
        assertNull(enumSpecEff.getBaseType());
        assertNotNull(enumSpecEff.getUnknownSchemaNodes());
        assertEquals("CURRENT", enumSpecEff.getStatus().toString());
        assertNull(enumSpecEff.getDescription());
        assertNull(enumSpecEff.getReference());
        assertNull(enumSpecEff.getUnits());
        assertNotNull(enumSpecEff.toString());
        assertNotNull(enumSpecEff.hashCode());
        assertFalse(enumSpecEff.equals(null));
        assertFalse(enumSpecEff.equals("test"));
        assertTrue(enumSpecEff.equals(enumSpecEff));

        assertEquals("zero", enumEff.getName());
        assertNotNull(enumEff.getUnknownSchemaNodes());
        assertEquals("test enum", enumEff.getDescription());
        assertEquals("test enum ref", enumEff.getReference());
        assertEquals("CURRENT", enumEff.getStatus().toString());
        assertEquals(0, enumEff.getValue());
    }

    @Test
    public void testIdentityRef() {
        currentLeaf = (LeafSchemaNode) types
                .getDataChildByName(QName.create(types.getQNameModule(), "leaf-identityref"));
        assertNotNull(currentLeaf.getType());
        final IdentityrefTypeDefinition identityRefEff = ((IdentityRefSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(identityRefEff.getDefaultValue());
        assertEquals("identityref", identityRefEff.getQName().getLocalName());
        assertEquals("identityref", identityRefEff.getPath().getLastComponent().getLocalName());
        assertNull(identityRefEff.getBaseType());
        assertNotNull(identityRefEff.getUnknownSchemaNodes());
        assertEquals("CURRENT", identityRefEff.getStatus().toString());
        assertEquals("test-identity", identityRefEff.getIdentity().getQName().getLocalName());
        assertNull(identityRefEff.getDescription());
        assertNull(identityRefEff.getReference());
        assertNotNull(identityRefEff.toString());

        // FIXME: the model is wrong, but we accept units in 'type' statement
        assertNull(identityRefEff.getUnits());
    }

    @Test
    public void testInstanceIdentifier() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
                "leaf-instance-identifier"));
        assertNotNull(currentLeaf.getType());
        final InstanceIdentifierTypeDefinition instanceIdentEff = (InstanceIdentifierTypeDefinition) ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();
        assertNotNull(instanceIdentEff.toString());

        assertFalse(instanceIdentEff.requireInstance());
        assertEquals("instance-identifier", instanceIdentEff.getQName().getLocalName());
        assertEquals("instance-identifier", instanceIdentEff.getPath().getLastComponent().getLocalName());
        assertNull(instanceIdentEff.getBaseType());
        assertNull(instanceIdentEff.getDefaultValue());
        assertNotNull(instanceIdentEff.getUnknownSchemaNodes());
        assertNull(instanceIdentEff.getDescription());
        assertNull(instanceIdentEff.getReference());
        assertNull(instanceIdentEff.getUnits());
        assertEquals("CURRENT", instanceIdentEff.getStatus().toString());
        assertNotNull(instanceIdentEff.hashCode());
        assertFalse(instanceIdentEff.equals(null));
        assertFalse(instanceIdentEff.equals("test"));
        assertTrue(instanceIdentEff.equals(instanceIdentEff));
    }

    @Test
    public void testLeafref() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-leafref"));
        assertNotNull(currentLeaf.getType());

        final LeafrefTypeDefinition leafrefEff = ((LeafrefSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertEquals("/container-test/leaf-test", leafrefEff.getPathStatement().toString());
        assertNull(leafrefEff.getBaseType());
        assertNull(leafrefEff.getUnits());
        assertNull(leafrefEff.getDefaultValue());
        assertNotNull(leafrefEff.toString());
        assertEquals("leafref", leafrefEff.getQName().getLocalName());
        assertEquals("CURRENT", leafrefEff.getStatus().toString());
        assertNotNull(leafrefEff.getUnknownSchemaNodes());
        assertEquals("leafref", leafrefEff.getPath().getLastComponent().getLocalName());
        assertNull(leafrefEff.getDescription());
        assertNull(leafrefEff.getReference());
        assertNotNull(leafrefEff.hashCode());
        assertFalse(leafrefEff.equals(null));
        assertFalse(leafrefEff.equals("test"));
        assertTrue(leafrefEff.equals(leafrefEff));
    }

    @Test
    public void testIntAll() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int8"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int8Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int8Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int16"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int16Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int16Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int32"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int32Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int32Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-int64"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> int64Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int64Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint8"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint8Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint8Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint16"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint16Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint16Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint32"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint32Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint32Eff.toString());

        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-uint64"));
        assertNotNull(currentLeaf.getType());
        final TypeEffectiveStatement<?> uint64Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint64Eff.toString());
    }

    @Test
    public void testUnion() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-union"));
        assertNotNull(currentLeaf.getType());
        final UnionTypeDefinition unionEff = ((UnionSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertEquals(2, unionEff.getTypes().size());
        assertEquals("union", unionEff.getQName().getLocalName());
        assertEquals("CURRENT", unionEff.getStatus().toString());
        assertNotNull(unionEff.getUnknownSchemaNodes());
        assertNull(unionEff.getBaseType());
        assertNull(unionEff.getUnits());
        assertNull(unionEff.getDefaultValue());
        assertNull(unionEff.getDescription());
        assertNull(unionEff.getReference());
        assertNotNull(unionEff.toString());
        assertNotNull(unionEff.hashCode());
        assertFalse(unionEff.equals(null));
        assertFalse(unionEff.equals("test"));
        assertTrue(unionEff.equals(unionEff));
        assertEquals("union", unionEff.getPath().getLastComponent().getLocalName());
    }

    @Test
    public void testLengthConstraint() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
                "leaf-length-pattern"));
        assertNotNull(currentLeaf.getType());
        final Entry<Range<Integer>, ConstraintMetaDefinition> lengthConstraint =
                ((StringTypeDefinition) currentLeaf.getType())
                .getLengthConstraints().asMapOfRanges().entrySet().iterator().next();
        final Entry<Range<Integer>, ConstraintMetaDefinition> lengthConstraintThird =
                ((StringTypeDefinition) currentLeaf.getType())
                .getLengthConstraints().asMapOfRanges().entrySet().iterator().next();
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
                "leaf-length-pattern-second"));
        assertNotNull(currentLeaf.getType());
        final Entry<Range<Integer>, ConstraintMetaDefinition> lengthConstraintSecond =
                ((StringTypeDefinition) currentLeaf.getType())
                .getLengthConstraints().asMapOfRanges().entrySet().iterator().next();

        assertEquals(1, lengthConstraint.getKey().lowerEndpoint().intValue());
        assertEquals(255, lengthConstraint.getKey().upperEndpoint().intValue());
        assertNull(lengthConstraint.getValue().getReference());
        assertNull(lengthConstraint.getValue().getDescription());
        assertEquals("The argument is out of bounds <1, 255>", lengthConstraint.getValue().getErrorMessage());
        assertEquals("length-out-of-specified-bounds", lengthConstraint.getValue().getErrorAppTag());
        assertNotNull(lengthConstraint.toString());
        assertNotNull(lengthConstraint.hashCode());
        assertFalse(lengthConstraint.equals(null));
        assertFalse(lengthConstraint.equals("test"));
        assertFalse(lengthConstraint.equals(lengthConstraintSecond));
        assertTrue(lengthConstraint.equals(lengthConstraintThird));
    }

    @Test
    public void testPatternConstraint() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
                "leaf-length-pattern"));
        assertNotNull(currentLeaf.getType());
        final PatternConstraintEffectiveImpl patternConstraint = (PatternConstraintEffectiveImpl) ((StringTypeDefinition) currentLeaf
                .getType()).getPatternConstraints().get(0);
        final PatternConstraintEffectiveImpl patternConstraintThird = (PatternConstraintEffectiveImpl) ((StringTypeDefinition) currentLeaf
                .getType()).getPatternConstraints().get(0);
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(),
                "leaf-length-pattern-second"));
        assertNotNull(currentLeaf.getType());
        final PatternConstraintEffectiveImpl patternConstraintSecond = (PatternConstraintEffectiveImpl) ((StringTypeDefinition) currentLeaf
                .getType()).getPatternConstraints().get(0);

        assertEquals("^[0-9a-fA-F]*$", patternConstraint.getRegularExpression());
        assertNull(patternConstraint.getReference());
        assertNull(patternConstraint.getDescription());
        assertEquals("Supplied value does not match the regular expression ^[0-9a-fA-F]*$.", patternConstraint.getErrorMessage());
        assertEquals("invalid-regular-expression", patternConstraint.getErrorAppTag());
        assertNotNull(patternConstraint.toString());
        assertNotNull(patternConstraint.hashCode());
        assertFalse(patternConstraint.equals(null));
        assertFalse(patternConstraint.equals("test"));
        assertFalse(patternConstraint.equals(patternConstraintSecond));
        assertTrue(patternConstraint.equals(patternConstraintThird));
    }

    @Test
    public void testString() {
        currentLeaf = (LeafSchemaNode) types.getDataChildByName(QName.create(types.getQNameModule(), "leaf-string"));
        assertNotNull(currentLeaf.getType());
        final StringTypeDefinition stringEff = (StringTypeDefinition) ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertEquals("string", stringEff.getQName().getLocalName());
        assertEquals("CURRENT", stringEff.getStatus().toString());
        assertNull(stringEff.getUnits());
        assertNull(stringEff.getDefaultValue());
        assertNotNull(stringEff.getUnknownSchemaNodes());
        assertNull(stringEff.getBaseType());
        assertNull(stringEff.getDescription());
        assertNull(stringEff.getReference());
        assertNotNull(stringEff.toString());
        assertNotNull(stringEff.hashCode());
        assertFalse(stringEff.equals(null));
        assertFalse(stringEff.equals("test"));
        assertTrue(stringEff.equals(stringEff));
        assertEquals("string", stringEff.getPath().getLastComponent().getLocalName());
        assertEquals(0, stringEff.getLengthConstraints().asMapOfRanges().size());
        assertNotNull(stringEff.getPatternConstraints());
    }
}

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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Decimal64SpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IdentityRefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LeafrefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UnionSpecificationEffectiveStatementImpl;

public class EffectiveStatementTypeTest {

    private static final YangStatementSourceImpl IMPORTED_MODULE = new YangStatementSourceImpl(
            "/type-tests/types.yang", false);
    private static EffectiveSchemaContext effectiveSchemaContext;
    private static LeafSchemaNode currentLeaf;

    @Before
    public void setup() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSource(IMPORTED_MODULE);
        effectiveSchemaContext = reactor.buildEffective();
    }

    @Test
    public void testBinary() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-binary");
        assertNotNull(currentLeaf.getType());

        BinaryTypeDefinition binaryEff = (BinaryTypeDefinition) ((TypeEffectiveStatement<?>)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(binaryEff.getBaseType());
        assertNull(binaryEff.getUnits());
        assertNull(binaryEff.getDefaultValue());
        assertEquals("binary", binaryEff.getQName().getLocalName());
        assertEquals(0, binaryEff.getLengthConstraints().size());
        assertEquals("CURRENT", binaryEff.getStatus().toString());
        assertEquals("binary", binaryEff.getPath().getPathFromRoot().iterator().next().getLocalName());
        assertNotNull(binaryEff.getUnknownSchemaNodes());
        assertNull(binaryEff.getDescription());
        assertNull(binaryEff.getReference());
    }

    @Test
    public void testBits() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-bits");
        assertNotNull(currentLeaf.getType());

        List<BitsTypeDefinition.Bit> bitsEffIter = ((BitsTypeDefinition) currentLeaf.getType()).getBits();
        BitEffectiveStatementImpl bitEff = (BitEffectiveStatementImpl) bitsEffIter.get(0);
        BitEffectiveStatementImpl bitEffSecond = (BitEffectiveStatementImpl) bitsEffIter.get(1);

        BitsTypeDefinition bitsEff = ((BitsSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        assertEquals("0", bitEff.getPosition().toString());
        assertEquals(0, bitEff.getPosition().longValue());
    }

    @Test
    public void testBoolean() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-boolean");
        assertNotNull(currentLeaf.getType());
        BooleanTypeDefinition booleanEff = (BooleanTypeDefinition) ((TypeEffectiveStatement<?>)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-decimal64");
        assertNotNull(currentLeaf.getType());
        DecimalTypeDefinition decimal64Eff = ((Decimal64SpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

        assertNull(decimal64Eff.getBaseType());
        assertNull(decimal64Eff.getUnits());
        assertNull(decimal64Eff.getDefaultValue());
        assertEquals("decimal64", decimal64Eff.getQName().getLocalName());
        assertNotNull(decimal64Eff.getUnknownSchemaNodes());

        // FIXME: The model is wrong: description/reference/status are not allowed under 'type', how come we parse it?
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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-empty");
        assertNotNull(currentLeaf.getType());
        EmptyTypeDefinition emptyEff = (EmptyTypeDefinition) ((TypeEffectiveStatement<?>)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-enum");
        assertNotNull(currentLeaf.getType());
        List<EnumTypeDefinition.EnumPair> enumEffIter = ((EnumTypeDefinition) currentLeaf.getType()).getValues();
        EnumEffectiveStatementImpl enumEff = (EnumEffectiveStatementImpl) enumEffIter.iterator().next();

        EnumTypeDefinition enumSpecEff = ((EnumSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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

        assertEquals("zero", enumEff.getQName().getLocalName());
        assertEquals("zero", enumEff.getName());
        assertNotNull(enumEff.getPath());
        assertNotNull(enumEff.getUnknownSchemaNodes());
        assertEquals("test enum", enumEff.getDescription());
        assertEquals("test enum ref", enumEff.getReference());
        assertEquals("CURRENT", enumEff.getStatus().toString());
        assertEquals("0", enumEff.getValue().toString());
    }

    @Test
    public void testIdentityRef() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-identityref");
        assertNotNull(currentLeaf.getType());
        IdentityrefTypeDefinition identityRefEff = ((IdentityRefSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-instance-identifier");
        assertNotNull(currentLeaf.getType());
        InstanceIdentifierTypeDefinition instanceIdentEff = (InstanceIdentifierTypeDefinition)
                ((TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-leafref");
        assertNotNull(currentLeaf.getType());

        LeafrefTypeDefinition leafrefEff = ((LeafrefSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int8");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> int8Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int8Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int16");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> int16Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int16Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int32");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> int32Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int32Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int64");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> int64Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int64Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint8");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> uint8Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint8Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint16");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> uint16Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint16Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint32");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> uint32Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint32Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint64");
        assertNotNull(currentLeaf.getType());
        TypeEffectiveStatement<?> uint64Eff = (TypeEffectiveStatement<?>) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint64Eff.toString());
    }

    @Test
    public void testUnion() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-union");
        assertNotNull(currentLeaf.getType());
        UnionTypeDefinition unionEff = ((UnionSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern");
        assertNotNull(currentLeaf.getType());
        LengthConstraint lengthConstraint = ((StringTypeDefinition) (currentLeaf.getType())).getLengthConstraints().get(0);
        LengthConstraint lengthConstraintThird = ((StringTypeDefinition) (currentLeaf.getType())).getLengthConstraints().get(0);
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern-second");
        assertNotNull(currentLeaf.getType());
        LengthConstraint lengthConstraintSecond = ((StringTypeDefinition) (currentLeaf.getType())).getLengthConstraints().get(0);

        assertEquals(1, lengthConstraint.getMin().intValue());
        assertEquals(255, lengthConstraint.getMax().intValue());
        assertNull(lengthConstraint.getReference());
        assertNull(lengthConstraint.getDescription());
        assertEquals("The argument is out of bounds <1, 255>", lengthConstraint.getErrorMessage());
        assertEquals("length-out-of-specified-bounds", lengthConstraint.getErrorAppTag());
        assertNotNull(lengthConstraint.toString());
        assertNotNull(lengthConstraint.hashCode());
        assertFalse(lengthConstraint.equals(null));
        assertFalse(lengthConstraint.equals("test"));
        assertFalse(lengthConstraint.equals(lengthConstraintSecond));
        assertTrue(lengthConstraint.equals(lengthConstraintThird));
    }

    @Test
    public void testPatternConstraint() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern");
        assertNotNull(currentLeaf.getType());
        PatternConstraintEffectiveImpl lengthConstraint = (PatternConstraintEffectiveImpl)
                ((StringTypeDefinition) (currentLeaf.getType())).getPatternConstraints().get(0);
        PatternConstraintEffectiveImpl lengthConstraintThird = (PatternConstraintEffectiveImpl)
                ((StringTypeDefinition) (currentLeaf.getType())).getPatternConstraints().get(0);
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern-second");
        assertNotNull(currentLeaf.getType());
        PatternConstraintEffectiveImpl lengthConstraintSecond = (PatternConstraintEffectiveImpl)
                ((StringTypeDefinition) (currentLeaf.getType())).getPatternConstraints().get(0);

        assertEquals("^[0-9a-fA-F]*$", lengthConstraint.getRegularExpression());
        assertNull(lengthConstraint.getReference());
        assertNull(lengthConstraint.getDescription());
        assertEquals("String ^[0-9a-fA-F]*$ is not valid regular expression.", lengthConstraint.getErrorMessage());
        assertEquals("invalid-regular-expression", lengthConstraint.getErrorAppTag());
        assertNotNull(lengthConstraint.toString());
        assertNotNull(lengthConstraint.hashCode());
        assertFalse(lengthConstraint.equals(null));
        assertFalse(lengthConstraint.equals("test"));
        assertFalse(lengthConstraint.equals(lengthConstraintSecond));
        assertTrue(lengthConstraint.equals(lengthConstraintThird));
    }

    @Test
    public void testString() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-string");
        assertNotNull(currentLeaf.getType());
        StringTypeDefinition stringEff = (StringTypeDefinition) ((TypeEffectiveStatement<?>)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next()).getTypeDefinition();

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
        assertEquals(0, stringEff.getLengthConstraints().size());
        assertNotNull(stringEff.getPatternConstraints());
    }
}

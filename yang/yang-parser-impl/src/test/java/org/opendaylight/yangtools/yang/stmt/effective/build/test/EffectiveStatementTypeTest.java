/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.effective.build.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BinaryEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BitsSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.BooleanEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Decimal64SpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EmptyEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.EnumSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.IdentityRefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.InstanceIdentifierSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Int8EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LeafrefSpecificationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.PatternConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt16EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt32EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt64EffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.UInt8EffectiveStatementImpl;
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

        BinaryEffectiveStatementImpl binaryEff = (BinaryEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertNull(binaryEff.getBaseType());
        assertEquals("", binaryEff.getUnits());
        assertTrue(binaryEff.getDefaultValue() instanceof List);
        assertEquals("binary", binaryEff.getQName().getLocalName());
        assertEquals(0, binaryEff.getLengthConstraints().get(0).getMin());
        assertEquals("CURRENT", binaryEff.getStatus().toString());
        assertEquals("binary", binaryEff.getPath().getPathFromRoot().iterator().next().getLocalName());
        assertNotNull(binaryEff.getUnknownSchemaNodes());
        assertNotNull(binaryEff.getDescription());
        assertNotNull(binaryEff.getReference());
    }

    @Test
    public void testBits() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-bits");
        assertNotNull(currentLeaf.getType());

        List<BitsTypeDefinition.Bit> bitsEffIter = ((BitsType) currentLeaf.getType()).getBits();
        BitEffectiveStatementImpl bitEff = (BitEffectiveStatementImpl) bitsEffIter.get(0);
        BitEffectiveStatementImpl bitEffSecond = (BitEffectiveStatementImpl) bitsEffIter.get(1);

        BitsSpecificationEffectiveStatementImpl bitsEff = (BitsSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();
        BitsSpecificationEffectiveStatementImpl bitsEffSecond = (BitsSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertNull(bitsEff.getBaseType());
        assertNotNull(bitsEff.getQName());
        assertEquals("bits", bitsEff.getQName().getLocalName());
        assertEquals("bits", bitsEff.getPath().getLastComponent().getLocalName());
        assertNotNull(bitsEff.getUnknownSchemaNodes());
        assertNotNull(bitsEff.getDescription());
        assertNotNull(bitsEff.getReference());
        assertEquals("CURRENT", bitsEff.getStatus().toString());
        assertEquals("", bitsEff.getUnits());
        assertNotNull(bitsEff.toString());
        assertNotNull(bitsEff.hashCode());
        assertFalse(bitsEff.equals(null));
        assertFalse(bitsEff.equals("test"));
        assertTrue(bitsEff.equals(bitsEffSecond));
        assertEquals(3, bitsEff.getBits().size());
        assertEquals(3, ((List<?>) bitsEff.getDefaultValue()).size());

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
        BooleanEffectiveStatementImpl booleanEff = (BooleanEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertNull(booleanEff.getBaseType());
        assertEquals("", booleanEff.getUnits());
        assertTrue(booleanEff.getDefaultValue().equals(false));
        assertEquals("boolean", booleanEff.getQName().getLocalName());
        assertNull(booleanEff.getPath().getParent().getParent());
        assertNotNull(booleanEff.getUnknownSchemaNodes());
        assertNotNull(booleanEff.getDescription());
        assertNotNull(booleanEff.getReference());
        assertEquals("CURRENT", booleanEff.getStatus().toString());
        assertNotNull(booleanEff.toString());
    }

    @Test
    public void testDecimal64() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-decimal64");
        assertNotNull(currentLeaf.getType());
        Decimal64SpecificationEffectiveStatementImpl decimal64Eff = (Decimal64SpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();
        Decimal64SpecificationEffectiveStatementImpl decimal64EffSecond = (Decimal64SpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertEquals("decimal64", decimal64Eff.getBaseType().getQName().getLocalName());
        assertEquals("", decimal64Eff.getUnits());
        assertNull(decimal64Eff.getDefaultValue());
        assertEquals("decimal64", decimal64Eff.getQName().getLocalName());
        assertNotNull(decimal64Eff.getUnknownSchemaNodes());
        assertNotNull(decimal64Eff.getDescription());
        assertNotNull(decimal64Eff.getReference());
        assertEquals("CURRENT", decimal64Eff.getStatus().toString());
        assertEquals(3, decimal64Eff.getRangeConstraints().size());
        assertNotNull(decimal64Eff.toString());
        assertNotNull(decimal64Eff.hashCode());
        assertTrue(decimal64Eff.getFractionDigits().equals(2));
        assertTrue(decimal64Eff.isExtended());
        assertFalse(decimal64Eff.equals(null));
        assertFalse(decimal64Eff.equals("test"));
        assertTrue(decimal64Eff.equals(decimal64EffSecond));
        assertEquals("decimal64", decimal64Eff.getPath().getLastComponent().getLocalName());
    }

    @Test
    public void testEmpty() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-empty");
        assertNotNull(currentLeaf.getType());
        EmptyEffectiveStatementImpl emptyEff = (EmptyEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();

        assertNull(emptyEff.getUnits());
        assertNull(emptyEff.getDefaultValue());
        assertNull(emptyEff.getBaseType());
        assertEquals("empty", emptyEff.getQName().getLocalName());
        assertNull(emptyEff.getPath().getParent().getParent());
        assertNotNull(emptyEff.getUnknownSchemaNodes());
        assertNotNull(emptyEff.getDescription());
        assertNotNull(emptyEff.getReference());
        assertEquals("CURRENT", emptyEff.getStatus().toString());
        assertNotNull(emptyEff.toString());
    }

    @Test
    public void testEnum() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-enum");
        assertNotNull(currentLeaf.getType());
        List<EnumTypeDefinition.EnumPair> enumEffIter = ((EnumerationType) currentLeaf.getType()).getValues();
        EnumEffectiveStatementImpl enumEff = (EnumEffectiveStatementImpl) enumEffIter.iterator().next();

        EnumSpecificationEffectiveStatementImpl enumSpecEff = (EnumSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();
        EnumSpecificationEffectiveStatementImpl enumSpecEffSecond = (EnumSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertEquals("enumeration", enumSpecEff.getQName().getLocalName());
        assertEquals("enumeration", enumSpecEff.getPath().getLastComponent().getLocalName());
        assertNull(enumSpecEff.getDefaultValue());
        assertEquals(3, enumSpecEff.getValues().size());
        assertNull(enumSpecEff.getBaseType());
        assertNotNull(enumSpecEff.getUnknownSchemaNodes());
        assertEquals("CURRENT", enumSpecEff.getStatus().toString());
        assertNotNull(enumSpecEff.getDescription());
        assertNotNull(enumSpecEff.getReference());
        assertEquals("", enumSpecEff.getUnits());
        assertNotNull(enumSpecEff.toString());
        assertNotNull(enumSpecEff.hashCode());
        assertFalse(enumSpecEff.equals(null));
        assertFalse(enumSpecEff.equals("test"));
        assertTrue(enumSpecEff.equals(enumSpecEffSecond));

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
        IdentityRefSpecificationEffectiveStatementImpl identityRefEff = (IdentityRefSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertTrue(identityRefEff.getDefaultValue() instanceof IdentityEffectiveStatementImpl);
        assertEquals("identityref", identityRefEff.getQName().getLocalName());
        assertEquals("identityref", identityRefEff.getPath().getLastComponent().getLocalName());
        assertNull(identityRefEff.getBaseType());
        assertNotNull(identityRefEff.getUnknownSchemaNodes());
        assertEquals("CURRENT", identityRefEff.getStatus().toString());
        assertEquals("test-identity", identityRefEff.getIdentity().getQName().getLocalName());
        assertNotNull(identityRefEff.getDescription());
        assertNotNull(identityRefEff.getReference());
        assertEquals("", identityRefEff.getUnits());
        assertNotNull(identityRefEff.toString());
    }

    @Test
    public void testInstanceIdentifier() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-instance-identifier");
        assertNotNull(currentLeaf.getType());
        InstanceIdentifierSpecificationEffectiveStatementImpl instanceIdentEff =
                (InstanceIdentifierSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        InstanceIdentifierSpecificationEffectiveStatementImpl instanceIdentEffSecond =
                (InstanceIdentifierSpecificationEffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(instanceIdentEff.toString());

        assertFalse(instanceIdentEff.requireInstance());
        assertEquals("instance-identifier", instanceIdentEff.getQName().getLocalName());
        assertEquals("instance-identifier", instanceIdentEff.getPath().getLastComponent().getLocalName());
        assertNull(instanceIdentEff.getBaseType());
        assertNull(instanceIdentEff.getDefaultValue());
        assertNull(instanceIdentEff.getPathStatement());
        assertNotNull(instanceIdentEff.getUnknownSchemaNodes());
        assertNotNull(instanceIdentEff.getDescription());
        assertNotNull(instanceIdentEff.getReference());
        assertEquals("", instanceIdentEff.getUnits());
        assertEquals("CURRENT", instanceIdentEff.getStatus().toString());
        assertNotNull(instanceIdentEff.hashCode());
        assertFalse(instanceIdentEff.equals(null));
        assertFalse(instanceIdentEff.equals("test"));
        assertTrue(instanceIdentEff.equals(instanceIdentEffSecond));
    }

    @Test
    public void testLeafref() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-leafref");
        assertNotNull(currentLeaf.getType());
        LeafrefSpecificationEffectiveStatementImpl leafrefEff = (LeafrefSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        LeafrefSpecificationEffectiveStatementImpl leafrefEffSecond = (LeafrefSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl) currentLeaf).effectiveSubstatements().iterator().next();

        assertEquals("/container-test/leaf-test", leafrefEff.getPathStatement().toString());
        assertNull(leafrefEff.getBaseType());
        assertEquals("", leafrefEff.getUnits());
        assertEquals("leafref", ((LeafrefSpecificationEffectiveStatementImpl) leafrefEff.getDefaultValue()).argument());
        assertNotNull(leafrefEff.toString());
        assertEquals("leafref", leafrefEff.getQName().getLocalName());
        assertEquals("CURRENT", leafrefEff.getStatus().toString());
        assertNotNull(leafrefEff.getUnknownSchemaNodes());
        assertEquals("leafref", leafrefEff.getPath().getLastComponent().getLocalName());
        assertNotNull(leafrefEff.getDescription());
        assertNotNull(leafrefEff.getReference());
        assertNotNull(leafrefEff.hashCode());
        assertFalse(leafrefEff.equals(null));
        assertFalse(leafrefEff.equals("test"));
        assertTrue(leafrefEff.equals(leafrefEffSecond));
    }

    @Test
    public void testIntAll() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int8");
        assertNotNull(currentLeaf.getType());
        Int8EffectiveStatementImpl int8Eff = (Int8EffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int8Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int16");
        assertNotNull(currentLeaf.getType());
        Int16EffectiveStatementImpl int16Eff = (Int16EffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int16Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int32");
        assertNotNull(currentLeaf.getType());
        Int32EffectiveStatementImpl int32Eff = (Int32EffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int32Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-int64");
        assertNotNull(currentLeaf.getType());
        Int64EffectiveStatementImpl int64Eff = (Int64EffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(int64Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint8");
        assertNotNull(currentLeaf.getType());
        UInt8EffectiveStatementImpl uint8Eff = (UInt8EffectiveStatementImpl) ((LeafEffectiveStatementImpl) currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint8Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint16");
        assertNotNull(currentLeaf.getType());
        UInt16EffectiveStatementImpl uint16Eff = (UInt16EffectiveStatementImpl) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint16Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint32");
        assertNotNull(currentLeaf.getType());
        UInt32EffectiveStatementImpl uint32Eff = (UInt32EffectiveStatementImpl) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint32Eff.toString());

        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-uint64");
        assertNotNull(currentLeaf.getType());
        UInt64EffectiveStatementImpl uint64Eff = (UInt64EffectiveStatementImpl) ((LeafEffectiveStatementImpl)
                currentLeaf)
                .effectiveSubstatements().iterator().next();
        assertNotNull(uint64Eff.toString());
    }

    @Test
    public void testUnion() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-union");
        assertNotNull(currentLeaf.getType());
        UnionSpecificationEffectiveStatementImpl unionEff = (UnionSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next();
        UnionSpecificationEffectiveStatementImpl unionEffSecond = (UnionSpecificationEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next();

        assertEquals(2, unionEff.getTypes().size());
        assertEquals("union", unionEff.getQName().getLocalName());
        assertEquals("CURRENT", unionEff.getStatus().toString());
        assertNotNull(unionEff.getUnknownSchemaNodes());
        assertNull(unionEff.getBaseType());
        assertNull(unionEff.getUnits());
        assertNull(unionEff.getDefaultValue());
        assertNotNull(unionEff.getDescription());
        assertNotNull(unionEff.getReference());
        assertNotNull(unionEff.toString());
        assertNotNull(unionEff.hashCode());
        assertFalse(unionEff.equals(null));
        assertFalse(unionEff.equals("test"));
        assertTrue(unionEff.equals(unionEffSecond));
        assertEquals("union", unionEff.getPath().getLastComponent().getLocalName());
    }

    @Test
    public void testLengthConstraint() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern");
        assertNotNull(currentLeaf.getType());
        LengthConstraintEffectiveImpl lengthConstraint = (LengthConstraintEffectiveImpl)
                ((ExtendedType) (currentLeaf.getType())).getLengthConstraints().get(0);
        LengthConstraintEffectiveImpl lengthConstraintThird = (LengthConstraintEffectiveImpl)
                ((ExtendedType) (currentLeaf.getType())).getLengthConstraints().get(0);
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern-second");
        assertNotNull(currentLeaf.getType());
        LengthConstraintEffectiveImpl lengthConstraintSecond = (LengthConstraintEffectiveImpl)
                ((ExtendedType) (currentLeaf.getType())).getLengthConstraints().get(0);

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
                ((ExtendedType) (currentLeaf.getType())).getPatternConstraints().get(0);
        PatternConstraintEffectiveImpl lengthConstraintThird = (PatternConstraintEffectiveImpl)
                ((ExtendedType) (currentLeaf.getType())).getPatternConstraints().get(0);
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern-second");
        assertNotNull(currentLeaf.getType());
        PatternConstraintEffectiveImpl lengthConstraintSecond = (PatternConstraintEffectiveImpl)
                ((ExtendedType) (currentLeaf.getType())).getPatternConstraints().get(0);

        assertEquals("^[0-9a-fA-F]*$", lengthConstraint.getRegularExpression());
        assertNotNull(lengthConstraint.getReference());
        assertNotNull(lengthConstraint.getDescription());
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
        StringEffectiveStatementImpl stringEff = (StringEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next();
        StringEffectiveStatementImpl stringEffSecond = (StringEffectiveStatementImpl)
                ((LeafEffectiveStatementImpl)currentLeaf).effectiveSubstatements().iterator().next();

        assertEquals("string", stringEff.getQName().getLocalName());
        assertEquals("CURRENT", stringEff.getStatus().toString());
        assertEquals("", stringEff.getUnits());
        assertEquals("", stringEff.getDefaultValue());
        assertNotNull(stringEff.getUnknownSchemaNodes());
        assertNull(stringEff.getBaseType());
        assertNotNull(stringEff.getDescription());
        assertNotNull(stringEff.getReference());
        assertNotNull(stringEff.toString());
        assertNotNull(stringEff.hashCode());
        assertFalse(stringEff.equals(null));
        assertFalse(stringEff.equals("test"));
        assertTrue(stringEff.equals(stringEffSecond));
        assertEquals("string", stringEff.getPath().getLastComponent().getLocalName());
        assertEquals(1, stringEff.getLengthConstraints().size());
        assertNotNull(stringEff.getPatternConstraints());
    }

    @Test
    public void testMissing() {
        currentLeaf = (LeafSchemaNode) effectiveSchemaContext.findModuleByName("types", null)
                .getDataChildByName("leaf-missing");
        assertNotNull(currentLeaf.getType());
        assertEquals(currentLeaf.getType(), TypeUtils.getYangPrimitiveTypeFromString(TypeUtils.STRING));
    }
}
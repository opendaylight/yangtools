/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Deviation.Deviate;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class YangParserTest {
    public static final String FS = File.separator;

    private final URI fooNS = URI.create("urn:opendaylight.foo");
    private final URI barNS = URI.create("urn:opendaylight.bar");
    private final URI bazNS = URI.create("urn:opendaylight.baz");
    private Date fooRev;
    private Date barRev;
    private Date bazRev;

    private Set<Module> modules;

    @Before
    public void init() throws Exception {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fooRev = simpleDateFormat.parse("2013-02-27");
        barRev = simpleDateFormat.parse("2013-07-03");
        bazRev = simpleDateFormat.parse("2013-02-27");

        modules = TestUtils.loadModules(getClass().getResource("/model").toURI());
        assertEquals(3, modules.size());
    }

    @Test
    public void testHeaders() throws ParseException {
        Module foo = TestUtils.findModule(modules, "foo");

        assertEquals("foo", foo.getName());
        assertEquals("1", foo.getYangVersion());
        assertEquals(fooNS, foo.getNamespace());
        assertEquals("foo", foo.getPrefix());

        Set<ModuleImport> imports = foo.getImports();
        assertEquals(2, imports.size());

        ModuleImport import2 = TestUtils.findImport(imports, "br");
        assertEquals("bar", import2.getModuleName());
        assertEquals(barRev, import2.getRevision());

        ModuleImport import3 = TestUtils.findImport(imports, "bz");
        assertEquals("baz", import3.getModuleName());
        assertEquals(bazRev, import3.getRevision());

        assertEquals("opendaylight", foo.getOrganization());
        assertEquals("http://www.opendaylight.org/", foo.getContact());
        Date expectedRevision = TestUtils.createDate("2013-02-27");
        assertEquals(expectedRevision, foo.getRevision());
        assertEquals(" WILL BE DEFINED LATER", foo.getReference());
    }

    @Test
    public void testParseList() {
        Module bar = TestUtils.findModule(modules, "bar");
        URI expectedNamespace = URI.create("urn:opendaylight.bar");
        String expectedPrefix = "bar";

        ContainerSchemaNode interfaces = (ContainerSchemaNode) bar.getDataChildByName("interfaces");

        ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName("ifEntry");
        // test SchemaNode args
        QName expectedQName = QName.create(expectedNamespace, barRev, "ifEntry");
        assertEquals(expectedQName, ifEntry.getQName());
        SchemaPath expectedPath = TestUtils.createPath(true, expectedNamespace, barRev, expectedPrefix, "interfaces",
                "ifEntry");
        assertEquals(expectedPath, ifEntry.getPath());
        assertNull(ifEntry.getDescription());
        assertNull(ifEntry.getReference());
        assertEquals(Status.CURRENT, ifEntry.getStatus());
        assertEquals(0, ifEntry.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(ifEntry.isAugmenting());
        assertTrue(ifEntry.isConfiguration());
        ConstraintDefinition constraints = ifEntry.getConstraints();
        assertNull(constraints.getWhenCondition());
        assertEquals(0, constraints.getMustConstraints().size());
        assertFalse(constraints.isMandatory());
        assertEquals(1, (int) constraints.getMinElements());
        assertEquals(11, (int) constraints.getMaxElements());
        // test AugmentationTarget args
        Set<AugmentationSchema> availableAugmentations = ifEntry.getAvailableAugmentations();
        assertEquals(2, availableAugmentations.size());
        // test ListSchemaNode args
        List<QName> expectedKey = new ArrayList<>();
        expectedKey.add(QName.create(expectedNamespace, barRev, "ifIndex"));
        assertEquals(expectedKey, ifEntry.getKeyDefinition());
        assertFalse(ifEntry.isUserOrdered());
        // test DataNodeContainer args
        assertEquals(0, ifEntry.getTypeDefinitions().size());
        assertEquals(4, ifEntry.getChildNodes().size());
        assertEquals(0, ifEntry.getGroupings().size());
        assertEquals(0, ifEntry.getUses().size());

        LeafSchemaNode ifIndex = (LeafSchemaNode) ifEntry.getDataChildByName("ifIndex");
        assertEquals(ifEntry.getKeyDefinition().get(0), ifIndex.getQName());
        assertTrue(ifIndex.getType() instanceof Uint32);
        LeafSchemaNode ifMtu = (LeafSchemaNode) ifEntry.getDataChildByName("ifMtu");
        assertTrue(ifMtu.getType() instanceof Int32);
    }

    @Test
    public void testTypedefRangesResolving() throws ParseException {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode int32Leaf = (LeafSchemaNode) foo.getDataChildByName("int32-leaf");

        ExtendedType leafType = (ExtendedType) int32Leaf.getType();
        QName leafTypeQName = leafType.getQName();
        assertEquals("int32-ext2", leafTypeQName.getLocalName());
        assertEquals(fooNS, leafTypeQName.getNamespace());
        assertEquals(fooRev, leafTypeQName.getRevision());
        assertNull(leafType.getUnits());
        assertNull(leafType.getDefaultValue());
        assertTrue(leafType.getLengthConstraints().isEmpty());
        assertTrue(leafType.getPatternConstraints().isEmpty());
        List<RangeConstraint> ranges = leafType.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(BigInteger.valueOf(12), range.getMin());
        assertEquals(BigInteger.valueOf(20), range.getMax());

        ExtendedType baseType = (ExtendedType) leafType.getBaseType();
        QName baseTypeQName = baseType.getQName();
        assertEquals("int32-ext2", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertEquals("mile", baseType.getUnits());
        assertEquals("11", baseType.getDefaultValue());
        assertTrue(leafType.getLengthConstraints().isEmpty());
        assertTrue(leafType.getPatternConstraints().isEmpty());
        List<RangeConstraint> baseTypeRanges = baseType.getRangeConstraints();
        assertEquals(2, baseTypeRanges.size());
        RangeConstraint baseTypeRange1 = baseTypeRanges.get(0);
        assertEquals(BigInteger.valueOf(3), baseTypeRange1.getMin());
        assertEquals(BigInteger.valueOf(9), baseTypeRange1.getMax());
        RangeConstraint baseTypeRange2 = baseTypeRanges.get(1);
        assertEquals(BigInteger.valueOf(11), baseTypeRange2.getMin());
        assertEquals(BigInteger.valueOf(20), baseTypeRange2.getMax());

        ExtendedType base = (ExtendedType) baseType.getBaseType();
        QName baseQName = base.getQName();
        assertEquals("int32-ext1", baseQName.getLocalName());
        assertEquals(barNS, baseQName.getNamespace());
        assertEquals(barRev, baseQName.getRevision());
        assertNull(base.getUnits());
        assertNull(base.getDefaultValue());
        assertTrue(leafType.getLengthConstraints().isEmpty());
        assertTrue(leafType.getPatternConstraints().isEmpty());
        List<RangeConstraint> baseRanges = base.getRangeConstraints();
        assertEquals(1, baseRanges.size());
        RangeConstraint baseRange = baseRanges.get(0);
        assertEquals(BigInteger.valueOf(2), baseRange.getMin());
        assertEquals(BigInteger.valueOf(20), baseRange.getMax());

        assertTrue(base.getBaseType() instanceof Int32);
    }

    @Test
    public void testTypedefPatternsResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode stringleaf = (LeafSchemaNode) foo.getDataChildByName("string-leaf");

        ExtendedType type = (ExtendedType) stringleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("string-ext4", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[e-z]*$", pattern.getRegularExpression());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        ExtendedType baseType1 = (ExtendedType) type.getBaseType();
        QName baseType1QName = baseType1.getQName();
        assertEquals("string-ext3", baseType1QName.getLocalName());
        assertEquals(barNS, baseType1QName.getNamespace());
        assertEquals(barRev, baseType1QName.getRevision());
        assertNull(baseType1.getUnits());
        assertNull(baseType1.getDefaultValue());
        patterns = baseType1.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^[b-u]*$", pattern.getRegularExpression());
        assertTrue(baseType1.getLengthConstraints().isEmpty());
        assertTrue(baseType1.getRangeConstraints().isEmpty());

        ExtendedType baseType2 = (ExtendedType) baseType1.getBaseType();
        QName baseType2QName = baseType2.getQName();
        assertEquals("string-ext2", baseType2QName.getLocalName());
        assertEquals(barNS, baseType2QName.getNamespace());
        assertEquals(barRev, baseType2QName.getRevision());
        assertNull(baseType2.getUnits());
        assertNull(baseType2.getDefaultValue());
        assertTrue(baseType2.getPatternConstraints().isEmpty());
        List<LengthConstraint> baseType2Lengths = baseType2.getLengthConstraints();
        assertEquals(1, baseType2Lengths.size());
        LengthConstraint length = baseType2Lengths.get(0);
        assertEquals(BigInteger.valueOf(6), length.getMin());
        assertEquals(BigInteger.TEN, length.getMax());
        assertTrue(baseType2.getRangeConstraints().isEmpty());

        ExtendedType baseType3 = (ExtendedType) baseType2.getBaseType();
        QName baseType3QName = baseType3.getQName();
        assertEquals("string-ext1", baseType3QName.getLocalName());
        assertEquals(barNS, baseType3QName.getNamespace());
        assertEquals(barRev, baseType3QName.getRevision());
        assertNull(baseType3.getUnits());
        assertNull(baseType3.getDefaultValue());
        patterns = baseType3.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^[a-k]*$", pattern.getRegularExpression());
        List<LengthConstraint> baseType3Lengths = baseType3.getLengthConstraints();
        assertEquals(1, baseType3Lengths.size());
        length = baseType3Lengths.get(0);
        assertEquals(BigInteger.valueOf(5), length.getMin());
        assertEquals(BigInteger.valueOf(11), length.getMax());
        assertTrue(baseType3.getRangeConstraints().isEmpty());

        assertTrue(baseType3.getBaseType() instanceof StringType);
    }

    @Test
    public void testTypedefInvalidPatternsResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode invalidPatternStringLeaf = (LeafSchemaNode) foo.getDataChildByName("invalid-pattern-string-leaf");
        ExtendedType type = (ExtendedType) invalidPatternStringLeaf.getType();
        QName typeQName = type.getQName();
        assertEquals("invalid-string-pattern", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode invalidDirectStringPatternDefLeaf = (LeafSchemaNode) foo.getDataChildByName("invalid-direct-string-pattern-def-leaf");
        type = (ExtendedType) invalidDirectStringPatternDefLeaf.getType();
        typeQName = type.getQName();
        assertEquals("string", typeQName.getLocalName());
        assertEquals(fooNS, typeQName.getNamespace());
        assertEquals(fooRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode multiplePatternStringLeaf = (LeafSchemaNode) foo.getDataChildByName("multiple-pattern-string-leaf");
        type = (ExtendedType) multiplePatternStringLeaf.getType();
        typeQName = type.getQName();
        assertEquals("multiple-pattern-string", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(!patterns.isEmpty());
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[e-z]*$", pattern.getRegularExpression());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        final LeafSchemaNode multiplePatternDirectStringDefLeaf = (LeafSchemaNode) foo.getDataChildByName("multiple-pattern-direct-string-def-leaf");
        type = (ExtendedType) multiplePatternDirectStringDefLeaf.getType();
        typeQName = type.getQName();
        assertEquals("string", typeQName.getLocalName());
        assertEquals(fooNS, typeQName.getNamespace());
        assertEquals(fooRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(!patterns.isEmpty());
        assertEquals(2, patterns.size());

        boolean isEZPattern = false;
        boolean isADPattern = false;
        for (final PatternConstraint patternConstraint : patterns) {
            if (patternConstraint.getRegularExpression().equals("^[e-z]*$")) {
                isEZPattern = true;
            } else if (patternConstraint.getRegularExpression().equals("^[a-d]*$")) {
                isADPattern = true;
            }
        }
        assertTrue(isEZPattern);
        assertTrue( isADPattern);
    }

    @Test
    public void testTypedefLengthsResolving() {
        Module foo = TestUtils.findModule(modules, "foo");

        LeafSchemaNode lengthLeaf = (LeafSchemaNode) foo.getDataChildByName("length-leaf");
        ExtendedType type = (ExtendedType) lengthLeaf.getType();

        QName typeQName = type.getQName();
        assertEquals("string-ext2", typeQName.getLocalName());
        assertEquals(fooNS, typeQName.getNamespace());
        assertEquals(fooRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertTrue(type.getPatternConstraints().isEmpty());
        List<LengthConstraint> typeLengths = type.getLengthConstraints();
        assertEquals(1, typeLengths.size());
        LengthConstraint length = typeLengths.get(0);
        assertEquals(BigInteger.valueOf(7), length.getMin());
        assertEquals(BigInteger.TEN, length.getMax());
        assertTrue(type.getRangeConstraints().isEmpty());

        ExtendedType baseType1 = (ExtendedType) type.getBaseType();
        QName baseType1QName = baseType1.getQName();
        assertEquals("string-ext2", baseType1QName.getLocalName());
        assertEquals(barNS, baseType1QName.getNamespace());
        assertEquals(barRev, baseType1QName.getRevision());
        assertNull(baseType1.getUnits());
        assertNull(baseType1.getDefaultValue());
        assertTrue(baseType1.getPatternConstraints().isEmpty());
        List<LengthConstraint> baseType2Lengths = baseType1.getLengthConstraints();
        assertEquals(1, baseType2Lengths.size());
        length = baseType2Lengths.get(0);
        assertEquals(BigInteger.valueOf(6), length.getMin());
        assertEquals(BigInteger.TEN, length.getMax());
        assertTrue(baseType1.getRangeConstraints().isEmpty());

        ExtendedType baseType2 = (ExtendedType) baseType1.getBaseType();
        QName baseType2QName = baseType2.getQName();
        assertEquals("string-ext1", baseType2QName.getLocalName());
        assertEquals(barNS, baseType2QName.getNamespace());
        assertEquals(barRev, baseType2QName.getRevision());
        assertNull(baseType2.getUnits());
        assertNull(baseType2.getDefaultValue());
        List<PatternConstraint> patterns = baseType2.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[a-k]*$", pattern.getRegularExpression());
        List<LengthConstraint> baseType3Lengths = baseType2.getLengthConstraints();
        assertEquals(1, baseType3Lengths.size());
        length = baseType3Lengths.get(0);
        assertEquals(BigInteger.valueOf(5), length.getMin());
        assertEquals(BigInteger.valueOf(11), length.getMax());
        assertTrue(baseType2.getRangeConstraints().isEmpty());

        assertTrue(baseType2.getBaseType() instanceof StringType);
    }

    @Test
    public void testTypedefDecimal1() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("decimal-leaf");

        ExtendedType type = (ExtendedType) testleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(fooNS, typeQName.getNamespace());
        assertEquals(fooRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertEquals(4, (int) type.getFractionDigits());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getPatternConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        ExtendedType typeBase = (ExtendedType) type.getBaseType();
        QName typeBaseQName = typeBase.getQName();
        assertEquals("my-decimal-type", typeBaseQName.getLocalName());
        assertEquals(barNS, typeBaseQName.getNamespace());
        assertEquals(barRev, typeBaseQName.getRevision());
        assertNull(typeBase.getUnits());
        assertNull(typeBase.getDefaultValue());
        assertNull(typeBase.getFractionDigits());
        assertTrue(typeBase.getLengthConstraints().isEmpty());
        assertTrue(typeBase.getPatternConstraints().isEmpty());
        assertTrue(typeBase.getRangeConstraints().isEmpty());

        Decimal64 decimal = (Decimal64) typeBase.getBaseType();
        assertEquals(6, (int) decimal.getFractionDigits());
    }

    @Test
    public void testTypedefDecimal2() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("decimal-leaf2");

        ExtendedType type = (ExtendedType) testleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertNull(type.getFractionDigits());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getPatternConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        Decimal64 baseTypeDecimal = (Decimal64) type.getBaseType();
        assertEquals(6, (int) baseTypeDecimal.getFractionDigits());
    }

    @Test
    public void testTypedefUnion() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode unionleaf = (LeafSchemaNode) foo.getDataChildByName("union-leaf");

        ExtendedType type = (ExtendedType) unionleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-union-ext", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertNull(type.getFractionDigits());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getPatternConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        ExtendedType baseType = (ExtendedType) type.getBaseType();
        QName baseTypeQName = baseType.getQName();
        assertEquals("my-union", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertNull(baseType.getUnits());
        assertNull(baseType.getDefaultValue());
        assertNull(baseType.getFractionDigits());
        assertTrue(baseType.getLengthConstraints().isEmpty());
        assertTrue(baseType.getPatternConstraints().isEmpty());
        assertTrue(baseType.getRangeConstraints().isEmpty());

        UnionType unionType = (UnionType) baseType.getBaseType();
        List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        assertEquals(2, unionTypes.size());

        ExtendedType unionType1 = (ExtendedType) unionTypes.get(0);
        QName unionType1QName = baseType.getQName();
        assertEquals("my-union", unionType1QName.getLocalName());
        assertEquals(barNS, unionType1QName.getNamespace());
        assertEquals(barRev, unionType1QName.getRevision());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());
        assertNull(unionType1.getFractionDigits());
        assertTrue(unionType1.getLengthConstraints().isEmpty());
        assertTrue(unionType1.getPatternConstraints().isEmpty());
        List<RangeConstraint> ranges = unionType1.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(BigInteger.ONE, range.getMin());
        assertEquals(BigInteger.valueOf(100), range.getMax());
        assertTrue(unionType1.getBaseType() instanceof Int16);

        assertTrue(unionTypes.get(1) instanceof Int32);
    }

    @Test
    public void testNestedUnionResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("custom-union-leaf");

        ExtendedType type = (ExtendedType) testleaf.getType();
        QName testleafTypeQName = type.getQName();
        assertEquals(bazNS, testleafTypeQName.getNamespace());
        assertEquals(bazRev, testleafTypeQName.getRevision());
        assertEquals("union1", testleafTypeQName.getLocalName());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertNull(type.getFractionDigits());
        assertTrue(type.getLengthConstraints().isEmpty());
        assertTrue(type.getPatternConstraints().isEmpty());
        assertTrue(type.getRangeConstraints().isEmpty());

        ExtendedType typeBase = (ExtendedType) type.getBaseType();
        QName typeBaseQName = typeBase.getQName();
        assertEquals(bazNS, typeBaseQName.getNamespace());
        assertEquals(bazRev, typeBaseQName.getRevision());
        assertEquals("union2", typeBaseQName.getLocalName());
        assertNull(typeBase.getUnits());
        assertNull(typeBase.getDefaultValue());
        assertNull(typeBase.getFractionDigits());
        assertTrue(typeBase.getLengthConstraints().isEmpty());
        assertTrue(typeBase.getPatternConstraints().isEmpty());
        assertTrue(typeBase.getRangeConstraints().isEmpty());

        UnionType union = (UnionType) typeBase.getBaseType();
        List<TypeDefinition<?>> unionTypes = union.getTypes();
        assertEquals(2, unionTypes.size());
        assertTrue(unionTypes.get(0) instanceof Int32);
        assertTrue(unionTypes.get(1) instanceof ExtendedType);

        ExtendedType unionType1 = (ExtendedType) unionTypes.get(1);
        QName uniontType1QName = unionType1.getQName();
        assertEquals(barNS, uniontType1QName.getNamespace());
        assertEquals(barRev, uniontType1QName.getRevision());
        assertEquals("nested-union2", uniontType1QName.getLocalName());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());
        assertNull(unionType1.getFractionDigits());
        assertTrue(unionType1.getLengthConstraints().isEmpty());
        assertTrue(unionType1.getPatternConstraints().isEmpty());
        assertTrue(unionType1.getRangeConstraints().isEmpty());

        UnionType nestedUnion = (UnionType) unionType1.getBaseType();
        List<TypeDefinition<?>> nestedUnion2Types = nestedUnion.getTypes();
        assertEquals(2, nestedUnion2Types.size());
        assertTrue(nestedUnion2Types.get(0) instanceof StringType);
        assertTrue(nestedUnion2Types.get(1) instanceof ExtendedType);

        ExtendedType myUnionExt = (ExtendedType) nestedUnion2Types.get(1);
        QName myUnionExtQName = myUnionExt.getQName();
        assertEquals(barNS, myUnionExtQName.getNamespace());
        assertEquals(barRev, myUnionExtQName.getRevision());
        assertEquals("my-union-ext", myUnionExtQName.getLocalName());
        assertNull(myUnionExt.getUnits());
        assertNull(myUnionExt.getDefaultValue());
        assertNull(myUnionExt.getFractionDigits());
        assertTrue(myUnionExt.getLengthConstraints().isEmpty());
        assertTrue(myUnionExt.getPatternConstraints().isEmpty());
        assertTrue(myUnionExt.getRangeConstraints().isEmpty());

        ExtendedType myUnion = (ExtendedType) myUnionExt.getBaseType();
        QName myUnionQName = myUnion.getQName();
        assertEquals(barNS, myUnionQName.getNamespace());
        assertEquals(barRev, myUnionQName.getRevision());
        assertEquals("my-union", myUnionQName.getLocalName());
        assertNull(myUnion.getUnits());
        assertNull(myUnion.getDefaultValue());
        assertNull(myUnion.getFractionDigits());
        assertTrue(myUnion.getLengthConstraints().isEmpty());
        assertTrue(myUnion.getPatternConstraints().isEmpty());
        assertTrue(myUnion.getRangeConstraints().isEmpty());

        UnionType myUnionBase = (UnionType) myUnion.getBaseType();
        List<TypeDefinition<?>> myUnionBaseTypes = myUnionBase.getTypes();
        assertEquals(2, myUnionBaseTypes.size());
        assertTrue(myUnionBaseTypes.get(0) instanceof ExtendedType);
        assertTrue(myUnionBaseTypes.get(1) instanceof Int32);

        ExtendedType int16Ext = (ExtendedType) myUnionBaseTypes.get(0);
        QName int16ExtQName = int16Ext.getQName();
        assertEquals(barNS, int16ExtQName.getNamespace());
        assertEquals(barRev, int16ExtQName.getRevision());
        assertEquals("int16", int16ExtQName.getLocalName());
        assertNull(int16Ext.getUnits());
        assertNull(int16Ext.getDefaultValue());
        assertNull(int16Ext.getFractionDigits());
        assertTrue(int16Ext.getLengthConstraints().isEmpty());
        assertTrue(int16Ext.getPatternConstraints().isEmpty());
        List<RangeConstraint> ranges = int16Ext.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(BigInteger.ONE, range.getMin());
        assertEquals(BigInteger.valueOf(100), range.getMax());

        assertTrue(int16Ext.getBaseType() instanceof Int16);
    }

    @Test
    public void testChoice() {
        Module foo = TestUtils.findModule(modules, "foo");
        ContainerSchemaNode transfer = (ContainerSchemaNode) foo.getDataChildByName("transfer");
        ChoiceSchemaNode how = (ChoiceSchemaNode) transfer.getDataChildByName("how");
        Set<ChoiceCaseNode> cases = how.getCases();
        assertEquals(5, cases.size());
        ChoiceCaseNode input = null;
        ChoiceCaseNode output = null;
        for (ChoiceCaseNode caseNode : cases) {
            if ("input".equals(caseNode.getQName().getLocalName())) {
                input = caseNode;
            } else if ("output".equals(caseNode.getQName().getLocalName())) {
                output = caseNode;
            }
        }
        assertNotNull(input);
        assertNotNull(input.getPath());
        assertNotNull(output);
        assertNotNull(output.getPath());
    }

    @Test
    public void testDeviation() {
        Module foo = TestUtils.findModule(modules, "foo");
        Set<Deviation> deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        Deviation dev = deviations.iterator().next();
        assertEquals("system/user ref", dev.getReference());

        List<QName> path = new ArrayList<>();
        path.add(QName.create(barNS, barRev, "interfaces"));
        path.add(QName.create(barNS, barRev, "ifEntry"));
        SchemaPath expectedPath = SchemaPath.create(path, true);

        assertEquals(expectedPath, dev.getTargetPath());
        assertEquals(Deviate.ADD, dev.getDeviate());
    }

    @Test
    public void testUnknownNode() {
        Module baz = TestUtils.findModule(modules, "baz");
        ContainerSchemaNode network = (ContainerSchemaNode) baz.getDataChildByName("network");
        List<UnknownSchemaNode> unknownNodes = network.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());
        UnknownSchemaNode unknownNode = unknownNodes.get(0);
        assertNotNull(unknownNode.getNodeType());
        assertEquals("point", unknownNode.getNodeParameter());
    }

    @Test
    public void testFeature() {
        Module baz = TestUtils.findModule(modules, "baz");
        Set<FeatureDefinition> features = baz.getFeatures();
        assertEquals(1, features.size());
    }

    @Test
    public void testExtension() {
        Module baz = TestUtils.findModule(modules, "baz");
        List<ExtensionDefinition> extensions = baz.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());
        ExtensionDefinition extension = extensions.get(0);
        assertEquals("name", extension.getArgument());
        assertEquals("Takes as argument a name string. Makes the code generator use the given name in the #define.",
                extension.getDescription());
        assertTrue(extension.isYinElement());
    }

    @Test
    public void testNotification() {
        Module baz = TestUtils.findModule(modules, "baz");
        String expectedPrefix = "c";

        Set<NotificationDefinition> notifications = baz.getNotifications();
        assertEquals(1, notifications.size());

        NotificationDefinition notification = notifications.iterator().next();
        // test SchemaNode args
        QName expectedQName = QName.create(bazNS, bazRev, "event");
        assertEquals(expectedQName, notification.getQName());
        SchemaPath expectedPath = TestUtils.createPath(true, bazNS, bazRev, expectedPrefix, "event");
        assertEquals(expectedPath, notification.getPath());
        assertNull(notification.getDescription());
        assertNull(notification.getReference());
        assertEquals(Status.CURRENT, notification.getStatus());
        assertEquals(0, notification.getUnknownSchemaNodes().size());
        // test DataNodeContainer args
        assertEquals(0, notification.getTypeDefinitions().size());
        assertEquals(3, notification.getChildNodes().size());
        assertEquals(0, notification.getGroupings().size());
        assertEquals(0, notification.getUses().size());

        LeafSchemaNode eventClass = (LeafSchemaNode) notification.getDataChildByName("event-class");
        assertTrue(eventClass.getType() instanceof StringType);
        LeafSchemaNode severity = (LeafSchemaNode) notification.getDataChildByName("severity");
        assertTrue(severity.getType() instanceof StringType);
    }

    @Test
    public void testRpc() {
        Module baz = TestUtils.findModule(modules, "baz");

        Set<RpcDefinition> rpcs = baz.getRpcs();
        assertEquals(1, rpcs.size());

        RpcDefinition rpc = rpcs.iterator().next();
        assertEquals("Retrieve all or part of a specified configuration.", rpc.getDescription());
        assertEquals("RFC 6241, Section 7.1", rpc.getReference());
    }

    @Test
    public void testTypePath() throws ParseException {
        Module bar = TestUtils.findModule(modules, "bar");
        Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // int32-ext1
        ExtendedType int32ext1 = (ExtendedType) TestUtils.findTypedef(types, "int32-ext1");
        QName int32TypedefQName = int32ext1.getQName();

        assertEquals(barNS, int32TypedefQName.getNamespace());
        assertEquals(barRev, int32TypedefQName.getRevision());
        assertEquals("int32-ext1", int32TypedefQName.getLocalName());

        SchemaPath typeSchemaPath = int32ext1.getPath();
        Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(int32TypedefQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // int32-ext1/int32
        Int32 int32 = (Int32) int32ext1.getBaseType();
        assertEquals(Int32.getInstance(), int32);
    }

    @Test
    public void testTypePath2() throws ParseException {
        Module bar = TestUtils.findModule(modules, "bar");
        Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // my-decimal-type
        ExtendedType myDecType = (ExtendedType) TestUtils.findTypedef(types, "my-decimal-type");
        QName myDecTypeQName = myDecType.getQName();

        assertEquals(barNS, myDecTypeQName.getNamespace());
        assertEquals(barRev, myDecTypeQName.getRevision());
        assertEquals("my-decimal-type", myDecTypeQName.getLocalName());

        SchemaPath typeSchemaPath = myDecType.getPath();
        Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(myDecTypeQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // my-base-int32-type/int32
        Decimal64 dec64 = (Decimal64) myDecType.getBaseType();
        QName dec64QName = dec64.getQName();

        assertEquals(URI.create("urn:ietf:params:xml:ns:yang:1"), dec64QName.getNamespace());
        assertNull(dec64QName.getRevision());
        assertEquals("decimal64", dec64QName.getLocalName());

        SchemaPath dec64SchemaPath = dec64.getPath();
        Iterable<QName> dec64Path = dec64SchemaPath.getPathFromRoot();
        Iterator<QName> dec64PathIt = dec64Path.iterator();
        assertEquals(myDecTypeQName, dec64PathIt.next());
        assertEquals(dec64QName, dec64PathIt.next());
        assertFalse(dec64PathIt.hasNext());
    }

    @Test
    public void testParseMethod1() throws Exception {
        File yangFile = new File(getClass().getResource("/parse-methods/m1.yang").toURI());
        File dependenciesDir = new File(getClass().getResource("/parse-methods").toURI());
        YangContextParser parser = new YangParserImpl();
        modules = parser.parseFile(yangFile, dependenciesDir).getModules();
        assertEquals(6, modules.size());
    }

    @Test
    public void testParseMethod2() throws Exception {
        File yangFile = new File(getClass().getResource("/parse-methods/m1.yang").toURI());
        File dependenciesDir = new File(getClass().getResource("/parse-methods/dependencies").toURI());
        YangContextParser parser = new YangParserImpl();
        modules = parser.parseFile(yangFile, dependenciesDir).getModules();
        assertEquals(6, modules.size());
    }

    @Test
    public void testSorting() throws Exception {
        // Correct order: m2, m4, m6, m8, m7, m6, m3, m1
        File yangFile = new File(getClass().getResource("/sorting-test/m1.yang").toURI());
        File dependenciesDir = new File(getClass().getResource("/sorting-test").toURI());
        YangContextParser parser = new YangParserImpl();
        modules = parser.parseFile(yangFile, dependenciesDir).getModules();
        SchemaContext ctx = new SchemaContextImpl(modules, Collections.<ModuleIdentifier, String> emptyMap());
        checkOrder(modules);
        assertSetEquals(modules, ctx.getModules());

        // ##########
        parser = new YangParserImpl();
        final File testDir = dependenciesDir;
        final String[] fileList = testDir.list();
        final List<File> testFiles = new ArrayList<>();
        if (fileList == null) {
            throw new FileNotFoundException(dependenciesDir.getAbsolutePath());
        }
        for (String fileName : fileList) {
            testFiles.add(new File(testDir, fileName));
        }
        Set<Module> newModules = parser.parseFiles(testFiles).getModules();
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
        // ##########
        newModules = parser.parseFiles(testFiles, null).getModules();
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
        // ##########
        List<InputStream> streams = new ArrayList<>();
        for (File f : testFiles) {
            streams.add(new FileInputStream(f));
        }
        newModules = parser.parseSources(BuilderUtils.filesToByteSources(testFiles)).getModules();
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
        // ##########
        streams.clear();
        for (File f : testFiles) {
            streams.add(new FileInputStream(f));
        }
        newModules = parser.parseSources(BuilderUtils.filesToByteSources(testFiles), null).getModules();
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
        // ##########
        Map<File, Module> mapped = parser.parseYangModelsMapped(testFiles);
        newModules = new LinkedHashSet<>(mapped.values());
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
        // ##########
        streams.clear();
        for (File f : testFiles) {
            streams.add(new FileInputStream(f));
        }
        Map<InputStream, Module> mappedStreams = parser.parseYangModelsFromStreamsMapped(streams);
        newModules = new LinkedHashSet<>(mappedStreams.values());
        assertSetEquals(newModules, modules);
        ctx = new SchemaContextImpl(newModules, Collections.<ModuleIdentifier, String> emptyMap());
        assertSetEquals(newModules, ctx.getModules());
    }

    private static void checkOrder(final Collection<Module> modules) {
        Iterator<Module> it = modules.iterator();
        Module m = it.next();
        assertEquals("m2", m.getName());
        m = it.next();
        assertEquals("m4", m.getName());
        m = it.next();
        assertEquals("m6", m.getName());
        m = it.next();
        assertEquals("m8", m.getName());
        m = it.next();
        assertEquals("m7", m.getName());
        m = it.next();
        assertEquals("m5", m.getName());
        m = it.next();
        assertEquals("m3", m.getName());
        m = it.next();
        assertEquals("m1", m.getName());
    }

    private static void assertSetEquals(final Set<Module> s1, final Set<Module> s2) {
        assertEquals(s1, s2);
        Iterator<Module> it = s1.iterator();
        for (Module m : s2) {
            assertEquals(m, it.next());
        }
    }

    @Test
    public void testSubmodules() throws Exception {
        Module foo = TestUtils.findModule(modules, "foo");

        DataSchemaNode id = foo.getDataChildByName("id");
        assertNotNull(id);
        DataSchemaNode subExt = foo.getDataChildByName("sub-ext");
        assertNotNull(subExt);
        DataSchemaNode subTransfer = foo.getDataChildByName("sub-transfer");
        assertNotNull(subTransfer);

        assertEquals(2, foo.getExtensionSchemaNodes().size());
        assertEquals(2, foo.getAugmentations().size());
    }

    @Test
    public void unknownStatementInSubmoduleHeaderTest() throws IOException, URISyntaxException {

        File yang = new File(getClass().getResource("/yang-grammar-test/submodule-header-extension.yang").toURI());

        try {
            YangParserImpl.getInstance().parseFile(yang, yang.getParentFile());
        } catch (YangSyntaxErrorException | YangParseException e) {
            e.printStackTrace();
            fail("YangSyntaxErrorException or YangParseException should not be thrown");
        }

    }

    @Test
    public void unknownStatementBetweenRevisionsTest() throws IOException, URISyntaxException {

        File yangModul = new File(getClass().getResource("/yang-grammar-test/revisions-extension.yang").toURI());
        File yangSubmodul = new File(getClass().getResource("/yang-grammar-test/submodule-header-extension.yang")
                .toURI());

        List<File> yangs = new ArrayList<File>();
        yangs.add(yangModul);
        yangs.add(yangSubmodul);

        try {
            YangParserImpl.getInstance().parseFiles(yangs);
        } catch (YangParseException e) {
            e.printStackTrace();
            fail("YangParseException should not be thrown");
        }
    }

    @Test
    public void unknownStatementsInStatementsTest() throws IOException, URISyntaxException {
        File yangModule1 = new File(getClass().getResource("/yang-grammar-test/stmtsep-in-statements.yang").toURI());
        File yangModule2 = new File(getClass().getResource("/yang-grammar-test/stmtsep-in-statements2.yang").toURI());
        File yangSubModule = new File(getClass().getResource("/yang-grammar-test/stmtsep-in-statements-sub.yang").toURI());

        List<File> yangs = new ArrayList<File>();
        yangs.add(yangModule1);
        yangs.add(yangModule2);
        yangs.add(yangSubModule);

        try {
            YangParserImpl.getInstance().parseFiles(yangs);
        } catch (YangParseException e) {
            e.printStackTrace();
            fail("YangParseException should not be thrown");
        }
    }
}

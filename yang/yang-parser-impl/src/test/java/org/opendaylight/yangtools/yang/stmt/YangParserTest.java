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
import static org.junit.Assert.fail;
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import com.google.common.collect.Range;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
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
        final DateFormat simpleDateFormat = SimpleDateFormatUtil.getRevisionFormat();
        fooRev = simpleDateFormat.parse("2013-02-27");
        barRev = simpleDateFormat.parse("2013-07-03");
        bazRev = simpleDateFormat.parse("2013-02-27");

        modules = TestUtils.loadModules(getClass().getResource("/model").toURI());
        assertEquals(3, modules.size());
    }

    @Test
    public void testHeaders() throws ParseException {
        final Module foo = TestUtils.findModule(modules, "foo");

        assertEquals("foo", foo.getName());
        assertEquals(YangVersion.VERSION_1.toString(), foo.getYangVersion());
        assertEquals(fooNS, foo.getNamespace());
        assertEquals("foo", foo.getPrefix());

        final Set<ModuleImport> imports = foo.getImports();
        assertEquals(2, imports.size());

        final ModuleImport import2 = TestUtils.findImport(imports, "br");
        assertEquals("bar", import2.getModuleName());
        assertEquals(barRev, import2.getRevision());

        final ModuleImport import3 = TestUtils.findImport(imports, "bz");
        assertEquals("baz", import3.getModuleName());
        assertEquals(bazRev, import3.getRevision());

        assertEquals("opendaylight", foo.getOrganization());
        assertEquals("http://www.opendaylight.org/", foo.getContact());
        final Date expectedRevision = TestUtils.createDate("2013-02-27");
        assertEquals(expectedRevision, foo.getRevision());
        assertNull(foo.getReference());
    }

    @Test
    public void testParseList() {
        final Module bar = TestUtils.findModule(modules, "bar");
        final URI expectedNamespace = URI.create("urn:opendaylight.bar");
        final String expectedPrefix = "bar";

        final ContainerSchemaNode interfaces = (ContainerSchemaNode) bar.getDataChildByName(QName.create(
            bar.getQNameModule(), "interfaces"));

        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(bar.getQNameModule(),
            "ifEntry"));
        // test SchemaNode args
        final QName expectedQName = QName.create(expectedNamespace, barRev, "ifEntry");
        assertEquals(expectedQName, ifEntry.getQName());
        final SchemaPath expectedPath = TestUtils.createPath(true, expectedNamespace, barRev, expectedPrefix,
            "interfaces", "ifEntry");
        assertEquals(expectedPath, ifEntry.getPath());
        assertNull(ifEntry.getDescription());
        assertNull(ifEntry.getReference());
        assertEquals(Status.CURRENT, ifEntry.getStatus());
        assertEquals(0, ifEntry.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(ifEntry.isAugmenting());
        assertTrue(ifEntry.isConfiguration());
        final ConstraintDefinition constraints = ifEntry.getConstraints();
        // :TODO augment to ifEntry have when condition and so in consequence
        // ifEntry should be a context node ?
        // assertNull(constraints.getWhenCondition());
        assertEquals(0, constraints.getMustConstraints().size());
        assertTrue(constraints.isMandatory());
        assertEquals(1, (int) constraints.getMinElements());
        assertEquals(11, (int) constraints.getMaxElements());
        // test AugmentationTarget args
        final Set<AugmentationSchema> availableAugmentations = ifEntry.getAvailableAugmentations();
        assertEquals(2, availableAugmentations.size());
        // test ListSchemaNode args
        final List<QName> expectedKey = new ArrayList<>();
        expectedKey.add(QName.create(expectedNamespace, barRev, "ifIndex"));
        assertEquals(expectedKey, ifEntry.getKeyDefinition());
        assertFalse(ifEntry.isUserOrdered());
        // test DataNodeContainer args
        assertEquals(0, ifEntry.getTypeDefinitions().size());
        assertEquals(4, ifEntry.getChildNodes().size());
        assertEquals(0, ifEntry.getGroupings().size());
        assertEquals(0, ifEntry.getUses().size());

        final LeafSchemaNode ifIndex = (LeafSchemaNode) ifEntry.getDataChildByName(QName.create(bar.getQNameModule(), "ifIndex"));
        assertEquals(ifEntry.getKeyDefinition().get(0), ifIndex.getQName());
        assertTrue(ifIndex.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals("minutes", ifIndex.getUnits());
        final LeafSchemaNode ifMtu = (LeafSchemaNode) ifEntry.getDataChildByName(QName.create(bar.getQNameModule(), "ifMtu"));
        assertEquals(BaseTypes.int32Type(), ifMtu.getType());
    }

    @Test
    public void testTypedefRangesResolving() throws ParseException {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode int32Leaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "int32-leaf"));

        final IntegerTypeDefinition leafType = (IntegerTypeDefinition) int32Leaf.getType();
        final QName leafTypeQName = leafType.getQName();
        assertEquals("int32-ext2", leafTypeQName.getLocalName());
        assertEquals(fooNS, leafTypeQName.getNamespace());
        assertEquals(fooRev, leafTypeQName.getRevision());
        assertEquals("mile", leafType.getUnits());
        assertEquals("11", leafType.getDefaultValue());

        final List<RangeConstraint> ranges = leafType.getRangeConstraints();
        assertEquals(1, ranges.size());
        final RangeConstraint range = ranges.get(0);
        assertEquals(12, range.getMin().intValue());
        assertEquals(20, range.getMax().intValue());

        final IntegerTypeDefinition baseType = leafType.getBaseType();
        final QName baseTypeQName = baseType.getQName();
        assertEquals("int32-ext2", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertEquals("mile", baseType.getUnits());
        assertEquals("11", baseType.getDefaultValue());

        final List<RangeConstraint> baseTypeRanges = baseType.getRangeConstraints();
        assertEquals(2, baseTypeRanges.size());
        final RangeConstraint baseTypeRange1 = baseTypeRanges.get(0);
        assertEquals(3, baseTypeRange1.getMin().intValue());
        assertEquals(9, baseTypeRange1.getMax().intValue());
        final RangeConstraint baseTypeRange2 = baseTypeRanges.get(1);
        assertEquals(11, baseTypeRange2.getMin().intValue());
        assertEquals(20, baseTypeRange2.getMax().intValue());

        final IntegerTypeDefinition base = baseType.getBaseType();
        final QName baseQName = base.getQName();
        assertEquals("int32-ext1", baseQName.getLocalName());
        assertEquals(barNS, baseQName.getNamespace());
        assertEquals(barRev, baseQName.getRevision());
        assertNull(base.getUnits());
        assertNull(base.getDefaultValue());

        final List<RangeConstraint> baseRanges = base.getRangeConstraints();
        assertEquals(1, baseRanges.size());
        final RangeConstraint baseRange = baseRanges.get(0);
        assertEquals(2, baseRange.getMin().intValue());
        assertEquals(20, baseRange.getMax().intValue());

        assertEquals(BaseTypes.int32Type(), base.getBaseType());
    }

    @Test
    public void testTypedefPatternsResolving() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode stringleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "string-leaf"));

        assertTrue(stringleaf.getType() instanceof StringTypeDefinition);
        final StringTypeDefinition type = (StringTypeDefinition) stringleaf.getType();
        final QName typeQName = type.getQName();
        assertEquals("string-ext4", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[e-z]*$", pattern.getRegularExpression());
        assertEquals(1, type.getLengthConstraints().asMapOfRanges().size());

        final StringTypeDefinition baseType1 = type.getBaseType();
        final QName baseType1QName = baseType1.getQName();
        assertEquals("string-ext3", baseType1QName.getLocalName());
        assertEquals(barNS, baseType1QName.getNamespace());
        assertEquals(barRev, baseType1QName.getRevision());
        assertNull(baseType1.getUnits());
        assertNull(baseType1.getDefaultValue());
        patterns = baseType1.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^[b-u]*$", pattern.getRegularExpression());
        assertEquals(1, baseType1.getLengthConstraints().asMapOfRanges().size());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        final QName baseType2QName = baseType2.getQName();
        assertEquals("string-ext2", baseType2QName.getLocalName());
        assertEquals(barNS, baseType2QName.getNamespace());
        assertEquals(barRev, baseType2QName.getRevision());
        assertNull(baseType2.getUnits());
        assertNull(baseType2.getDefaultValue());
        assertTrue(baseType2.getPatternConstraints().isEmpty());
        final Map<Range<Integer>, ConstraintMetaDefinition> baseType2Lengths = baseType2.getLengthConstraints()
                .asMapOfRanges();
        assertEquals(1, baseType2Lengths.size());
        Range<Integer> length = baseType2Lengths.keySet().iterator().next();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType3 = baseType2.getBaseType();
        final QName baseType3QName = baseType3.getQName();
        assertEquals("string-ext1", baseType3QName.getLocalName());
        assertEquals(barNS, baseType3QName.getNamespace());
        assertEquals(barRev, baseType3QName.getRevision());
        assertNull(baseType3.getUnits());
        assertNull(baseType3.getDefaultValue());
        patterns = baseType3.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^[a-k]*$", pattern.getRegularExpression());
        final Map<Range<Integer>, ConstraintMetaDefinition> baseType3Lengths = baseType3.getLengthConstraints()
                .asMapOfRanges();
        assertEquals(1, baseType3Lengths.size());
        length = baseType3Lengths.keySet().iterator().next();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType3.getBaseType());
    }

    @Test
    public void testTypedefInvalidPatternsResolving() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode invalidPatternStringLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "invalid-pattern-string-leaf"));
        StringTypeDefinition type = (StringTypeDefinition) invalidPatternStringLeaf.getType();
        QName typeQName = type.getQName();
        assertEquals("invalid-string-pattern", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode invalidDirectStringPatternDefLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "invalid-direct-string-pattern-def-leaf"));
        type = (StringTypeDefinition) invalidDirectStringPatternDefLeaf.getType();
        typeQName = type.getQName();
        assertEquals("string", typeQName.getLocalName());
        assertEquals(YangConstants.RFC6020_YANG_NAMESPACE, typeQName.getNamespace());
        assertNull(typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode multiplePatternStringLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "multiple-pattern-string-leaf"));
        type = (StringTypeDefinition) multiplePatternStringLeaf.getType();
        typeQName = type.getQName();
        assertEquals("multiple-pattern-string", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(!patterns.isEmpty());
        assertEquals(1, patterns.size());
        final PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[e-z]*$", pattern.getRegularExpression());
        assertEquals(1, type.getLengthConstraints().asMapOfRanges().size());

        final LeafSchemaNode multiplePatternDirectStringDefLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "multiple-pattern-direct-string-def-leaf"));
        type = (StringTypeDefinition) multiplePatternDirectStringDefLeaf.getType();
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
        assertTrue(isADPattern);
    }

    @Test
    public void testTypedefLengthsResolving() {
        final Module foo = TestUtils.findModule(modules, "foo");

        final LeafSchemaNode lengthLeaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "length-leaf"));
        final StringTypeDefinition type = (StringTypeDefinition) lengthLeaf.getType();

        final QName typeQName = type.getQName();
        assertEquals("string-ext2", typeQName.getLocalName());
        assertEquals(fooNS, typeQName.getNamespace());
        assertEquals(fooRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertTrue(type.getPatternConstraints().isEmpty());
        final Map<Range<Integer>, ConstraintMetaDefinition> typeLengths = type.getLengthConstraints().asMapOfRanges();
        assertEquals(1, typeLengths.size());
        Range<Integer> length = typeLengths.keySet().iterator().next();
        assertEquals(7, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType1 = type.getBaseType();
        final QName baseType1QName = baseType1.getQName();
        assertEquals("string-ext2", baseType1QName.getLocalName());
        assertEquals(barNS, baseType1QName.getNamespace());
        assertEquals(barRev, baseType1QName.getRevision());
        assertNull(baseType1.getUnits());
        assertNull(baseType1.getDefaultValue());
        assertTrue(baseType1.getPatternConstraints().isEmpty());
        final Map<Range<Integer>, ConstraintMetaDefinition> baseType2Lengths = baseType1.getLengthConstraints()
                .asMapOfRanges();
        assertEquals(1, baseType2Lengths.size());
        length = baseType2Lengths.keySet().iterator().next();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        final QName baseType2QName = baseType2.getQName();
        assertEquals("string-ext1", baseType2QName.getLocalName());
        assertEquals(barNS, baseType2QName.getNamespace());
        assertEquals(barRev, baseType2QName.getRevision());
        assertNull(baseType2.getUnits());
        assertNull(baseType2.getDefaultValue());
        final List<PatternConstraint> patterns = baseType2.getPatternConstraints();
        assertEquals(1, patterns.size());
        final PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[a-k]*$", pattern.getRegularExpression());
        final Map<Range<Integer>, ConstraintMetaDefinition> baseType3Lengths = baseType2.getLengthConstraints()
                .asMapOfRanges();
        assertEquals(1, baseType3Lengths.size());
        length = baseType3Lengths.keySet().iterator().next();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType2.getBaseType());
    }

    @Test
    public void testTypedefDecimal1() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "decimal-leaf"));

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        final QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertEquals(6, type.getFractionDigits().intValue());
        assertEquals(1, type.getRangeConstraints().size());

        final DecimalTypeDefinition typeBase = type.getBaseType();
        final QName typeBaseQName = typeBase.getQName();
        assertEquals("decimal64", typeBaseQName.getLocalName());
        assertEquals(barNS, typeBaseQName.getNamespace());
        assertEquals(barRev, typeBaseQName.getRevision());
        assertNull(typeBase.getUnits());
        assertNull(typeBase.getDefaultValue());
        assertEquals(6, typeBase.getFractionDigits().intValue());
        assertEquals(1, typeBase.getRangeConstraints().size());

        assertNull(typeBase.getBaseType());
    }

    @Test
    public void testTypedefDecimal2() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "decimal-leaf2"));

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        final QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertEquals(6, type.getFractionDigits().intValue());
        assertEquals(1, type.getRangeConstraints().size());

        final DecimalTypeDefinition baseTypeDecimal = type.getBaseType();
        assertEquals(6, baseTypeDecimal.getFractionDigits().intValue());
    }

    @Test
    public void testTypedefUnion() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode unionleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "union-leaf"));

        assertTrue(unionleaf.getType() instanceof UnionTypeDefinition);
        final UnionTypeDefinition type = (UnionTypeDefinition) unionleaf.getType();
        final QName typeQName = type.getQName();
        assertEquals("my-union-ext", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());

        final UnionTypeDefinition baseType = type.getBaseType();
        final QName baseTypeQName = baseType.getQName();
        assertEquals("my-union", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertNull(baseType.getUnits());
        assertNull(baseType.getDefaultValue());

        final UnionTypeDefinition unionType = baseType.getBaseType();
        final List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        assertEquals(2, unionTypes.size());

        final IntegerTypeDefinition unionType1 = (IntegerTypeDefinition) unionTypes.get(0);
        final QName unionType1QName = baseType.getQName();
        assertEquals("my-union", unionType1QName.getLocalName());
        assertEquals(barNS, unionType1QName.getNamespace());
        assertEquals(barRev, unionType1QName.getRevision());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());

        final List<RangeConstraint> ranges = unionType1.getRangeConstraints();
        assertEquals(1, ranges.size());
        final RangeConstraint range = ranges.get(0);
        assertEquals(1, range.getMin().intValue());
        assertEquals(100, range.getMax().intValue());
        assertEquals(BaseTypes.int16Type(), unionType1.getBaseType());

        assertEquals(BaseTypes.int32Type(), unionTypes.get(1));
    }

    @Test
    public void testNestedUnionResolving() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "custom-union-leaf"));

        assertTrue(testleaf.getType() instanceof UnionTypeDefinition);
        final UnionTypeDefinition type = (UnionTypeDefinition) testleaf.getType();
        final QName testleafTypeQName = type.getQName();
        assertEquals(bazNS, testleafTypeQName.getNamespace());
        assertEquals(bazRev, testleafTypeQName.getRevision());
        assertEquals("union1", testleafTypeQName.getLocalName());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());

        final UnionTypeDefinition typeBase = type.getBaseType();
        final QName typeBaseQName = typeBase.getQName();
        assertEquals(bazNS, typeBaseQName.getNamespace());
        assertEquals(bazRev, typeBaseQName.getRevision());
        assertEquals("union2", typeBaseQName.getLocalName());
        assertNull(typeBase.getUnits());
        assertNull(typeBase.getDefaultValue());

        final UnionTypeDefinition union = typeBase.getBaseType();
        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        assertEquals(2, unionTypes.size());
        assertEquals(BaseTypes.int32Type(), unionTypes.get(0));
        assertTrue(unionTypes.get(1) instanceof UnionTypeDefinition);

        final UnionTypeDefinition unionType1 = (UnionTypeDefinition) unionTypes.get(1);
        final QName uniontType1QName = unionType1.getQName();
        assertEquals(barNS, uniontType1QName.getNamespace());
        assertEquals(barRev, uniontType1QName.getRevision());
        assertEquals("nested-union2", uniontType1QName.getLocalName());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());

        final UnionTypeDefinition nestedUnion = unionType1.getBaseType();
        final List<TypeDefinition<?>> nestedUnion2Types = nestedUnion.getTypes();
        assertEquals(2, nestedUnion2Types.size());
        assertTrue(nestedUnion2Types.get(1) instanceof StringTypeDefinition);
        assertTrue(nestedUnion2Types.get(0) instanceof UnionTypeDefinition);

        final UnionTypeDefinition myUnionExt = (UnionTypeDefinition) nestedUnion2Types.get(0);
        final QName myUnionExtQName = myUnionExt.getQName();
        assertEquals(barNS, myUnionExtQName.getNamespace());
        assertEquals(barRev, myUnionExtQName.getRevision());
        assertEquals("my-union-ext", myUnionExtQName.getLocalName());
        assertNull(myUnionExt.getUnits());
        assertNull(myUnionExt.getDefaultValue());


        final UnionTypeDefinition myUnion = myUnionExt.getBaseType();
        final QName myUnionQName = myUnion.getQName();
        assertEquals(barNS, myUnionQName.getNamespace());
        assertEquals(barRev, myUnionQName.getRevision());
        assertEquals("my-union", myUnionQName.getLocalName());
        assertNull(myUnion.getUnits());
        assertNull(myUnion.getDefaultValue());

        final UnionTypeDefinition myUnionBase = myUnion.getBaseType();
        final List<TypeDefinition<?>> myUnionBaseTypes = myUnionBase.getTypes();
        assertEquals(2, myUnionBaseTypes.size());
        assertTrue(myUnionBaseTypes.get(0) instanceof IntegerTypeDefinition);
        assertEquals(BaseTypes.int32Type(), myUnionBaseTypes.get(1));

        final IntegerTypeDefinition int16Ext = (IntegerTypeDefinition) myUnionBaseTypes.get(0);
        final QName int16ExtQName = int16Ext.getQName();
        assertEquals(barNS, int16ExtQName.getNamespace());
        assertEquals(barRev, int16ExtQName.getRevision());
        assertEquals("int16", int16ExtQName.getLocalName());
        assertNull(int16Ext.getUnits());
        assertNull(int16Ext.getDefaultValue());
        final List<RangeConstraint> ranges = int16Ext.getRangeConstraints();
        assertEquals(1, ranges.size());
        final RangeConstraint range = ranges.get(0);
        assertEquals(1, range.getMin().intValue());
        assertEquals(100, range.getMax().intValue());

        assertEquals(BaseTypes.int16Type(), int16Ext.getBaseType());
    }

    @Test
    public void testChoice() {
        final Module foo = TestUtils.findModule(modules, "foo");
        final ContainerSchemaNode transfer = (ContainerSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "transfer"));
        final ChoiceSchemaNode how = (ChoiceSchemaNode) transfer.getDataChildByName(QName.create(foo.getQNameModule(), "how"));
        final Set<ChoiceCaseNode> cases = how.getCases();
        assertEquals(5, cases.size());
        ChoiceCaseNode input = null;
        ChoiceCaseNode output = null;
        for (final ChoiceCaseNode caseNode : cases) {
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
        final Module foo = TestUtils.findModule(modules, "foo");
        final Set<Deviation> deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation dev = deviations.iterator().next();
        assertEquals("system/user ref", dev.getReference());

        final List<QName> path = new ArrayList<>();
        path.add(QName.create(barNS, barRev, "interfaces"));
        path.add(QName.create(barNS, barRev, "ifEntry"));
        final SchemaPath expectedPath = SchemaPath.create(path, true);

        assertEquals(expectedPath, dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }

    @Test
    public void testUnknownNode() {
        final Module baz = TestUtils.findModule(modules, "baz");
        final ContainerSchemaNode network = (ContainerSchemaNode) baz.getDataChildByName(QName.create(baz.getQNameModule(), "network"));
        final List<UnknownSchemaNode> unknownNodes = network.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());
        final UnknownSchemaNode unknownNode = unknownNodes.get(0);
        assertNotNull(unknownNode.getNodeType());
        assertEquals("point", unknownNode.getNodeParameter());
    }

    @Test
    public void testFeature() {
        final Module baz = TestUtils.findModule(modules, "baz");
        final Set<FeatureDefinition> features = baz.getFeatures();
        assertEquals(1, features.size());
    }

    @Test
    public void testExtension() {
        final Module baz = TestUtils.findModule(modules, "baz");
        final List<ExtensionDefinition> extensions = baz.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());
        final ExtensionDefinition extension = extensions.get(0);
        assertEquals("name", extension.getArgument());
        assertEquals("Takes as argument a name string. Makes the code generator use the given name in the #define.",
                extension.getDescription());
        assertTrue(extension.isYinElement());
    }

    @Test
    public void testNotification() {
        final Module baz = TestUtils.findModule(modules, "baz");
        final String expectedPrefix = "c";

        final Set<NotificationDefinition> notifications = baz.getNotifications();
        assertEquals(1, notifications.size());

        final NotificationDefinition notification = notifications.iterator().next();
        // test SchemaNode args
        final QName expectedQName = QName.create(bazNS, bazRev, "event");
        assertEquals(expectedQName, notification.getQName());
        final SchemaPath expectedPath = TestUtils.createPath(true, bazNS, bazRev, expectedPrefix, "event");
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

        final LeafSchemaNode eventClass = (LeafSchemaNode) notification.getDataChildByName(QName.create(baz.getQNameModule(), "event-class"));
        assertTrue(eventClass.getType() instanceof StringTypeDefinition);
        final LeafSchemaNode severity = (LeafSchemaNode) notification.getDataChildByName(QName.create(baz.getQNameModule(), "severity"));
        assertTrue(severity.getType() instanceof StringTypeDefinition);
    }

    @Test
    public void testRpc() {
        final Module baz = TestUtils.findModule(modules, "baz");

        final Set<RpcDefinition> rpcs = baz.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition rpc = rpcs.iterator().next();
        assertEquals("Retrieve all or part of a specified configuration.", rpc.getDescription());
        assertEquals("RFC 6241, Section 7.1", rpc.getReference());
    }

    @Test
    public void testTypePath() throws ParseException {
        final Module bar = TestUtils.findModule(modules, "bar");
        final Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // int32-ext1
        final IntegerTypeDefinition int32ext1 = (IntegerTypeDefinition) TestUtils.findTypedef(types, "int32-ext1");
        final QName int32TypedefQName = int32ext1.getQName();

        assertEquals(barNS, int32TypedefQName.getNamespace());
        assertEquals(barRev, int32TypedefQName.getRevision());
        assertEquals("int32-ext1", int32TypedefQName.getLocalName());

        final SchemaPath typeSchemaPath = int32ext1.getPath();
        final Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        final Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(int32TypedefQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // int32-ext1/int32
        final IntegerTypeDefinition int32 = int32ext1.getBaseType();
        assertEquals(BaseTypes.int32Type(), int32);
    }

    @Test
    public void testTypePath2() throws ParseException {
        final Module bar = TestUtils.findModule(modules, "bar");
        final Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // my-decimal-type
        final DecimalTypeDefinition myDecType = (DecimalTypeDefinition) TestUtils.findTypedef(types, "my-decimal-type");
        final QName myDecTypeQName = myDecType.getQName();

        assertEquals(barNS, myDecTypeQName.getNamespace());
        assertEquals(barRev, myDecTypeQName.getRevision());
        assertEquals("my-decimal-type", myDecTypeQName.getLocalName());

        final SchemaPath typeSchemaPath = myDecType.getPath();
        final Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        final Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(myDecTypeQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // my-base-int32-type/int32
        final DecimalTypeDefinition dec64 = myDecType.getBaseType();
        final QName dec64QName = dec64.getQName();

        assertEquals(barNS, dec64QName.getNamespace());
        assertEquals(barRev, dec64QName.getRevision());
        assertEquals("decimal64", dec64QName.getLocalName());

        final SchemaPath dec64SchemaPath = dec64.getPath();
        final Iterable<QName> dec64Path = dec64SchemaPath.getPathFromRoot();
        final Iterator<QName> dec64PathIt = dec64Path.iterator();
        assertEquals(myDecTypeQName, dec64PathIt.next());
        assertEquals(dec64QName, dec64PathIt.next());
        assertFalse(dec64PathIt.hasNext());
    }

    private static void checkOrder(final Collection<Module> modules) {
        final Iterator<Module> it = modules.iterator();
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
        final Iterator<Module> it = s1.iterator();
        for (final Module m : s2) {
            assertEquals(m, it.next());
        }
    }

    @Test
    public void testSubmodules() {
        final Module foo = TestUtils.findModule(modules, "foo");

        final DataSchemaNode id = foo.getDataChildByName(QName.create(foo.getQNameModule(), "id"));
        assertNotNull(id);
        final DataSchemaNode subExt = foo.getDataChildByName(QName.create(foo.getQNameModule(), "sub-ext"));
        assertNotNull(subExt);
        final DataSchemaNode subTransfer = foo.getDataChildByName(QName.create(foo.getQNameModule(), "sub-transfer"));
        assertNotNull(subTransfer);

        assertEquals(2, foo.getExtensionSchemaNodes().size());
        assertEquals(2, foo.getAugmentations().size());
    }

    @Test
    public void unknownStatementInSubmoduleHeaderTest() throws IOException, URISyntaxException, ReactorException {
        final StatementStreamSource yang1 = sourceForResource("/yang-grammar-test/revisions-extension.yang");
        final StatementStreamSource yang2 = sourceForResource("/yang-grammar-test/submodule-header-extension.yang");

        try {
            TestUtils.parseYangSources(yang1, yang2);
        } catch (final YangParseException e) {
            e.printStackTrace();
            fail("YangParseException should not be thrown");
        }

    }

    @Test
    public void unknownStatementBetweenRevisionsTest() throws ReactorException {

        final StatementStreamSource yangModule = sourceForResource("/yang-grammar-test/revisions-extension.yang");
        final StatementStreamSource yangSubmodule = sourceForResource(
                "/yang-grammar-test/submodule-header-extension.yang");

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangModule, yangSubmodule);

        final SchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void unknownStatementsInStatementsTest() throws ReactorException {

        final StatementStreamSource yangFile1 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements.yang");
        final StatementStreamSource yangFile2 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements2.yang");
        final StatementStreamSource yangFile3 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements-sub.yang");

        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(yangFile1, yangFile2, yangFile3);
        // TODO: change test or create new module in order to respect new statement parser validations
        try {
            final SchemaContext result = reactor.buildEffective();
        } catch (final Exception e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("aaa is not a YANG statement or use of extension"));
        }
    }

}

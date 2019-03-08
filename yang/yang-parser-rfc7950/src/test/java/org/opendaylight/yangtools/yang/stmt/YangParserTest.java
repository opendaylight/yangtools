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
import com.google.common.collect.RangeSet;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
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
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

public class YangParserTest {
    private static final QNameModule FOO = QNameModule.create(URI.create("urn:opendaylight.foo"),
        Revision.of("2013-02-27"));
    private static final QNameModule BAR = QNameModule.create(URI.create("urn:opendaylight.bar"),
        Revision.of("2013-07-03"));
    private static final QNameModule BAZ = QNameModule.create(URI.create("urn:opendaylight.baz"),
        Revision.of("2013-02-27"));

    private SchemaContext context;
    private Module foo;
    private Module bar;
    private Module baz;

    @Before
    public void init() throws Exception {
        context = TestUtils.loadModules(getClass().getResource("/model").toURI());
        foo = TestUtils.findModule(context, "foo").get();
        bar = TestUtils.findModule(context, "bar").get();
        baz = TestUtils.findModule(context, "baz").get();
    }

    @Test
    public void testHeaders() throws ParseException {
        assertEquals("foo", foo.getName());
        assertEquals(YangVersion.VERSION_1, foo.getYangVersion());
        assertEquals(FOO.getNamespace(), foo.getNamespace());
        assertEquals("foo", foo.getPrefix());

        final Set<ModuleImport> imports = foo.getImports();
        assertEquals(2, imports.size());

        final ModuleImport import2 = TestUtils.findImport(imports, "br");
        assertEquals("bar", import2.getModuleName());
        assertEquals(BAR.getRevision(), import2.getRevision());

        final ModuleImport import3 = TestUtils.findImport(imports, "bz");
        assertEquals("baz", import3.getModuleName());
        assertEquals(BAZ.getRevision(), import3.getRevision());

        assertEquals(Optional.of("opendaylight"), foo.getOrganization());
        assertEquals(Optional.of("http://www.opendaylight.org/"), foo.getContact());
        assertEquals(Revision.ofNullable("2013-02-27"), foo.getRevision());
        assertFalse(foo.getReference().isPresent());
    }

    @Test
    public void testParseList() {
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) bar.getDataChildByName(QName.create(
            bar.getQNameModule(), "interfaces"));

        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(QName.create(bar.getQNameModule(),
            "ifEntry"));
        // test SchemaNode args
        assertEquals(QName.create(BAR, "ifEntry"), ifEntry.getQName());

        final SchemaPath expectedPath = TestUtils.createPath(true, BAR, "interfaces", "ifEntry");
        assertEquals(expectedPath, ifEntry.getPath());
        assertFalse(ifEntry.getDescription().isPresent());
        assertFalse(ifEntry.getReference().isPresent());
        assertEquals(Status.CURRENT, ifEntry.getStatus());
        assertEquals(0, ifEntry.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(ifEntry.isAugmenting());
        assertTrue(ifEntry.isConfiguration());
        // :TODO augment to ifEntry have when condition and so in consequence
        // ifEntry should be a context node ?
        // assertNull(constraints.getWhenCondition());
        assertEquals(0, ifEntry.getMustConstraints().size());
        ElementCountConstraint constraints = ifEntry.getElementCountConstraint().get();
        assertEquals(1, constraints.getMinElements().intValue());
        assertEquals(11, constraints.getMaxElements().intValue());
        // test AugmentationTarget args
        final Set<AugmentationSchemaNode> availableAugmentations = ifEntry.getAvailableAugmentations();
        assertEquals(2, availableAugmentations.size());
        // test ListSchemaNode args
        final List<QName> expectedKey = new ArrayList<>();
        expectedKey.add(QName.create(BAR, "ifIndex"));
        assertEquals(expectedKey, ifEntry.getKeyDefinition());
        assertFalse(ifEntry.isUserOrdered());
        // test DataNodeContainer args
        assertEquals(0, ifEntry.getTypeDefinitions().size());
        assertEquals(4, ifEntry.getChildNodes().size());
        assertEquals(0, ifEntry.getGroupings().size());
        assertEquals(0, ifEntry.getUses().size());

        final LeafSchemaNode ifIndex = (LeafSchemaNode) ifEntry.getDataChildByName(QName.create(bar.getQNameModule(),
            "ifIndex"));
        assertEquals(ifEntry.getKeyDefinition().get(0), ifIndex.getQName());
        assertTrue(ifIndex.getType() instanceof Uint32TypeDefinition);
        assertEquals(Optional.of("minutes"), ifIndex.getType().getUnits());
        final LeafSchemaNode ifMtu = (LeafSchemaNode) ifEntry.getDataChildByName(QName.create(bar.getQNameModule(),
            "ifMtu"));
        assertEquals(BaseTypes.int32Type(), ifMtu.getType());
    }

    @Test
    public void testTypedefRangesResolving() throws ParseException {
        final LeafSchemaNode int32Leaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "int32-leaf"));

        final Int32TypeDefinition leafType = (Int32TypeDefinition) int32Leaf.getType();
        assertEquals(QName.create(FOO, "int32-ext2"), leafType.getQName());
        assertEquals(Optional.of("mile"), leafType.getUnits());
        assertEquals(Optional.of("11"), leafType.getDefaultValue());

        final RangeSet<? extends Number> rangeset = leafType.getRangeConstraint().get().getAllowedRanges();
        final Set<? extends Range<? extends Number>> ranges = rangeset.asRanges();
        assertEquals(1, ranges.size());

        final Range<? extends Number> range = ranges.iterator().next();
        assertEquals(12, range.lowerEndpoint().intValue());
        assertEquals(20, range.upperEndpoint().intValue());

        final Int32TypeDefinition firstBaseType = leafType.getBaseType();
        assertEquals(QName.create(BAR, "int32-ext2"), firstBaseType.getQName());
        assertEquals(Optional.of("mile"), firstBaseType.getUnits());
        assertEquals(Optional.of("11"), firstBaseType.getDefaultValue());

        final RangeSet<? extends Number> firstRangeset = firstBaseType.getRangeConstraint().get().getAllowedRanges();
        final Set<? extends Range<? extends Number>> baseRanges = firstRangeset.asRanges();
        assertEquals(2, baseRanges.size());

        final Iterator<? extends Range<? extends Number>> it = baseRanges.iterator();
        final Range<? extends Number> baseTypeRange1 = it.next();
        assertEquals(3, baseTypeRange1.lowerEndpoint().intValue());
        assertEquals(9, baseTypeRange1.upperEndpoint().intValue());
        final Range<? extends Number> baseTypeRange2 = it.next();
        assertEquals(11, baseTypeRange2.lowerEndpoint().intValue());
        assertEquals(20, baseTypeRange2.upperEndpoint().intValue());

        final Int32TypeDefinition secondBaseType = firstBaseType.getBaseType();
        final QName baseQName = secondBaseType.getQName();
        assertEquals("int32-ext1", baseQName.getLocalName());
        assertEquals(BAR, baseQName.getModule());
        assertEquals(Optional.empty(), secondBaseType.getUnits());
        assertEquals(Optional.empty(), secondBaseType.getDefaultValue());

        final Set<? extends Range<? extends Number>> secondRanges = secondBaseType.getRangeConstraint().get()
                .getAllowedRanges().asRanges();
        assertEquals(1, secondRanges.size());
        final Range<? extends Number> secondRange = secondRanges.iterator().next();
        assertEquals(2, secondRange.lowerEndpoint().intValue());
        assertEquals(20, secondRange.upperEndpoint().intValue());

        assertEquals(BaseTypes.int32Type(), secondBaseType.getBaseType());
    }

    @Test
    public void testTypedefPatternsResolving() {
        final LeafSchemaNode stringleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "string-leaf"));

        assertTrue(stringleaf.getType() instanceof StringTypeDefinition);
        final StringTypeDefinition type = (StringTypeDefinition) stringleaf.getType();
        final QName typeQName = type.getQName();
        assertEquals("string-ext4", typeQName.getLocalName());
        assertEquals(BAR, typeQName.getModule());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^(?:[e-z]*)$", pattern.getJavaPatternString());
        assertEquals(1, type.getLengthConstraint().get().getAllowedRanges().asRanges().size());

        final StringTypeDefinition baseType1 = type.getBaseType();
        final QName baseType1QName = baseType1.getQName();
        assertEquals("string-ext3", baseType1QName.getLocalName());
        assertEquals(BAR, baseType1QName.getModule());
        assertEquals(Optional.empty(), baseType1.getUnits());
        assertEquals(Optional.empty(), baseType1.getDefaultValue());
        patterns = baseType1.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^(?:[b-u]*)$", pattern.getJavaPatternString());
        assertEquals(1, baseType1.getLengthConstraint().get().getAllowedRanges().asRanges().size());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        final QName baseType2QName = baseType2.getQName();
        assertEquals("string-ext2", baseType2QName.getLocalName());
        assertEquals(BAR, baseType2QName.getModule());
        assertEquals(Optional.empty(), baseType2.getUnits());
        assertEquals(Optional.empty(), baseType2.getDefaultValue());
        assertTrue(baseType2.getPatternConstraints().isEmpty());
        final RangeSet<Integer> baseType2Lengths = baseType2.getLengthConstraint().get().getAllowedRanges();
        assertEquals(1, baseType2Lengths.asRanges().size());
        Range<Integer> length = baseType2Lengths.span();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType3 = baseType2.getBaseType();
        assertEquals(QName.create(BAR, "string-ext1"), baseType3.getQName());
        assertEquals(Optional.empty(), baseType3.getUnits());
        assertEquals(Optional.empty(), baseType3.getDefaultValue());
        patterns = baseType3.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^(?:[a-k]*)$", pattern.getJavaPatternString());
        final RangeSet<Integer> baseType3Lengths = baseType3.getLengthConstraint().get().getAllowedRanges();
        assertEquals(1, baseType3Lengths.asRanges().size());
        length = baseType3Lengths.span();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType3.getBaseType());
    }

    @Test
    public void testTypedefInvalidPatternsResolving() {
        final LeafSchemaNode invalidPatternStringLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "invalid-pattern-string-leaf"));
        StringTypeDefinition type = (StringTypeDefinition) invalidPatternStringLeaf.getType();
        assertEquals(QName.create(BAR, "invalid-string-pattern"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode invalidDirectStringPatternDefLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "invalid-direct-string-pattern-def-leaf"));
        type = (StringTypeDefinition) invalidDirectStringPatternDefLeaf.getType();

        assertEquals(QName.create(YangConstants.RFC6020_YANG_MODULE, "string"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(patterns.isEmpty());

        final LeafSchemaNode multiplePatternStringLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "multiple-pattern-string-leaf"));
        type = (StringTypeDefinition) multiplePatternStringLeaf.getType();
        assertEquals(QName.create(BAR, "multiple-pattern-string"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(!patterns.isEmpty());
        assertEquals(1, patterns.size());
        final PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^(?:[e-z]*)$", pattern.getJavaPatternString());
        assertEquals(1, type.getLengthConstraint().get().getAllowedRanges().asRanges().size());

        final LeafSchemaNode multiplePatternDirectStringDefLeaf = (LeafSchemaNode) foo
                .getDataChildByName(QName.create(foo.getQNameModule(), "multiple-pattern-direct-string-def-leaf"));
        type = (StringTypeDefinition) multiplePatternDirectStringDefLeaf.getType();
        assertEquals(QName.create(FOO, "string"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertTrue(!patterns.isEmpty());
        assertEquals(2, patterns.size());

        boolean isEZPattern = false;
        boolean isADPattern = false;
        for (final PatternConstraint patternConstraint : patterns) {
            if (patternConstraint.getJavaPatternString().equals("^(?:[e-z]*)$")) {
                isEZPattern = true;
            } else if (patternConstraint.getJavaPatternString().equals("^(?:[a-d]*)$")) {
                isADPattern = true;
            }
        }
        assertTrue(isEZPattern);
        assertTrue(isADPattern);
    }

    @Test
    public void testTypedefLengthsResolving() {
        final LeafSchemaNode lengthLeaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "length-leaf"));
        final StringTypeDefinition type = (StringTypeDefinition) lengthLeaf.getType();

        assertEquals(QName.create(FOO, "string-ext2"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertTrue(type.getPatternConstraints().isEmpty());
        final RangeSet<Integer> typeLengths = type.getLengthConstraint().get().getAllowedRanges();
        assertEquals(1, typeLengths.asRanges().size());
        Range<Integer> length = typeLengths.span();
        assertEquals(7, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType1 = type.getBaseType();
        assertEquals(QName.create(BAR, "string-ext2"), baseType1.getQName());
        assertEquals(Optional.empty(), baseType1.getUnits());
        assertEquals(Optional.empty(), baseType1.getDefaultValue());
        assertTrue(baseType1.getPatternConstraints().isEmpty());
        final RangeSet<Integer> baseType2Lengths = baseType1.getLengthConstraint().get().getAllowedRanges();
        assertEquals(1, baseType2Lengths.asRanges().size());
        length = baseType2Lengths.span();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        assertEquals(QName.create(BAR, "string-ext1"), baseType2.getQName());
        assertEquals(Optional.empty(), baseType2.getUnits());
        assertEquals(Optional.empty(), baseType2.getDefaultValue());
        final List<PatternConstraint> patterns = baseType2.getPatternConstraints();
        assertEquals(1, patterns.size());
        final PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^(?:[a-k]*)$", pattern.getJavaPatternString());
        final RangeSet<Integer> baseType3Lengths = baseType2.getLengthConstraint().get().getAllowedRanges();
        assertEquals(1, baseType3Lengths.asRanges().size());
        length = baseType3Lengths.span();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType2.getBaseType());
    }

    @Test
    public void testTypedefDecimal1() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "decimal-leaf"));

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        assertEquals(QName.create(BAR, "my-decimal-type"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertEquals(6, type.getFractionDigits());
        assertEquals(1, type.getRangeConstraint().get().getAllowedRanges().asRanges().size());

        final DecimalTypeDefinition typeBase = type.getBaseType();
        assertEquals(QName.create(BAR, "decimal64"), typeBase.getQName());
        assertEquals(Optional.empty(), typeBase.getUnits());
        assertEquals(Optional.empty(), typeBase.getDefaultValue());
        assertEquals(6, typeBase.getFractionDigits());
        assertEquals(1, typeBase.getRangeConstraint().get().getAllowedRanges().asRanges().size());

        assertNull(typeBase.getBaseType());
    }

    @Test
    public void testTypedefDecimal2() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "decimal-leaf2"));

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        assertEquals(QName.create(BAR, "my-decimal-type"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertEquals(6, type.getFractionDigits());
        assertEquals(1, type.getRangeConstraint().get().getAllowedRanges().asRanges().size());

        final DecimalTypeDefinition baseTypeDecimal = type.getBaseType();
        assertEquals(6, baseTypeDecimal.getFractionDigits());
    }

    @Test
    public void testTypedefUnion() {
        final LeafSchemaNode unionleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "union-leaf"));

        assertTrue(unionleaf.getType() instanceof UnionTypeDefinition);
        final UnionTypeDefinition type = (UnionTypeDefinition) unionleaf.getType();
        assertEquals(QName.create(BAR, "my-union-ext"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());

        final UnionTypeDefinition baseType = type.getBaseType();
        assertEquals(QName.create(BAR, "my-union"), baseType.getQName());
        assertEquals(Optional.empty(), baseType.getUnits());
        assertEquals(Optional.empty(), baseType.getDefaultValue());

        final UnionTypeDefinition unionType = baseType.getBaseType();
        final List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        assertEquals(2, unionTypes.size());

        final Int16TypeDefinition unionType1 = (Int16TypeDefinition) unionTypes.get(0);
        assertEquals(QName.create(BAR, "my-union"), baseType.getQName());
        assertEquals(Optional.empty(), unionType1.getUnits());
        assertEquals(Optional.empty(), unionType1.getDefaultValue());

        final RangeConstraint<?> ranges = unionType1.getRangeConstraint().get();
        assertEquals(1, ranges.getAllowedRanges().asRanges().size());
        final Range<?> range = ranges.getAllowedRanges().span();
        assertEquals((short)1, range.lowerEndpoint());
        assertEquals((short)100, range.upperEndpoint());
        assertEquals(BaseTypes.int16Type(), unionType1.getBaseType());

        assertEquals(BaseTypes.int32Type(), unionTypes.get(1));
    }

    @Test
    public void testNestedUnionResolving() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
            "custom-union-leaf"));

        assertTrue(testleaf.getType() instanceof UnionTypeDefinition);
        final UnionTypeDefinition type = (UnionTypeDefinition) testleaf.getType();
        assertEquals(QName.create(BAZ, "union1"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());

        final UnionTypeDefinition typeBase = type.getBaseType();
        assertEquals(QName.create(BAZ, "union2"), typeBase.getQName());
        assertEquals(Optional.empty(), typeBase.getUnits());
        assertEquals(Optional.empty(), typeBase.getDefaultValue());

        final UnionTypeDefinition union = typeBase.getBaseType();
        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        assertEquals(2, unionTypes.size());
        assertEquals(BaseTypes.int32Type(), unionTypes.get(0));
        assertTrue(unionTypes.get(1) instanceof UnionTypeDefinition);

        final UnionTypeDefinition unionType1 = (UnionTypeDefinition) unionTypes.get(1);
        assertEquals(QName.create(BAR, "nested-union2"), unionType1.getQName());
        assertEquals(Optional.empty(), unionType1.getUnits());
        assertEquals(Optional.empty(), unionType1.getDefaultValue());

        final UnionTypeDefinition nestedUnion = unionType1.getBaseType();
        final List<TypeDefinition<?>> nestedUnion2Types = nestedUnion.getTypes();
        assertEquals(2, nestedUnion2Types.size());
        assertTrue(nestedUnion2Types.get(1) instanceof StringTypeDefinition);
        assertTrue(nestedUnion2Types.get(0) instanceof UnionTypeDefinition);

        final UnionTypeDefinition myUnionExt = (UnionTypeDefinition) nestedUnion2Types.get(0);
        assertEquals(QName.create(BAR, "my-union-ext"), myUnionExt.getQName());
        assertEquals(Optional.empty(), myUnionExt.getUnits());
        assertEquals(Optional.empty(), myUnionExt.getDefaultValue());


        final UnionTypeDefinition myUnion = myUnionExt.getBaseType();
        assertEquals(QName.create(BAR, "my-union"), myUnion.getQName());
        assertEquals(Optional.empty(), myUnion.getUnits());
        assertEquals(Optional.empty(), myUnion.getDefaultValue());

        final UnionTypeDefinition myUnionBase = myUnion.getBaseType();
        final List<TypeDefinition<?>> myUnionBaseTypes = myUnionBase.getTypes();
        assertEquals(2, myUnionBaseTypes.size());
        assertTrue(myUnionBaseTypes.get(0) instanceof Int16TypeDefinition);
        assertEquals(BaseTypes.int32Type(), myUnionBaseTypes.get(1));

        final Int16TypeDefinition int16Ext = (Int16TypeDefinition) myUnionBaseTypes.get(0);
        assertEquals(QName.create(BAR, "int16"), int16Ext.getQName());
        assertEquals(Optional.empty(), int16Ext.getUnits());
        assertEquals(Optional.empty(), int16Ext.getDefaultValue());
        final Set<? extends Range<? extends Number>> ranges = int16Ext.getRangeConstraint().get().getAllowedRanges()
                .asRanges();
        assertEquals(1, ranges.size());
        final Range<? extends Number> range = ranges.iterator().next();
        assertEquals(1, range.lowerEndpoint().intValue());
        assertEquals(100, range.upperEndpoint().intValue());

        assertEquals(BaseTypes.int16Type(), int16Ext.getBaseType());
    }

    @Test
    public void testChoice() {
        final ContainerSchemaNode transfer = (ContainerSchemaNode) foo.getDataChildByName(
            QName.create(foo.getQNameModule(), "transfer"));
        final ChoiceSchemaNode how = (ChoiceSchemaNode) transfer.getDataChildByName(
            QName.create(foo.getQNameModule(), "how"));
        final SortedMap<QName, CaseSchemaNode> cases = how.getCases();
        assertEquals(5, cases.size());
        CaseSchemaNode input = null;
        CaseSchemaNode output = null;
        for (final CaseSchemaNode caseNode : cases.values()) {
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
        final Set<Deviation> deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation dev = deviations.iterator().next();
        assertEquals(Optional.of("system/user ref"), dev.getReference());

        final SchemaPath expectedPath = SchemaPath.create(true,
            QName.create(BAR, "interfaces"),
            QName.create(BAR, "ifEntry"));

        assertEquals(expectedPath, dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }

    @Test
    public void testUnknownNode() {
        final ContainerSchemaNode network = (ContainerSchemaNode) baz.getDataChildByName(
            QName.create(baz.getQNameModule(), "network"));
        final List<UnknownSchemaNode> unknownNodes = network.getUnknownSchemaNodes();
        assertEquals(1, unknownNodes.size());
        final UnknownSchemaNode unknownNode = unknownNodes.get(0);
        assertNotNull(unknownNode.getNodeType());
        assertEquals("point", unknownNode.getNodeParameter());
    }

    @Test
    public void testFeature() {
        final Set<FeatureDefinition> features = baz.getFeatures();
        assertEquals(3, features.size());
    }

    @Test
    public void testExtension() {
        final List<ExtensionDefinition> extensions = baz.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());
        final ExtensionDefinition extension = extensions.get(0);
        assertEquals("name", extension.getArgument());
        assertEquals(
            Optional.of("Takes as argument a name string. Makes the code generator use the given name in the #define."),
                extension.getDescription());
        assertTrue(extension.isYinElement());
    }

    @Test
    public void testNotification() {
        final Set<NotificationDefinition> notifications = baz.getNotifications();
        assertEquals(1, notifications.size());

        final NotificationDefinition notification = notifications.iterator().next();
        // test SchemaNode args
        assertEquals(QName.create(BAZ, "event"), notification.getQName());
        final SchemaPath expectedPath = SchemaPath.create(true,  QName.create(BAZ, "event"));
        assertEquals(expectedPath, notification.getPath());
        assertFalse(notification.getDescription().isPresent());
        assertFalse(notification.getReference().isPresent());
        assertEquals(Status.CURRENT, notification.getStatus());
        assertEquals(0, notification.getUnknownSchemaNodes().size());
        // test DataNodeContainer args
        assertEquals(0, notification.getTypeDefinitions().size());
        assertEquals(3, notification.getChildNodes().size());
        assertEquals(0, notification.getGroupings().size());
        assertEquals(0, notification.getUses().size());

        final LeafSchemaNode eventClass = (LeafSchemaNode) notification.getDataChildByName(
            QName.create(baz.getQNameModule(), "event-class"));
        assertTrue(eventClass.getType() instanceof StringTypeDefinition);
        final LeafSchemaNode severity = (LeafSchemaNode) notification.getDataChildByName(
            QName.create(baz.getQNameModule(), "severity"));
        assertTrue(severity.getType() instanceof StringTypeDefinition);
    }

    @Test
    public void testRpc() {
        final Set<RpcDefinition> rpcs = baz.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition rpc = rpcs.iterator().next();
        assertEquals(Optional.of("Retrieve all or part of a specified configuration."), rpc.getDescription());
        assertEquals(Optional.of("RFC 6241, Section 7.1"), rpc.getReference());
    }

    @Test
    public void testTypePath() throws ParseException {
        final Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // int32-ext1
        final Int32TypeDefinition int32ext1 = (Int32TypeDefinition) TestUtils.findTypedef(types, "int32-ext1");
        final QName int32TypedefQName = QName.create(BAR, "int32-ext1");
        assertEquals(int32TypedefQName, int32ext1.getQName());

        final SchemaPath typeSchemaPath = int32ext1.getPath();
        final Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        final Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(int32TypedefQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // int32-ext1/int32
        final Int32TypeDefinition int32 = int32ext1.getBaseType();
        assertEquals(BaseTypes.int32Type(), int32);
    }

    @Test
    public void testTypePath2() throws ParseException {
        final Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // my-decimal-type
        final DecimalTypeDefinition myDecType = (DecimalTypeDefinition) TestUtils.findTypedef(types, "my-decimal-type");
        final QName myDecTypeQName = myDecType.getQName();

        assertEquals(BAR, myDecTypeQName.getModule());
        assertEquals("my-decimal-type", myDecTypeQName.getLocalName());

        final SchemaPath typeSchemaPath = myDecType.getPath();
        final Iterable<QName> typePath = typeSchemaPath.getPathFromRoot();
        final Iterator<QName> typePathIt = typePath.iterator();
        assertEquals(myDecTypeQName, typePathIt.next());
        assertFalse(typePathIt.hasNext());

        // my-base-int32-type/int32
        final DecimalTypeDefinition dec64 = myDecType.getBaseType();
        final QName dec64QName = dec64.getQName();

        assertEquals(BAR, dec64QName.getModule());
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
        Module module = it.next();
        assertEquals("m2", module.getName());
        module = it.next();
        assertEquals("m4", module.getName());
        module = it.next();
        assertEquals("m6", module.getName());
        module = it.next();
        assertEquals("m8", module.getName());
        module = it.next();
        assertEquals("m7", module.getName());
        module = it.next();
        assertEquals("m5", module.getName());
        module = it.next();
        assertEquals("m3", module.getName());
        module = it.next();
        assertEquals("m1", module.getName());
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
        TestUtils.parseYangSources(yang1, yang2);
    }

    @Test
    public void unknownStatementBetweenRevisionsTest() throws ReactorException {
        final SchemaContext result = RFC7950Reactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/yang-grammar-test/revisions-extension.yang"))
                .addSource(sourceForResource("/yang-grammar-test/submodule-header-extension.yang"))
                .buildEffective();
        assertNotNull(result);
    }

    @Test
    public void unknownStatementsInStatementsTest() {

        final StatementStreamSource yangFile1 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements.yang");
        final StatementStreamSource yangFile2 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements2.yang");
        final StatementStreamSource yangFile3 = sourceForResource(
                "/yang-grammar-test/stmtsep-in-statements-sub.yang");

        final BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild()
                .addSources(yangFile1, yangFile2, yangFile3);
        // TODO: change test or create new module in order to respect new statement parser validations
        try {
            final SchemaContext result = reactor.buildEffective();
        } catch (final ReactorException e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("aaa is not a YANG statement or use of extension"));
        }
    }

}

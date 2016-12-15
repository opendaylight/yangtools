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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
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
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
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
        assertNull(foo.getReference());
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
        // :TODO augment to ifEntry have when condition and so in consequence
        // ifEntry should be a context node ?
        // assertNull(constraints.getWhenCondition());
        assertEquals(0, constraints.getMustConstraints().size());
        assertTrue(constraints.isMandatory());
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
        assertTrue(ifIndex.getType() instanceof UnsignedIntegerTypeDefinition);
        assertEquals("minutes", ifIndex.getUnits());
        LeafSchemaNode ifMtu = (LeafSchemaNode) ifEntry.getDataChildByName("ifMtu");
        assertEquals(BaseTypes.int32Type(), ifMtu.getType());
    }

    @Test
    public void testTypedefRangesResolving() throws ParseException {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode int32Leaf = (LeafSchemaNode) foo.getDataChildByName("int32-leaf");

        IntegerTypeDefinition leafType = (IntegerTypeDefinition) int32Leaf.getType();
        QName leafTypeQName = leafType.getQName();
        assertEquals("int32-ext2", leafTypeQName.getLocalName());
        assertEquals(fooNS, leafTypeQName.getNamespace());
        assertEquals(fooRev, leafTypeQName.getRevision());
        assertEquals("mile", leafType.getUnits());
        assertEquals("11", leafType.getDefaultValue());

        List<RangeConstraint> ranges = leafType.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(12, range.getMin().intValue());
        assertEquals(20, range.getMax().intValue());

        IntegerTypeDefinition baseType = leafType.getBaseType();
        QName baseTypeQName = baseType.getQName();
        assertEquals("int32-ext2", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertEquals("mile", baseType.getUnits());
        assertEquals("11", baseType.getDefaultValue());

        List<RangeConstraint> baseTypeRanges = baseType.getRangeConstraints();
        assertEquals(2, baseTypeRanges.size());
        RangeConstraint baseTypeRange1 = baseTypeRanges.get(0);
        assertEquals(3, baseTypeRange1.getMin().intValue());
        assertEquals(9, baseTypeRange1.getMax().intValue());
        RangeConstraint baseTypeRange2 = baseTypeRanges.get(1);
        assertEquals(11, baseTypeRange2.getMin().intValue());
        assertEquals(20, baseTypeRange2.getMax().intValue());

        IntegerTypeDefinition base = baseType.getBaseType();
        QName baseQName = base.getQName();
        assertEquals("int32-ext1", baseQName.getLocalName());
        assertEquals(barNS, baseQName.getNamespace());
        assertEquals(barRev, baseQName.getRevision());
        assertNull(base.getUnits());
        assertNull(base.getDefaultValue());

        List<RangeConstraint> baseRanges = base.getRangeConstraints();
        assertEquals(1, baseRanges.size());
        RangeConstraint baseRange = baseRanges.get(0);
        assertEquals(2, baseRange.getMin().intValue());
        assertEquals(20, baseRange.getMax().intValue());

        assertEquals(BaseTypes.int32Type(), base.getBaseType());
    }

    @Test
    public void testTypedefPatternsResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode stringleaf = (LeafSchemaNode) foo.getDataChildByName("string-leaf");

        assertTrue(stringleaf.getType() instanceof StringTypeDefinition);
        StringTypeDefinition type = (StringTypeDefinition) stringleaf.getType();
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
        assertEquals(1, type.getLengthConstraints().size());

        StringTypeDefinition baseType1 = type.getBaseType();
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
        assertEquals(1, baseType1.getLengthConstraints().size());

        StringTypeDefinition baseType2 = baseType1.getBaseType();
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
        assertEquals(6, length.getMin().intValue());
        assertEquals(10, length.getMax().intValue());

        StringTypeDefinition baseType3 = baseType2.getBaseType();
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
        assertEquals(5, length.getMin().intValue());
        assertEquals(11, length.getMax().intValue());

        assertEquals(BaseTypes.stringType(), baseType3.getBaseType());
    }

    @Test
    public void testTypedefInvalidPatternsResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        final LeafSchemaNode invalidPatternStringLeaf = (LeafSchemaNode) foo
                .getDataChildByName("invalid-pattern-string-leaf");
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
                .getDataChildByName("invalid-direct-string-pattern-def-leaf");
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
                .getDataChildByName("multiple-pattern-string-leaf");
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
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^[e-z]*$", pattern.getRegularExpression());
        assertEquals(1, type.getLengthConstraints().size());

        final LeafSchemaNode multiplePatternDirectStringDefLeaf = (LeafSchemaNode) foo
                .getDataChildByName("multiple-pattern-direct-string-def-leaf");
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
        Module foo = TestUtils.findModule(modules, "foo");

        LeafSchemaNode lengthLeaf = (LeafSchemaNode) foo.getDataChildByName("length-leaf");
        StringTypeDefinition type = (StringTypeDefinition) lengthLeaf.getType();

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
        assertEquals(7, length.getMin().intValue());
        assertEquals(10, length.getMax().intValue());

        StringTypeDefinition baseType1 = type.getBaseType();
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
        assertEquals(6, length.getMin().intValue());
        assertEquals(10, length.getMax().intValue());

        StringTypeDefinition baseType2 = baseType1.getBaseType();
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
        assertEquals(5, length.getMin().intValue());
        assertEquals(11, length.getMax().intValue());

        assertEquals(BaseTypes.stringType(), baseType2.getBaseType());
    }

    @Test
    public void testTypedefDecimal1() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("decimal-leaf");

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertEquals(6, type.getFractionDigits().intValue());
        assertEquals(1, type.getRangeConstraints().size());

        DecimalTypeDefinition typeBase = type.getBaseType();
        QName typeBaseQName = typeBase.getQName();
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
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("decimal-leaf2");

        assertTrue(testleaf.getType() instanceof DecimalTypeDefinition);
        DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-decimal-type", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());
        assertEquals(6, type.getFractionDigits().intValue());
        assertEquals(1, type.getRangeConstraints().size());

        DecimalTypeDefinition baseTypeDecimal = type.getBaseType();
        assertEquals(6, baseTypeDecimal.getFractionDigits().intValue());
    }

    @Test
    public void testTypedefUnion() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode unionleaf = (LeafSchemaNode) foo.getDataChildByName("union-leaf");

        assertTrue(unionleaf.getType() instanceof UnionTypeDefinition);
        UnionTypeDefinition type = (UnionTypeDefinition) unionleaf.getType();
        QName typeQName = type.getQName();
        assertEquals("my-union-ext", typeQName.getLocalName());
        assertEquals(barNS, typeQName.getNamespace());
        assertEquals(barRev, typeQName.getRevision());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());

        UnionTypeDefinition baseType = type.getBaseType();
        QName baseTypeQName = baseType.getQName();
        assertEquals("my-union", baseTypeQName.getLocalName());
        assertEquals(barNS, baseTypeQName.getNamespace());
        assertEquals(barRev, baseTypeQName.getRevision());
        assertNull(baseType.getUnits());
        assertNull(baseType.getDefaultValue());

        UnionTypeDefinition unionType = baseType.getBaseType();
        List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        assertEquals(2, unionTypes.size());

        IntegerTypeDefinition unionType1 = (IntegerTypeDefinition) unionTypes.get(0);
        QName unionType1QName = baseType.getQName();
        assertEquals("my-union", unionType1QName.getLocalName());
        assertEquals(barNS, unionType1QName.getNamespace());
        assertEquals(barRev, unionType1QName.getRevision());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());

        List<RangeConstraint> ranges = unionType1.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(1, range.getMin().intValue());
        assertEquals(100, range.getMax().intValue());
        assertEquals(BaseTypes.int16Type(), unionType1.getBaseType());

        assertEquals(BaseTypes.int32Type(), unionTypes.get(1));
    }

    @Test
    public void testNestedUnionResolving() {
        Module foo = TestUtils.findModule(modules, "foo");
        LeafSchemaNode testleaf = (LeafSchemaNode) foo.getDataChildByName("custom-union-leaf");

        assertTrue(testleaf.getType() instanceof UnionTypeDefinition);
        UnionTypeDefinition type = (UnionTypeDefinition) testleaf.getType();
        QName testleafTypeQName = type.getQName();
        assertEquals(bazNS, testleafTypeQName.getNamespace());
        assertEquals(bazRev, testleafTypeQName.getRevision());
        assertEquals("union1", testleafTypeQName.getLocalName());
        assertNull(type.getUnits());
        assertNull(type.getDefaultValue());

        UnionTypeDefinition typeBase = type.getBaseType();
        QName typeBaseQName = typeBase.getQName();
        assertEquals(bazNS, typeBaseQName.getNamespace());
        assertEquals(bazRev, typeBaseQName.getRevision());
        assertEquals("union2", typeBaseQName.getLocalName());
        assertNull(typeBase.getUnits());
        assertNull(typeBase.getDefaultValue());

        UnionTypeDefinition union = typeBase.getBaseType();
        List<TypeDefinition<?>> unionTypes = union.getTypes();
        assertEquals(2, unionTypes.size());
        assertEquals(BaseTypes.int32Type(), unionTypes.get(0));
        assertTrue(unionTypes.get(1) instanceof UnionTypeDefinition);

        UnionTypeDefinition unionType1 = (UnionTypeDefinition) unionTypes.get(1);
        QName uniontType1QName = unionType1.getQName();
        assertEquals(barNS, uniontType1QName.getNamespace());
        assertEquals(barRev, uniontType1QName.getRevision());
        assertEquals("nested-union2", uniontType1QName.getLocalName());
        assertNull(unionType1.getUnits());
        assertNull(unionType1.getDefaultValue());

        UnionTypeDefinition nestedUnion = unionType1.getBaseType();
        List<TypeDefinition<?>> nestedUnion2Types = nestedUnion.getTypes();
        assertEquals(2, nestedUnion2Types.size());
        assertTrue(nestedUnion2Types.get(1) instanceof StringTypeDefinition);
        assertTrue(nestedUnion2Types.get(0) instanceof UnionTypeDefinition);

        UnionTypeDefinition myUnionExt = (UnionTypeDefinition) nestedUnion2Types.get(0);
        QName myUnionExtQName = myUnionExt.getQName();
        assertEquals(barNS, myUnionExtQName.getNamespace());
        assertEquals(barRev, myUnionExtQName.getRevision());
        assertEquals("my-union-ext", myUnionExtQName.getLocalName());
        assertNull(myUnionExt.getUnits());
        assertNull(myUnionExt.getDefaultValue());


        UnionTypeDefinition myUnion = myUnionExt.getBaseType();
        QName myUnionQName = myUnion.getQName();
        assertEquals(barNS, myUnionQName.getNamespace());
        assertEquals(barRev, myUnionQName.getRevision());
        assertEquals("my-union", myUnionQName.getLocalName());
        assertNull(myUnion.getUnits());
        assertNull(myUnion.getDefaultValue());

        UnionTypeDefinition myUnionBase = myUnion.getBaseType();
        List<TypeDefinition<?>> myUnionBaseTypes = myUnionBase.getTypes();
        assertEquals(2, myUnionBaseTypes.size());
        assertTrue(myUnionBaseTypes.get(0) instanceof IntegerTypeDefinition);
        assertEquals(BaseTypes.int32Type(), myUnionBaseTypes.get(1));

        IntegerTypeDefinition int16Ext = (IntegerTypeDefinition) myUnionBaseTypes.get(0);
        QName int16ExtQName = int16Ext.getQName();
        assertEquals(barNS, int16ExtQName.getNamespace());
        assertEquals(barRev, int16ExtQName.getRevision());
        assertEquals("int16", int16ExtQName.getLocalName());
        assertNull(int16Ext.getUnits());
        assertNull(int16Ext.getDefaultValue());
        List<RangeConstraint> ranges = int16Ext.getRangeConstraints();
        assertEquals(1, ranges.size());
        RangeConstraint range = ranges.get(0);
        assertEquals(1, range.getMin().intValue());
        assertEquals(100, range.getMax().intValue());

        assertEquals(BaseTypes.int16Type(), int16Ext.getBaseType());
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
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
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
        assertTrue(eventClass.getType() instanceof StringTypeDefinition);
        LeafSchemaNode severity = (LeafSchemaNode) notification.getDataChildByName("severity");
        assertTrue(severity.getType() instanceof StringTypeDefinition);
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
        IntegerTypeDefinition int32ext1 = (IntegerTypeDefinition) TestUtils.findTypedef(types, "int32-ext1");
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
        IntegerTypeDefinition int32 = int32ext1.getBaseType();
        assertEquals(BaseTypes.int32Type(), int32);
    }

    @Test
    public void testTypePath2() throws ParseException {
        Module bar = TestUtils.findModule(modules, "bar");
        Set<TypeDefinition<?>> types = bar.getTypeDefinitions();

        // my-decimal-type
        DecimalTypeDefinition myDecType = (DecimalTypeDefinition) TestUtils.findTypedef(types, "my-decimal-type");
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
        DecimalTypeDefinition dec64 = myDecType.getBaseType();
        QName dec64QName = dec64.getQName();

        assertEquals(barNS, dec64QName.getNamespace());
        assertEquals(barRev, dec64QName.getRevision());
        assertEquals("decimal64", dec64QName.getLocalName());

        SchemaPath dec64SchemaPath = dec64.getPath();
        Iterable<QName> dec64Path = dec64SchemaPath.getPathFromRoot();
        Iterator<QName> dec64PathIt = dec64Path.iterator();
        assertEquals(myDecTypeQName, dec64PathIt.next());
        assertEquals(dec64QName, dec64PathIt.next());
        assertFalse(dec64PathIt.hasNext());
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
    public void testSubmodules() {
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
    public void unknownStatementInSubmoduleHeaderTest() throws IOException, URISyntaxException, ReactorException {
        StatementStreamSource yang1 = new YangStatementSourceImpl("/yang-grammar-test/revisions-extension.yang", false);
        StatementStreamSource yang2 = new YangStatementSourceImpl("/yang-grammar-test/submodule-header-extension.yang",
                false);

        try {
            TestUtils.parseYangSources(yang1, yang2);
        } catch (YangParseException e) {
            e.printStackTrace();
            fail("YangParseException should not be thrown");
        }

    }

    @Test
    public void unknownStatementBetweenRevisionsTest() throws ReactorException {

        final YangStatementSourceImpl yangModule = new YangStatementSourceImpl(
                "/yang-grammar-test/revisions-extension.yang", false);
        final YangStatementSourceImpl yangSubmodule = new YangStatementSourceImpl(
                "/yang-grammar-test/submodule-header-extension.yang", false);

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, yangModule, yangSubmodule);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);
    }

    @Test
    public void unknownStatementsInStatementsTest() throws ReactorException {

        final YangStatementSourceImpl yangFile1 = new YangStatementSourceImpl(
                "/yang-grammar-test/stmtsep-in-statements.yang", false);
        final YangStatementSourceImpl yangFile2 = new YangStatementSourceImpl(
                "/yang-grammar-test/stmtsep-in-statements2.yang", false);
        final YangStatementSourceImpl yangFile3 = new YangStatementSourceImpl(
                "/yang-grammar-test/stmtsep-in-statements-sub.yang", false);

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        addSources(reactor, yangFile1, yangFile2, yangFile3);
        // TODO: change test or create new module in order to respect new statement parser validations
        try {
            final EffectiveSchemaContext result = reactor.buildEffective();
        } catch (final Exception e) {
            assertEquals(SomeModifiersUnresolvedException.class, e.getClass());
            assertTrue(e.getCause() instanceof SourceException);
            assertTrue(e.getCause().getMessage().startsWith("aaa is not a YANG statement or use of extension"));
        }
    }

    private static void addSources(final CrossSourceStatementReactor.BuildAction reactor, final YangStatementSourceImpl... sources) {
        for (YangStatementSourceImpl source : sources) {
            reactor.addSource(source);
        }
    }
}

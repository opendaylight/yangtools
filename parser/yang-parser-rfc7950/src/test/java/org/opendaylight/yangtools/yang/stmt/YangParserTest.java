/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
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
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

class YangParserTest extends AbstractModelTest {
    @Test
    void testHeaders() {
        assertEquals("foo", FOO.getName());
        assertEquals(YangVersion.VERSION_1, FOO.getYangVersion());
        assertEquals(XMLNamespace.of("urn:opendaylight.foo"), FOO.getNamespace());
        assertEquals("foo", FOO.getPrefix());

        final Collection<? extends ModuleImport> imports = FOO.getImports();
        assertEquals(2, imports.size());

        final ModuleImport import2 = TestUtils.findImport(imports, "br");
        assertEquals(Unqualified.of("bar"), import2.getModuleName());
        assertEquals(Revision.ofNullable("2013-07-03"), import2.getRevision());

        final ModuleImport import3 = TestUtils.findImport(imports, "bz");
        assertEquals(Unqualified.of("baz"), import3.getModuleName());
        assertEquals(Revision.ofNullable("2013-02-27"), import3.getRevision());

        assertEquals(Optional.of("opendaylight"), FOO.getOrganization());
        assertEquals(Optional.of("http://www.opendaylight.org/"), FOO.getContact());
        assertEquals(Revision.ofNullable("2013-02-27"), FOO.getRevision());
        assertFalse(FOO.getReference().isPresent());
    }

    @Test
    void testParseList() {
        final ContainerSchemaNode interfaces = (ContainerSchemaNode) BAR.getDataChildByName(barQName("interfaces"));
        final ListSchemaNode ifEntry = (ListSchemaNode) interfaces.getDataChildByName(barQName("ifEntry"));
        // test SchemaNode args
        assertEquals(barQName("ifEntry"), ifEntry.getQName());

        assertFalse(ifEntry.getDescription().isPresent());
        assertFalse(ifEntry.getReference().isPresent());
        assertEquals(Status.CURRENT, ifEntry.getStatus());
        assertEquals(0, ifEntry.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(ifEntry.isAugmenting());
        assertEquals(Optional.of(Boolean.TRUE), ifEntry.effectiveConfig());
        // :TODO augment to ifEntry have when condition and so in consequence
        // ifEntry should be a context node ?
        // assertNull(constraints.getWhenCondition());
        assertEquals(0, ifEntry.getMustConstraints().size());
        ElementCountConstraint constraints = ifEntry.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 1, constraints.getMinElements());
        assertEquals((Object) 11, constraints.getMaxElements());
        // test AugmentationTarget args
        final Collection<? extends AugmentationSchemaNode> availableAugmentations = ifEntry.getAvailableAugmentations();
        assertEquals(2, availableAugmentations.size());
        // test ListSchemaNode args
        final List<QName> expectedKey = new ArrayList<>();
        expectedKey.add(barQName("ifIndex"));
        assertEquals(expectedKey, ifEntry.getKeyDefinition());
        assertFalse(ifEntry.isUserOrdered());
        // test DataNodeContainer args
        assertEquals(0, ifEntry.getTypeDefinitions().size());
        assertEquals(4, ifEntry.getChildNodes().size());
        assertEquals(0, ifEntry.getGroupings().size());
        assertEquals(0, ifEntry.getUses().size());

        final LeafSchemaNode ifIndex = (LeafSchemaNode) ifEntry.getDataChildByName(barQName("ifIndex"));
        assertEquals(ifEntry.getKeyDefinition().get(0), ifIndex.getQName());
        assertInstanceOf(Uint32TypeDefinition.class, ifIndex.getType());
        assertEquals(Optional.of("minutes"), ifIndex.getType().getUnits());
        final LeafSchemaNode ifMtu = (LeafSchemaNode) ifEntry.getDataChildByName(barQName("ifMtu"));
        assertEquals(BaseTypes.int32Type(), ifMtu.getType());
    }

    @Test
    void testTypedefRangesResolving() {
        final LeafSchemaNode int32Leaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("int32-leaf"));

        final Int32TypeDefinition leafType = (Int32TypeDefinition) int32Leaf.getType();
        assertEquals(fooQName("int32-ext2"), leafType.getQName());
        assertEquals(Optional.of("mile"), leafType.getUnits());
        assertEquals(Optional.of("11"), leafType.getDefaultValue());

        final var ranges = leafType.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(1, ranges.size());

        final var range = ranges.iterator().next();
        assertEquals(12, range.lowerEndpoint().intValue());
        assertEquals(20, range.upperEndpoint().intValue());

        final Int32TypeDefinition firstBaseType = leafType.getBaseType();
        assertEquals(barQName("int32-ext2"), firstBaseType.getQName());
        assertEquals(Optional.of("mile"), firstBaseType.getUnits());
        assertEquals(Optional.of("11"), firstBaseType.getDefaultValue());

        final var baseRanges = firstBaseType.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(2, baseRanges.size());

        final var it = baseRanges.iterator();
        final var baseTypeRange1 = it.next();
        assertEquals(3, baseTypeRange1.lowerEndpoint().intValue());
        assertEquals(9, baseTypeRange1.upperEndpoint().intValue());
        final var baseTypeRange2 = it.next();
        assertEquals(11, baseTypeRange2.lowerEndpoint().intValue());
        assertEquals(20, baseTypeRange2.upperEndpoint().intValue());

        final Int32TypeDefinition secondBaseType = firstBaseType.getBaseType();
        assertEquals(barQName("int32-ext1"), secondBaseType.getQName());
        assertEquals(Optional.empty(), secondBaseType.getUnits());
        assertEquals(Optional.empty(), secondBaseType.getDefaultValue());

        final var secondRanges = secondBaseType.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(1, secondRanges.size());
        final var secondRange = secondRanges.iterator().next();
        assertEquals(2, secondRange.lowerEndpoint().intValue());
        assertEquals(20, secondRange.upperEndpoint().intValue());

        assertEquals(BaseTypes.int32Type(), secondBaseType.getBaseType());
    }

    @Test
    void testTypedefPatternsResolving() {
        final LeafSchemaNode stringleaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("string-leaf"));

        assertInstanceOf(StringTypeDefinition.class, stringleaf.getType());
        final StringTypeDefinition type = (StringTypeDefinition) stringleaf.getType();
        assertEquals(barQName("string-ext4"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(1, patterns.size());
        PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^(?:[e-z]*)$", pattern.getJavaPatternString());
        assertEquals(1, type.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final StringTypeDefinition baseType5 = type.getBaseType();
        assertEquals(barQName("string-ext5"), baseType5.getQName());
        assertEquals(Optional.empty(), baseType5.getUnits());
        assertEquals(Optional.empty(), baseType5.getDefaultValue());
        patterns = baseType5.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^(?:[\\i-[:]][\\c-[:]]*)$", pattern.getJavaPatternString());
        assertEquals(1, baseType5.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final StringTypeDefinition baseType1 = type.getBaseType();
        assertEquals(barQName("string-ext3"), baseType1.getQName());
        assertEquals(Optional.empty(), baseType1.getUnits());
        assertEquals(Optional.empty(), baseType1.getDefaultValue());
        patterns = baseType1.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^(?:[b-u]*)$", pattern.getJavaPatternString());
        assertEquals(1, baseType1.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        assertEquals(barQName("string-ext2"), baseType2.getQName());
        assertEquals(Optional.empty(), baseType2.getUnits());
        assertEquals(Optional.empty(), baseType2.getDefaultValue());
        assertTrue(baseType2.getPatternConstraints().isEmpty());
        final RangeSet<Integer> baseType2Lengths = baseType2.getLengthConstraint().orElseThrow().getAllowedRanges();
        assertEquals(1, baseType2Lengths.asRanges().size());
        Range<Integer> length = baseType2Lengths.span();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType3 = baseType2.getBaseType();
        assertEquals(barQName("string-ext1"), baseType3.getQName());
        assertEquals(Optional.empty(), baseType3.getUnits());
        assertEquals(Optional.empty(), baseType3.getDefaultValue());
        patterns = baseType3.getPatternConstraints();
        assertEquals(1, patterns.size());
        pattern = patterns.iterator().next();
        assertEquals("^(?:[a-k]*)$", pattern.getJavaPatternString());
        final RangeSet<Integer> baseType3Lengths = baseType3.getLengthConstraint().orElseThrow().getAllowedRanges();
        assertEquals(1, baseType3Lengths.asRanges().size());
        length = baseType3Lengths.span();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType3.getBaseType());
    }

    @Test
    void testTypedefInvalidPatternsResolving() {
        final LeafSchemaNode multiplePatternStringLeaf = (LeafSchemaNode) FOO.getDataChildByName(
            fooQName("multiple-pattern-string-leaf"));
        StringTypeDefinition type = (StringTypeDefinition) multiplePatternStringLeaf.getType();
        assertEquals(barQName("multiple-pattern-string"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        List<PatternConstraint> patterns = type.getPatternConstraints();
        assertEquals(2, patterns.size());
        assertEquals("^(?:[A-Z]*-%22!\\^\\^)$", patterns.get(0).getJavaPatternString());
        assertEquals("^(?:[e-z]*)$", patterns.get(1).getJavaPatternString());
        assertEquals(1, type.getLengthConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final LeafSchemaNode multiplePatternDirectStringDefLeaf = (LeafSchemaNode) FOO.getDataChildByName(
            fooQName("multiple-pattern-direct-string-def-leaf"));
        type = (StringTypeDefinition) multiplePatternDirectStringDefLeaf.getType();
        assertEquals(fooQName("string"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        patterns = type.getPatternConstraints();
        assertEquals(3, patterns.size());

        assertEquals("^(?:[e-z]*)$", patterns.get(0).getJavaPatternString());
        assertEquals("^(?:[A-Z]*-%22!\\^\\^})$", patterns.get(1).getJavaPatternString());
        assertEquals("^(?:[a-d]*)$", patterns.get(2).getJavaPatternString());
    }

    @Test
    void testTypedefLengthsResolving() {
        final LeafSchemaNode lengthLeaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("length-leaf"));
        final StringTypeDefinition type = (StringTypeDefinition) lengthLeaf.getType();

        assertEquals(fooQName("string-ext2"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertTrue(type.getPatternConstraints().isEmpty());
        final RangeSet<Integer> typeLengths = type.getLengthConstraint().orElseThrow().getAllowedRanges();
        assertEquals(1, typeLengths.asRanges().size());
        Range<Integer> length = typeLengths.span();
        assertEquals(7, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType1 = type.getBaseType();
        assertEquals(barQName("string-ext2"), baseType1.getQName());
        assertEquals(Optional.empty(), baseType1.getUnits());
        assertEquals(Optional.empty(), baseType1.getDefaultValue());
        assertTrue(baseType1.getPatternConstraints().isEmpty());
        final RangeSet<Integer> baseType2Lengths = baseType1.getLengthConstraint().orElseThrow().getAllowedRanges();
        assertEquals(1, baseType2Lengths.asRanges().size());
        length = baseType2Lengths.span();
        assertEquals(6, length.lowerEndpoint().intValue());
        assertEquals(10, length.upperEndpoint().intValue());

        final StringTypeDefinition baseType2 = baseType1.getBaseType();
        assertEquals(barQName("string-ext1"), baseType2.getQName());
        assertEquals(Optional.empty(), baseType2.getUnits());
        assertEquals(Optional.empty(), baseType2.getDefaultValue());
        final List<PatternConstraint> patterns = baseType2.getPatternConstraints();
        assertEquals(1, patterns.size());
        final PatternConstraint pattern = patterns.iterator().next();
        assertEquals("^(?:[a-k]*)$", pattern.getJavaPatternString());
        final RangeSet<Integer> baseType3Lengths = baseType2.getLengthConstraint().orElseThrow().getAllowedRanges();
        assertEquals(1, baseType3Lengths.asRanges().size());
        length = baseType3Lengths.span();
        assertEquals(5, length.lowerEndpoint().intValue());
        assertEquals(11, length.upperEndpoint().intValue());

        assertEquals(BaseTypes.stringType(), baseType2.getBaseType());
    }

    @Test
    void testTypedefDecimal1() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("decimal-leaf"));

        assertInstanceOf(DecimalTypeDefinition.class, testleaf.getType());
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        assertEquals(barQName("my-decimal-type"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertEquals(6, type.getFractionDigits());
        assertEquals(1, type.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final DecimalTypeDefinition typeBase = type.getBaseType();
        assertEquals(barQName("decimal64"), typeBase.getQName());
        assertEquals(Optional.empty(), typeBase.getUnits());
        assertEquals(Optional.empty(), typeBase.getDefaultValue());
        assertEquals(6, typeBase.getFractionDigits());
        assertEquals(1, typeBase.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        assertNull(typeBase.getBaseType());
    }

    @Test
    void testTypedefDecimal2() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("decimal-leaf2"));

        assertInstanceOf(DecimalTypeDefinition.class, testleaf.getType());
        final DecimalTypeDefinition type = (DecimalTypeDefinition) testleaf.getType();
        assertEquals(barQName("my-decimal-type"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());
        assertEquals(6, type.getFractionDigits());
        assertEquals(1, type.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges().size());

        final DecimalTypeDefinition baseTypeDecimal = type.getBaseType();
        assertEquals(6, baseTypeDecimal.getFractionDigits());
    }

    @Test
    void testTypedefUnion() {
        final LeafSchemaNode unionleaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("union-leaf"));

        assertInstanceOf(UnionTypeDefinition.class, unionleaf.getType());
        final UnionTypeDefinition type = (UnionTypeDefinition) unionleaf.getType();
        assertEquals(barQName("my-union-ext"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());

        final UnionTypeDefinition baseType = type.getBaseType();
        assertEquals(barQName("my-union"), baseType.getQName());
        assertEquals(Optional.empty(), baseType.getUnits());
        assertEquals(Optional.empty(), baseType.getDefaultValue());

        final UnionTypeDefinition unionType = baseType.getBaseType();
        final List<TypeDefinition<?>> unionTypes = unionType.getTypes();
        assertEquals(2, unionTypes.size());

        final Int16TypeDefinition unionType1 = (Int16TypeDefinition) unionTypes.get(0);
        assertEquals(barQName("my-union"), baseType.getQName());
        assertEquals(Optional.empty(), unionType1.getUnits());
        assertEquals(Optional.empty(), unionType1.getDefaultValue());

        final var ranges = unionType1.getRangeConstraint().orElseThrow();
        assertEquals(1, ranges.getAllowedRanges().asRanges().size());
        final var range = ranges.getAllowedRanges().span();
        assertEquals(Short.valueOf("1"), range.lowerEndpoint());
        assertEquals(Short.valueOf("100"), range.upperEndpoint());
        assertEquals(BaseTypes.int16Type(), unionType1.getBaseType());

        assertEquals(BaseTypes.int32Type(), unionTypes.get(1));
    }

    @Test
    void testNestedUnionResolving() {
        final LeafSchemaNode testleaf = (LeafSchemaNode) FOO.getDataChildByName(fooQName("custom-union-leaf"));

        assertInstanceOf(UnionTypeDefinition.class, testleaf.getType());
        final UnionTypeDefinition type = (UnionTypeDefinition) testleaf.getType();
        assertEquals(bazQName("union1"), type.getQName());
        assertEquals(Optional.empty(), type.getUnits());
        assertEquals(Optional.empty(), type.getDefaultValue());

        final UnionTypeDefinition typeBase = type.getBaseType();
        assertEquals(bazQName("union2"), typeBase.getQName());
        assertEquals(Optional.empty(), typeBase.getUnits());
        assertEquals(Optional.empty(), typeBase.getDefaultValue());

        final UnionTypeDefinition union = typeBase.getBaseType();
        final List<TypeDefinition<?>> unionTypes = union.getTypes();
        assertEquals(2, unionTypes.size());
        assertEquals(BaseTypes.int32Type(), unionTypes.get(0));
        assertInstanceOf(UnionTypeDefinition.class, unionTypes.get(1));

        final UnionTypeDefinition unionType1 = (UnionTypeDefinition) unionTypes.get(1);
        assertEquals(barQName("nested-union2"), unionType1.getQName());
        assertEquals(Optional.empty(), unionType1.getUnits());
        assertEquals(Optional.empty(), unionType1.getDefaultValue());

        final UnionTypeDefinition nestedUnion = unionType1.getBaseType();
        final List<TypeDefinition<?>> nestedUnion2Types = nestedUnion.getTypes();
        assertEquals(2, nestedUnion2Types.size());
        assertInstanceOf(StringTypeDefinition.class, nestedUnion2Types.get(1));
        assertInstanceOf(UnionTypeDefinition.class, nestedUnion2Types.get(0));

        final UnionTypeDefinition myUnionExt = (UnionTypeDefinition) nestedUnion2Types.get(0);
        assertEquals(barQName("my-union-ext"), myUnionExt.getQName());
        assertEquals(Optional.empty(), myUnionExt.getUnits());
        assertEquals(Optional.empty(), myUnionExt.getDefaultValue());


        final UnionTypeDefinition myUnion = myUnionExt.getBaseType();
        assertEquals(barQName("my-union"), myUnion.getQName());
        assertEquals(Optional.empty(), myUnion.getUnits());
        assertEquals(Optional.empty(), myUnion.getDefaultValue());

        final UnionTypeDefinition myUnionBase = myUnion.getBaseType();
        final List<TypeDefinition<?>> myUnionBaseTypes = myUnionBase.getTypes();
        assertEquals(2, myUnionBaseTypes.size());
        assertInstanceOf(Int16TypeDefinition.class, myUnionBaseTypes.get(0));
        assertEquals(BaseTypes.int32Type(), myUnionBaseTypes.get(1));

        final Int16TypeDefinition int16Ext = (Int16TypeDefinition) myUnionBaseTypes.get(0);
        assertEquals(TypeDefinitions.INT16, int16Ext.getQName());
        assertEquals(Optional.empty(), int16Ext.getUnits());
        assertEquals(Optional.empty(), int16Ext.getDefaultValue());
        final var ranges = int16Ext.getRangeConstraint().orElseThrow().getAllowedRanges().asRanges();
        assertEquals(1, ranges.size());
        final var range = ranges.iterator().next();
        assertEquals(1, range.lowerEndpoint().intValue());
        assertEquals(100, range.upperEndpoint().intValue());

        assertEquals(BaseTypes.int16Type(), int16Ext.getBaseType());
    }

    @Test
    void testChoice() {
        final ContainerSchemaNode transfer = (ContainerSchemaNode) FOO.getDataChildByName(fooQName("transfer"));
        final ChoiceSchemaNode how = (ChoiceSchemaNode) transfer.getDataChildByName(fooQName("how"));
        final var cases = how.getCases();
        assertEquals(5, cases.size());
        CaseSchemaNode input = null;
        CaseSchemaNode output = null;
        for (final CaseSchemaNode caseNode : cases) {
            if ("input".equals(caseNode.getQName().getLocalName())) {
                input = caseNode;
            } else if ("output".equals(caseNode.getQName().getLocalName())) {
                output = caseNode;
            }
        }
        assertNotNull(input);
        assertNotNull(output);
    }

    @Test
    void testDeviation() {
        final var deviations = FOO.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation dev = deviations.iterator().next();
        assertEquals(Optional.of("system/user ref"), dev.getReference());

        assertEquals(Absolute.of(barQName("interfaces"), barQName("ifEntry")), dev.getTargetPath());
        assertEquals(DeviateKind.ADD, dev.getDeviates().iterator().next().getDeviateType());
    }

    @Test
    void testUnknownNode() {
        final ContainerSchemaNode network = (ContainerSchemaNode) BAZ.getDataChildByName(bazQName("network"));
        final var unknownNodes = network.asEffectiveStatement().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownNodes.size());
        assertEquals("point", unknownNodes.iterator().next().argument());
    }

    @Test
    void testFeature() {
        final var features = BAZ.getFeatures();
        assertEquals(3, features.size());
    }

    @Test
    void testExtension() {
        final var extensions = BAZ.getExtensionSchemaNodes();
        assertEquals(1, extensions.size());
        final ExtensionDefinition extension = extensions.iterator().next();
        assertEquals("name", extension.getArgument());
        assertEquals(
            Optional.of("Takes as argument a name string. Makes the code generator use the given name in the #define."),
            extension.getDescription());
        assertTrue(extension.isYinElement());
    }

    @Test
    void testNotification() {
        final var notifications = BAZ.getNotifications();
        assertEquals(1, notifications.size());

        final NotificationDefinition notification = notifications.iterator().next();
        // test SchemaNode args
        assertEquals(bazQName("event"), notification.getQName());
        assertFalse(notification.getDescription().isPresent());
        assertFalse(notification.getReference().isPresent());
        assertEquals(Status.CURRENT, notification.getStatus());
        assertEquals(0, notification.getUnknownSchemaNodes().size());
        // test DataNodeContainer args
        assertEquals(0, notification.getTypeDefinitions().size());
        assertEquals(3, notification.getChildNodes().size());
        assertEquals(0, notification.getGroupings().size());
        assertEquals(0, notification.getUses().size());

        final LeafSchemaNode eventClass = (LeafSchemaNode) notification.getDataChildByName(bazQName("event-class"));
        assertInstanceOf(StringTypeDefinition.class, eventClass.getType());
        final LeafSchemaNode severity = (LeafSchemaNode) notification.getDataChildByName(bazQName("severity"));
        assertInstanceOf(StringTypeDefinition.class, severity.getType());
    }

    @Test
    void testRpc() {
        final var rpcs = BAZ.getRpcs();
        assertEquals(1, rpcs.size());

        final RpcDefinition rpc = rpcs.iterator().next();
        assertEquals(Optional.of("Retrieve all or part of a specified configuration."), rpc.getDescription());
        assertEquals(Optional.of("RFC 6241, Section 7.1"), rpc.getReference());
    }

    @Test
    void testTypePath() {
        final Collection<? extends TypeDefinition<?>> types = BAR.getTypeDefinitions();

        // int32-ext1
        final Int32TypeDefinition int32ext1 = (Int32TypeDefinition) TestUtils.findTypedef(types, "int32-ext1");
        assertEquals(barQName("int32-ext1"), int32ext1.getQName());

        // int32-ext1/int32
        assertEquals(BaseTypes.int32Type(), int32ext1.getBaseType());
    }

    @Test
    void testTypePath2() {
        final var types = BAR.getTypeDefinitions();

        // my-decimal-type
        final DecimalTypeDefinition myDecType = (DecimalTypeDefinition) TestUtils.findTypedef(types, "my-decimal-type");
        assertEquals(barQName("my-decimal-type"), myDecType.getQName());

        // my-base-int32-type/int32
        assertEquals(barQName("decimal64"), myDecType.getBaseType().getQName());
    }

    @Test
    void testSubmodules() {
        final DataSchemaNode id = FOO.getDataChildByName(fooQName("id"));
        assertNotNull(id);
        final DataSchemaNode subExt = FOO.getDataChildByName(fooQName("sub-ext"));
        assertNotNull(subExt);
        final DataSchemaNode subTransfer = FOO.getDataChildByName(fooQName("sub-transfer"));
        assertNotNull(subTransfer);

        assertEquals(2, FOO.getExtensionSchemaNodes().size());
        assertEquals(2, FOO.getAugmentations().size());
    }

    @Test
    void unknownStatementInSubmoduleHeaderTest() {
        assertEffectiveModel(
            "/yang-grammar-test/revisions-extension.yang",
            "/yang-grammar-test/submodule-header-extension.yang");
    }

    @Test
    void unknownStatementBetweenRevisionsTest() {
        assertEffectiveModel(
            "/yang-grammar-test/revisions-extension.yang",
            "/yang-grammar-test/submodule-header-extension.yang");
    }

    @Test
    void unknownStatementsInStatementsTest() {
        assertSourceException(startsWith("aaa is not a YANG statement or use of extension"),
            "/yang-grammar-test/stmtsep-in-statements.yang",
            "/yang-grammar-test/stmtsep-in-statements2.yang",
            "/yang-grammar-test/stmtsep-in-statements-sub.yang");
    }
}

package org.opendaylight.yangtools.yang.parser.impl;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.parser.api.YangContextParser;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

public class Bug4623Test {

    @Test
    public void testStringTypeWithUnknownSchemaNodeAtTheEndOfTypeDefinition() throws Exception {
        // given
        File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());
        YangContextParser parser = new YangParserImpl();

        // when
        SchemaContext schemaContext = parser.parseFiles(Arrays.asList(extdef, stringWithExt));

        LeafSchemaNode leaf = (LeafSchemaNode) schemaContext
                .findModuleByName("types", null)
                .getDataChildByName("leaf-length-pattern-unknown");

        // then
        Assert.assertNotNull(leaf);
        Assert.assertNotNull(leaf.getType());

        final List<UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();
        Assert.assertNotNull(leaf.getUnknownSchemaNodes());
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final ExtendedType type = (ExtendedType) leaf.getType();
        final List<LengthConstraint> lengthConstraints = type.getLengthConstraints();
        final List<PatternConstraint> patternConstraints = type.getPatternConstraints();

        Assert.assertNotNull(lengthConstraints);
        Assert.assertNotNull(patternConstraints);
        Assert.assertFalse(lengthConstraints.size() == 0);
        Assert.assertFalse(patternConstraints.size() == 0);

        final LengthConstraint lengthConstraint = lengthConstraints.get(0);
        Assert.assertEquals(lengthConstraint.getMin(), BigInteger.valueOf(2L));
        Assert.assertEquals(lengthConstraint.getMax(), BigInteger.TEN);

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }


    @Test
    public void testStringTypeWithUnknownSchemaNodeBetweenStringRestrictionStatements() throws Exception {
        // given
        File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());
        YangContextParser parser = new YangParserImpl();

        // when
        SchemaContext schemaContext = parser.parseFiles(Arrays.asList(extdef, stringWithExt));

        LeafSchemaNode leaf = (LeafSchemaNode) schemaContext
                .findModuleByName("types", null)
                .getDataChildByName("leaf-length-unknown-pattern");

        // then
        Assert.assertNotNull(leaf);
        Assert.assertNotNull(leaf.getType());

        final List<UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();
        Assert.assertNotNull(leaf.getUnknownSchemaNodes());
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final ExtendedType type = (ExtendedType) leaf.getType();
        final List<LengthConstraint> lengthConstraints = type.getLengthConstraints();
        final List<PatternConstraint> patternConstraints = type.getPatternConstraints();

        Assert.assertNotNull(lengthConstraints);
        Assert.assertNotNull(patternConstraints);
        Assert.assertFalse(lengthConstraints.size() == 0);
        Assert.assertFalse(patternConstraints.size() == 0);

        final LengthConstraint lengthConstraint = lengthConstraints.get(0);
        Assert.assertEquals(lengthConstraint.getMin(), BigInteger.valueOf(2L));
        Assert.assertEquals(lengthConstraint.getMax(), BigInteger.TEN);

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }

    @Test
    public void testStringTypeWithUnknownSchemaNodeOnTheStartOfTypeDefinition() throws Exception {
        // given
        File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());
        YangContextParser parser = new YangParserImpl();

        // when
        SchemaContext schemaContext = parser.parseFiles(Arrays.asList(extdef, stringWithExt));

        LeafSchemaNode leaf = (LeafSchemaNode) schemaContext
                .findModuleByName("types", null)
                .getDataChildByName("leaf-unknown-length-pattern");

        // then
        Assert.assertNotNull(leaf);
        Assert.assertNotNull(leaf.getType());

        final List<UnknownSchemaNode> unknownSchemaNodes = leaf.getUnknownSchemaNodes();
        Assert.assertNotNull(leaf.getUnknownSchemaNodes());
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final ExtendedType type = (ExtendedType) leaf.getType();
        final List<LengthConstraint> lengthConstraints = type.getLengthConstraints();
        final List<PatternConstraint> patternConstraints = type.getPatternConstraints();

        Assert.assertNotNull(lengthConstraints);
        Assert.assertNotNull(patternConstraints);
        Assert.assertFalse(lengthConstraints.size() == 0);
        Assert.assertFalse(patternConstraints.size() == 0);

        final LengthConstraint lengthConstraint = lengthConstraints.get(0);
        Assert.assertEquals(lengthConstraint.getMin(), BigInteger.valueOf(2L));
        Assert.assertEquals(lengthConstraint.getMax(), BigInteger.TEN);

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }
}
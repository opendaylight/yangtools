/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import com.google.common.collect.Range;
import java.io.File;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public class Bug4623Test {

    @Test
    public void testStringTypeWithUnknownSchemaNodeAtTheEndOfTypeDefinition() throws Exception {
        // given
        final File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        final File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());

        // when
        final SchemaContext schemaContext = TestUtils.parseYangSources(extdef, stringWithExt);

        final LeafSchemaNode leaf = (LeafSchemaNode) schemaContext.findModuleByName("types", null).getDataChildByName(
                QName.create("urn:custom.types.demo", "1970-01-01", "leaf-length-pattern-unknown"));

        // then
        Assert.assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        Assert.assertNotNull(type);
        final List<UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        Assert.assertNotNull(unknownSchemaNodes);
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraint = ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        Assert.assertNotNull(lengthConstraint);
        Assert.assertNotNull(patternConstraints);
        Assert.assertFalse(lengthConstraint.getAllowedRanges().isEmpty());
        Assert.assertFalse(patternConstraints.isEmpty());

        final Range<Integer> span = lengthConstraint.getAllowedRanges().span();
        Assert.assertEquals(Integer.valueOf(2), span.lowerEndpoint());
        Assert.assertEquals(Integer.valueOf(10), span.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }

    @Test
    public void testStringTypeWithUnknownSchemaNodeBetweenStringRestrictionStatements() throws Exception {
        // given
        final File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        final File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());

        // when
        final SchemaContext schemaContext = TestUtils.parseYangSources(extdef, stringWithExt);

        final LeafSchemaNode leaf = (LeafSchemaNode) schemaContext.findModuleByName("types", null).getDataChildByName(
                QName.create("urn:custom.types.demo", "1970-01-01", "leaf-length-unknown-pattern"));

        // then
        Assert.assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        Assert.assertNotNull(type);
        final List<UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        Assert.assertNotNull(unknownSchemaNodes);
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraints = ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        Assert.assertNotNull(lengthConstraints);
        Assert.assertNotNull(patternConstraints);
        Assert.assertEquals(1, lengthConstraints.getAllowedRanges().asRanges().size());
        Assert.assertFalse(patternConstraints.isEmpty());

        final Range<Integer> lengthConstraint = lengthConstraints.getAllowedRanges().span();
        Assert.assertEquals(Integer.valueOf(2), lengthConstraint.lowerEndpoint());
        Assert.assertEquals(Integer.valueOf(10), lengthConstraint.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }

    @Test
    public void testStringTypeWithUnknownSchemaNodeOnTheStartOfTypeDefinition() throws Exception {
        // given
        final File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        final File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());

        // when
        final SchemaContext schemaContext = TestUtils.parseYangSources(extdef, stringWithExt);

        final LeafSchemaNode leaf = (LeafSchemaNode) schemaContext.findModuleByName("types", null).getDataChildByName(
                QName.create("urn:custom.types.demo", "1970-01-01", "leaf-unknown-length-pattern"));

        // then
        Assert.assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        Assert.assertNotNull(type);
        final List<UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        Assert.assertNotNull(unknownSchemaNodes);
        Assert.assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.get(0);
        Assert.assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        Assert.assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(), "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraints =
                ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        Assert.assertNotNull(lengthConstraints);
        Assert.assertNotNull(patternConstraints);
        Assert.assertEquals(1, lengthConstraints.getAllowedRanges().asRanges().size());
        Assert.assertFalse(patternConstraints.size() == 0);

        final Range<Integer> lengthConstraint = lengthConstraints.getAllowedRanges().span();
        Assert.assertEquals(Integer.valueOf(2), lengthConstraint.lowerEndpoint());
        Assert.assertEquals(Integer.valueOf(10), lengthConstraint.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        Assert.assertEquals(patternConstraint.getRegularExpression(), "^[0-9a-fA-F]$");
    }
}
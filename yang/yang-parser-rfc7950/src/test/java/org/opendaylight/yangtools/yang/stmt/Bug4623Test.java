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

import com.google.common.collect.Range;
import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
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

        final LeafSchemaNode leaf = (LeafSchemaNode) typesModule(schemaContext).getDataChildByName(
            QName.create(URI.create("urn:custom.types.demo"), "leaf-length-pattern-unknown"));

        // then
        assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        assertNotNull(unknownSchemaNodes);
        assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(),
            "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraint = ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        assertNotNull(lengthConstraint);
        assertNotNull(patternConstraints);
        assertFalse(lengthConstraint.getAllowedRanges().isEmpty());
        assertFalse(patternConstraints.isEmpty());

        final Range<Integer> span = lengthConstraint.getAllowedRanges().span();
        assertEquals(Integer.valueOf(2), span.lowerEndpoint());
        assertEquals(Integer.valueOf(10), span.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        assertEquals(patternConstraint.getRegularExpressionString(), "[0-9a-fA-F]");
    }

    @Test
    public void testStringTypeWithUnknownSchemaNodeBetweenStringRestrictionStatements() throws Exception {
        // given
        final File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        final File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());

        // when
        final SchemaContext schemaContext = TestUtils.parseYangSources(extdef, stringWithExt);

        final LeafSchemaNode leaf = (LeafSchemaNode) typesModule(schemaContext).getDataChildByName(
                QName.create(URI.create("urn:custom.types.demo"), "leaf-length-unknown-pattern"));

        // then
        assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        assertNotNull(unknownSchemaNodes);
        assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(),
            "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraints = ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        assertNotNull(lengthConstraints);
        assertNotNull(patternConstraints);
        assertEquals(1, lengthConstraints.getAllowedRanges().asRanges().size());
        assertFalse(patternConstraints.isEmpty());

        final Range<Integer> lengthConstraint = lengthConstraints.getAllowedRanges().span();
        assertEquals(Integer.valueOf(2), lengthConstraint.lowerEndpoint());
        assertEquals(Integer.valueOf(10), lengthConstraint.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        assertEquals(patternConstraint.getRegularExpressionString(), "[0-9a-fA-F]");
    }

    @Test
    public void testStringTypeWithUnknownSchemaNodeOnTheStartOfTypeDefinition() throws Exception {
        // given
        final File extdef = new File(getClass().getResource("/bugs/bug4623/extension-def.yang").toURI());
        final File stringWithExt = new File(getClass().getResource("/bugs/bug4623/string-with-ext.yang").toURI());

        // when
        final SchemaContext schemaContext = TestUtils.parseYangSources(extdef, stringWithExt);

        final LeafSchemaNode leaf = (LeafSchemaNode) typesModule(schemaContext).getDataChildByName(
                QName.create(URI.create("urn:custom.types.demo"), "leaf-unknown-length-pattern"));

        // then
        assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);
        final Collection<? extends UnknownSchemaNode> unknownSchemaNodes = type.getUnknownSchemaNodes();
        assertNotNull(unknownSchemaNodes);
        assertFalse(unknownSchemaNodes.size() == 0);

        final UnknownSchemaNode unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertEquals(unknownSchemaNode.getNodeParameter(), "unknown");
        assertEquals(unknownSchemaNode.getNodeType().getModule().getNamespace().toString(),
            "urn:simple.extension.typedefs");

        final LengthConstraint lengthConstraints =
                ((StringTypeDefinition) type).getLengthConstraint().get();
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();

        assertNotNull(lengthConstraints);
        assertNotNull(patternConstraints);
        assertEquals(1, lengthConstraints.getAllowedRanges().asRanges().size());
        assertFalse(patternConstraints.size() == 0);

        final Range<Integer> lengthConstraint = lengthConstraints.getAllowedRanges().span();
        assertEquals(Integer.valueOf(2), lengthConstraint.lowerEndpoint());
        assertEquals(Integer.valueOf(10), lengthConstraint.upperEndpoint());

        final PatternConstraint patternConstraint = patternConstraints.get(0);
        assertEquals(patternConstraint.getRegularExpressionString(), "[0-9a-fA-F]");
    }

    private static Module typesModule(final SchemaContext context) {
        return context.findModules("types").iterator().next();
    }
}
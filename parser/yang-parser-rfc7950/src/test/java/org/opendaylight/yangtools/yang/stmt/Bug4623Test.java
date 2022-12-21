/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Range;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

class Bug4623Test extends AbstractYangTest {
    private static Module TYPES;

    @BeforeAll
    static void beforeClass() throws Exception {
        // given
        TYPES = assertEffectiveModelDir("/bugs/bug4623").findModules("types").iterator().next();
    }

    @Test
    void testStringTypeWithUnknownSchemaNodeAtTheEndOfTypeDefinition() {
        // when
        final LeafSchemaNode leaf = (LeafSchemaNode) TYPES.getDataChildByName(
            QName.create(XMLNamespace.of("urn:custom.types.demo"), "leaf-length-pattern-unknown"));

        // then
        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);

        // here are no effective extensions
        assertEquals(0, type.getUnknownSchemaNodes().size());
        assertExtension(leaf);

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
        assertEquals("[0-9a-fA-F]", patternConstraint.getRegularExpressionString());
    }

    @Test
    void testStringTypeWithUnknownSchemaNodeBetweenStringRestrictionStatements() {
        // when
        final LeafSchemaNode leaf = (LeafSchemaNode) TYPES.getDataChildByName(
            QName.create(XMLNamespace.of("urn:custom.types.demo"), "leaf-length-unknown-pattern"));

        // then
        assertNotNull(leaf);

        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);
        assertEquals(0, type.getUnknownSchemaNodes().size());
        assertExtension(leaf);

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
        assertEquals("[0-9a-fA-F]", patternConstraint.getRegularExpressionString());
    }

    @Test
    void testStringTypeWithUnknownSchemaNodeOnTheStartOfTypeDefinition() {
        // when
        final LeafSchemaNode leaf = (LeafSchemaNode) TYPES.getDataChildByName(
            QName.create(XMLNamespace.of("urn:custom.types.demo"), "leaf-unknown-length-pattern"));

        // then
        final TypeDefinition<?> type = leaf.getType();
        assertNotNull(type);
        assertEquals(0, type.getUnknownSchemaNodes().size());
        assertExtension(leaf);

        final LengthConstraint lengthConstraints =
            ((StringTypeDefinition) type).getLengthConstraint().get();
        assertEquals(1, lengthConstraints.getAllowedRanges().asRanges().size());

        final Range<Integer> lengthConstraint = lengthConstraints.getAllowedRanges().span();
        assertEquals(Integer.valueOf(2), lengthConstraint.lowerEndpoint());
        assertEquals(Integer.valueOf(10), lengthConstraint.upperEndpoint());

        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        final PatternConstraint patternConstraint = patternConstraints.get(0);
        assertEquals("[0-9a-fA-F]", patternConstraint.getRegularExpressionString());
    }

    private static void assertExtension(final LeafSchemaNode leaf) {
        final Collection<? extends UnrecognizedStatement> unknownSchemaNodes = leaf.asEffectiveStatement().getDeclared()
            .findFirstDeclaredSubstatement(TypeStatement.class).orElseThrow()
            .declaredSubstatements(UnrecognizedStatement.class);
        assertEquals(1, unknownSchemaNodes.size());

        final UnrecognizedStatement unknownSchemaNode = unknownSchemaNodes.iterator().next();
        assertEquals("unknown", unknownSchemaNode.argument());
        assertEquals(QName.create("urn:simple.extension.typedefs", "unknown"),
            unknownSchemaNode.statementDefinition().getStatementName());
    }
}
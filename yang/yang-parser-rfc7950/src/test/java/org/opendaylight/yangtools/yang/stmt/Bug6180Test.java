/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public class Bug6180Test {

    @Test
    public void stringTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(new File(getClass().getResource(
                "/bugs/bug6180/string-test.yang").toURI()));
        assertNotNull(schemaContext);
        assertEquals(1, schemaContext.getModules().size());
        final Module module = schemaContext.getModules().iterator().next();
        assertEquals(Optional.of("    1. this text contains \"string enclosed in double quotes\" and"
                + " special characters: \\,\n,\t          2. this text contains \"string enclosed in double quotes\""
                + " and special characters: \\,\n,\n,                     3. this text contains \"string enclosed in"
                + " double quotes\" and special characters: \\,\n,\t      "), module.getDescription());
    }

    @Test
    public void doubleQuotesTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(new File(getClass().getResource(
                "/bugs/bug6180/double-quotes.yang").toURI()));
        assertNotNull(schemaContext);
        verifyDoubleQuotesExpression(schemaContext);
    }

    @Test
    public void doubleQuotesSinbleInsideTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(new File(getClass().getResource(
                "/bugs/bug6180/double-quotes-single-inside.yang").toURI()));
        assertNotNull(schemaContext);
        verifySingleQuotesExpression(schemaContext);
    }

    @Test
    public void singleQuotesTest() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources(new File(getClass().getResource(
                "/bugs/bug6180/single-quotes.yang").toURI()));
        assertNotNull(schemaContext);
        verifyDoubleQuotesExpression(schemaContext);
    }

    private static void verifyDoubleQuotesExpression(final SchemaContext schemaContext) {
        final DataSchemaNode dataNodeBar = schemaContext.getDataChildByName(QName.create("foo", "2016-07-11", "bar"));
        assertTrue(dataNodeBar instanceof ContainerSchemaNode);
        final ContainerSchemaNode bar = (ContainerSchemaNode) dataNodeBar;
        final RevisionAwareXPath whenCondition = bar.getWhenCondition().get();
        assertEquals("/foo != \"bar\"", whenCondition.getOriginalString());

        final Set<TypeDefinition<?>> typeDefinitions = schemaContext.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());
        final TypeDefinition<?> type = typeDefinitions.iterator().next();
        assertTrue(type instanceof StringTypeDefinition);
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        final PatternConstraint pattern = patternConstraints.iterator().next();
        assertEquals("^(?:\".*\")$", pattern.getJavaPatternString());
        assertTrue(Pattern.compile(pattern.getJavaPatternString()).matcher("\"enclosed string in quotes\"").matches());
    }

    private static void verifySingleQuotesExpression(final SchemaContext schemaContext) {
        final DataSchemaNode dataNodeBar = schemaContext.getDataChildByName(QName.create("foo", "2016-07-11", "bar"));
        assertTrue(dataNodeBar instanceof ContainerSchemaNode);
        final ContainerSchemaNode bar = (ContainerSchemaNode) dataNodeBar;
        final RevisionAwareXPath whenCondition = bar.getWhenCondition().get();
        assertEquals("/foo != 'bar'", whenCondition.getOriginalString());

        final Set<TypeDefinition<?>> typeDefinitions = schemaContext.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());
        final TypeDefinition<?> type = typeDefinitions.iterator().next();
        assertTrue(type instanceof StringTypeDefinition);
        final List<PatternConstraint> patternConstraints = ((StringTypeDefinition) type).getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        final PatternConstraint pattern = patternConstraints.iterator().next();
        assertEquals("^(?:'.*')$", pattern.getJavaPatternString());
        assertTrue(Pattern.compile(pattern.getJavaPatternString()).matcher("'enclosed string in quotes'").matches());
    }
}
/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

class Bug6180Test extends AbstractYangTest {
    @Test
    void stringTest() {
        final var schemaContext = assertEffectiveModel("/bugs/bug6180/string-test.yang");
        assertEquals(1, schemaContext.getModules().size());
        final var module = schemaContext.getModules().iterator().next();
        assertEquals(Optional.of("    1. this text contains \"string enclosed in double quotes\" and"
            + " special characters: \\,\n,\t          2. this text contains \"string enclosed in double quotes\""
            + " and special characters: \\,\n,\n,                     3. this text contains \"string enclosed in"
            + " double quotes\" and special characters: \\,\n,\t      "), module.getDescription());
    }

    @Test
    void doubleQuotesTest() {
        verifyDoubleQuotesExpression(assertEffectiveModel("/bugs/bug6180/double-quotes.yang"));
    }

    @Test
    void doubleQuotesSinbleInsideTest() {
        verifySingleQuotesExpression(assertEffectiveModel("/bugs/bug6180/double-quotes-single-inside.yang"));
    }

    @Test
    void singleQuotesTest() {
        verifyDoubleQuotesExpression(assertEffectiveModel("/bugs/bug6180/single-quotes.yang"));
    }

    private static void verifyDoubleQuotesExpression(final EffectiveModelContext schemaContext) {
        final var bar = assertInstanceOf(ContainerSchemaNode.class,
            schemaContext.getDataChildByName(QName.create("foo", "2016-07-11", "bar")));
        assertEquals("/foo != \"bar\"", bar.getWhenCondition().orElseThrow().toString());

        final var typeDefinitions = schemaContext.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());
        final var patternConstraints = assertInstanceOf(StringTypeDefinition.class, typeDefinitions.iterator().next())
            .getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        final var pattern = patternConstraints.iterator().next();
        assertEquals("^(?:\".*\")$", pattern.getJavaPatternString());
        assertTrue(Pattern.compile(pattern.getJavaPatternString()).matcher("\"enclosed string in quotes\"").matches());
    }

    private static void verifySingleQuotesExpression(final EffectiveModelContext schemaContext) {
        final var bar = assertInstanceOf(ContainerSchemaNode.class,
            schemaContext.getDataChildByName(QName.create("foo", "2016-07-11", "bar")));
        assertEquals("/foo != 'bar'", bar.getWhenCondition().orElseThrow().toString());

        final var typeDefinitions = schemaContext.getTypeDefinitions();
        assertEquals(1, typeDefinitions.size());
        final var patternConstraints = assertInstanceOf(StringTypeDefinition.class, typeDefinitions.iterator().next())
            .getPatternConstraints();
        assertEquals(1, patternConstraints.size());
        final var pattern = patternConstraints.iterator().next();
        assertEquals("^(?:'.*')$", pattern.getJavaPatternString());
        assertTrue(Pattern.compile(pattern.getJavaPatternString()).matcher("'enclosed string in quotes'").matches());
    }
}
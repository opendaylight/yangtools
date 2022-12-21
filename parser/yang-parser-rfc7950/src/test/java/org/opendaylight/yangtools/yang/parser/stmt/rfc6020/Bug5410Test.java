/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug5410Test extends AbstractYangTest {
    private static final String FOO_NS = "foo";

    @Test
    void testYangPattern() {
        final var context = assertEffectiveModelDir("/bugs/bug5410");

        final PatternConstraint pattern = getPatternConstraintOf(context, "leaf-with-pattern");

        final String rawRegex = pattern.getRegularExpressionString();
        final String expectedYangRegex = "$0$.*|$1$[a-zA-Z0-9./]{1,8}$[a-zA-Z0-9./]{22}|$5$(rounds=\\d+$)?"
            + "[a-zA-Z0-9./]{1,16}$[a-zA-Z0-9./]{43}|$6$(rounds=\\d+$)?[a-zA-Z0-9./]{1,16}$[a-zA-Z0-9./]{86}";
        assertEquals(expectedYangRegex, rawRegex);

        final String javaRegexFromYang = pattern.getJavaPatternString();
        final String expectedJavaRegex = "^(?:\\$0\\$.*|\\$1\\$[a-zA-Z0-9./]{1,8}\\$[a-zA-Z0-9./]{22}|\\$5\\$"
            + "(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{43}|\\$6\\$(rounds=\\d+\\$)?"
            + "[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{86})$";
        assertEquals(expectedJavaRegex, javaRegexFromYang);

        final String value = "$6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/"
            + "00gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//";
        testPattern(javaRegexFromYang, List.of(value), List.of());
    }

    private static PatternConstraint getPatternConstraintOf(final SchemaContext context, final String leafName) {
        final LeafSchemaNode leaf = assertInstanceOf(LeafSchemaNode.class, context.getDataChildByName(foo(leafName)));
        final StringTypeDefinition strType = assertInstanceOf(StringTypeDefinition.class, leaf.getType());
        return strType.getPatternConstraints().iterator().next();
    }


    private static void testPattern(final String javaRegex, final List<String> positiveMatches,
            final List<String> negativeMatches) {
        for (var value : positiveMatches) {
            assertTrue(value.matches(javaRegex), "Value '" + value + "' does not match java regex '" + javaRegex + "'");
        }
        for (var value : negativeMatches) {
            assertFalse(value.matches(javaRegex), "Value '" + value + "' matches java regex '" + javaRegex + "'");
        }
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }
}

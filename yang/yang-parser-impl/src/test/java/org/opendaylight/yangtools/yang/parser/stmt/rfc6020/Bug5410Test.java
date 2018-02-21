/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug5410Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void testYangPattern() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5410");
        assertNotNull(context);

        final PatternConstraint pattern = getPatternConstraintOf(context, "leaf-with-pattern");

        final String rawRegex = pattern.getRawRegularExpression();
        final String expectedYangRegex = "$0$.*|$1$[a-zA-Z0-9./]{1,8}$[a-zA-Z0-9./]{22}|$5$(rounds=\\d+$)?[a-zA-Z0-9./]{1,16}$[a-zA-Z0-9./]{43}|$6$(rounds=\\d+$)?[a-zA-Z0-9./]{1,16}$[a-zA-Z0-9./]{86}";
        assertEquals(expectedYangRegex, rawRegex);

        final String javaRegexFromYang = pattern.getRegularExpression();
        final String expectedJavaRegex = "^(?:\\$0\\$.*|\\$1\\$[a-zA-Z0-9./]{1,8}\\$[a-zA-Z0-9./]{22}|\\$5\\$"
                + "(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{43}|\\$6\\$(rounds=\\d+\\$)?"
                + "[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{86})$";
        assertEquals(expectedJavaRegex, javaRegexFromYang);

        final String value = "$6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/00gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//";
        testPattern(javaRegexFromYang, ImmutableList.of(value), ImmutableList.of());
    }

    private static PatternConstraint getPatternConstraintOf(final SchemaContext context, final String leafName) {
        final DataSchemaNode dataChildByName = context.getDataChildByName(foo(leafName));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) dataChildByName;
        final TypeDefinition<? extends TypeDefinition<?>> type = leaf.getType();
        assertTrue(type instanceof StringTypeDefinition);
        final StringTypeDefinition strType = (StringTypeDefinition) type;
        return strType.getPatternConstraints().iterator().next();
    }


    private static void testPattern(final String javaRegex, final List<String> positiveMatches,
            final List<String> negativeMatches) {
        for (final String value : positiveMatches) {
            assertTrue("Value '" + value + "' does not match java regex '" + javaRegex + "'", value.matches(javaRegex));
        }
        for (final String value : negativeMatches) {
            assertFalse("Value '" + value + "' matches java regex '" + javaRegex + "'", value.matches(javaRegex));
        }
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, FOO_REV, localName);
    }
}

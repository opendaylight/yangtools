/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5410Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void test() throws SourceException, ReactorException, URISyntaxException, IOException,
            YangSyntaxErrorException {
        /*
         * xsd:
         * $0$.*|$1$[a-zA-Z0-9./]{1,8}$[a-zA-Z0-9./]{22}|$5$(rounds=\d+$)?[a
         * -zA-Z0
         * -9./]{1,16}$[a-zA-Z0-9./]{43}|$6$(rounds=\d+$)?[a-zA-Z0-9./]{1,16
         * }$[a-zA-Z0-9./]{86} java:
         *
         * string: $6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/00
         * gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//
         */
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5410");
        assertNotNull(context);

        final String testString = "$6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/00gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//";

        final String anchored = getPattern(context, "anchored");
        testPattern(anchored, testString);

        final String unanchored = getPattern(context, "unanchored");
        testPattern(unanchored, testString);

        final String yangPattern = unanchored.substring(1, unanchored.length() - 1);
        final String expectedJavaPattern = "^\\$0\\$.*|\\$1\\$[a-zA-Z0-9./]{1,8}\\$[a-zA-Z0-9./]{22}|\\$5\\$(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{43}|\\$6\\$(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{86}$";

        final Matcher matcher = Pattern.compile("\\$").matcher(yangPattern);
        assertEquals(expectedJavaPattern, "^" + matcher.replaceAll("\\\\\\$") + "$");
        testPattern(expectedJavaPattern, testString);
    }

    @Test
    public void testCarret() {
        testPattern("^[^:]+$", "abc");
        testPattern("^[$^]+$", "$^");

        testPattern("^[^:]+$", "a:bc");
        testPattern("^[$^]+$", "s$^");
    }

    @Test
    public void testSpecial() {
        testPattern("^\\^\\$$", "^$");
    }

    @Test
    public void testSpecial2() {
        testPattern("^\\^[^a][a^][\\^][$]\\$$", "^b^^$$");
    }


    private void testPattern(final String regex, final String testString) {
        System.out.println("regex:" + regex);
        System.out.println("testString:" + testString);
        System.out.println("matches: " + testString.matches(regex));
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(testString);
        System.out.println("matcher find: " + matcher.find());
    }

    private String getPattern(final SchemaContext context, final String leafName) {
        final DataSchemaNode dataChildByName = context.getDataChildByName(foo(leafName));
        assertTrue(dataChildByName instanceof LeafSchemaNode);
        final LeafSchemaNode leaf = (LeafSchemaNode) dataChildByName;
        final TypeDefinition<? extends TypeDefinition<?>> type = leaf.getType();
        assertTrue(type instanceof StringTypeDefinition);
        final StringTypeDefinition strType = (StringTypeDefinition) type;
        return strType.getPatternConstraints().iterator().next().getRegularExpression();
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, FOO_REV, localName);
    }
}

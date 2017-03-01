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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

public class Bug5410Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void testJavaRegexFromXSD() {
        testPattern("^[^:]+$", "^\\^[^:]+\\$$", ImmutableList.of("^a$", "^abc$"),
                ImmutableList.of("abc$", "^abc", "^a:bc$"));
        testPattern("^[$^]$", "^\\^[$^]\\$$", ImmutableList.of("^^$", "^$$"), ImmutableList.of("^^", "^$", "$^", "$$"));
        testPattern("[$-%]+", "^[$-%]+$", ImmutableList.of("$", "%", "%$"), ImmutableList.of("$-", "$-%", "-", "^"));
        testPattern("[$-&]+", "^[$-&]+$", ImmutableList.of("$", "%&", "%$", "$%&"), ImmutableList.of("#", "$-&", "'"));

        testPattern("[a-z&&[^m-p]]+", "^[a-z&&[^m-p]]+$", ImmutableList.of("a", "z", "az"),
                ImmutableList.of("m", "anz", "o"));
        testPattern("^[\\[-b&&[^^-a]]+$", "^\\^[\\[-b&&[^^-a]]+\\$$", ImmutableList.of("^[$", "^\\$", "^]$", "^b$"),
                ImmutableList.of("^a$", "^^$", "^_$"));

        testPattern("[^^-~&&[^$-^]]", "^[^^-~&&[^$-^]]$", ImmutableList.of("!", "\"", "#"),
                ImmutableList.of("a", "A", "z", "Z", "$", "%", "^", "}"));
        testPattern("\\\\\\[^[^^-~&&[^$-^]]", "^\\\\\\[\\^[^^-~&&[^$-^]]$",
                ImmutableList.of("\\[^ ", "\\[^!", "\\[^\"", "\\[^#"),
                ImmutableList.of("\\[^a", "\\[^A", "\\[^z", "\\[^Z", "\\[^$", "\\[^%", "\\[^^", "\\[^}"));
        testPattern("^\\[^\\\\[^^-b&&[^\\[-\\]]]\\]^", "^\\^\\[\\^\\\\[^^-b&&[^\\[-\\]]]\\]\\^$",
                ImmutableList.of("^[^\\c]^", "^[^\\Z]^"),
                ImmutableList.of("^[^\\[]^", "^[^\\\\]^", "^[^\\]]^", "^[^\\^]^", "^[^\\_]^", "^[^\\b]^"));
    }

    @Test
    public void testJavaPattern() {
        testPattern("^[\\$\\^]+$", ImmutableList.of("$^", "^", "$"), ImmutableList.of("\\", "a"));
        testPattern("^[^\\$-\\^]$", ImmutableList.of("a", "_", "#"), ImmutableList.of("%", "^", "$", "]", "\\"));
    }

    private static void testPattern(final String xsdRegex, final String expectedJavaRegex,
            final List<String> positiveMatches, final List<String> negativeMatches) {
        final String javaRegexFromXSD = javaRegexFromXSD(xsdRegex);
        assertEquals(expectedJavaRegex, javaRegexFromXSD);

        for (final String value : positiveMatches) {
            assertTrue("Value '" + value + "' does not match java regex '" + javaRegexFromXSD + "'",
                    testMatch(javaRegexFromXSD, value));
        }
        for (final String value : negativeMatches) {
            assertFalse("Value '" + value + "' matches java regex '" + javaRegexFromXSD + "'",
                    testMatch(javaRegexFromXSD, value));
        }
    }

    private static void testPattern(final String javaRegex,
            final List<String> positiveMatches, final List<String> negativeMatches) {
        for (final String value : positiveMatches) {
            assertTrue("Value '" + value + "' does not match java regex '" + javaRegex + "'",
                    testMatch(javaRegex, value));
        }
        for (final String value : negativeMatches) {
            assertFalse("Value '" + value + "' matches java regex '" + javaRegex + "'",
                    testMatch(javaRegex, value));
        }
    }

    private static String javaRegexFromXSD(final String xsdRegex) {
        return PatternStatementImpl.Definition.getJavaRegexFromXSD(xsdRegex);
    }

    private static boolean testMatch(final String javaRegex, final String value) {
        return value.matches(javaRegex);
    }

    private static String getPattern(final SchemaContext context, final String leafName) {
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

    // @Test
    // public void test() throws SourceException, ReactorException,
    // URISyntaxException, IOException,
    // YangSyntaxErrorException {
    // /*
    // * xsd:
    // * $0$.*|$1$[a-zA-Z0-9./]{1,8}$[a-zA-Z0-9./]{22}|$5$(rounds=\d+$)?[a
    // * -zA-Z0
    // * -9./]{1,16}$[a-zA-Z0-9./]{43}|$6$(rounds=\d+$)?[a-zA-Z0-9./]{1,16
    // * }$[a-zA-Z0-9./]{86} java:
    // *
    // * string: $6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/00
    // * gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//
    // */
    // final SchemaContext context =
    // StmtTestUtils.parseYangSources("/bugs/bug5410");
    // assertNotNull(context);
    //
    //
    //
    // final String testString =
    // "$6$AnrKGc0V$B/0/A.pWg4HrrA6YiEJOtFGibQ9Fmm5.4rI/00gEz3QeB7joSxBU3YtbHDm6NSkS1dKTQy3BWhwKKDS8nB5S//";
    //
    // final String anchored = getPattern(context, "anchored");
    // testPattern(anchored, testString);
    //
    // final String unanchored = getPattern(context, "unanchored");
    // testPattern(unanchored, testString);
    //
    // final String yangPattern = unanchored.substring(1, unanchored.length() -
    // 1);
    // final String expectedJavaPattern =
    // "^\\$0\\$.*|\\$1\\$[a-zA-Z0-9./]{1,8}\\$[a-zA-Z0-9./]{22}|\\$5\\$(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{43}|\\$6\\$(rounds=\\d+\\$)?[a-zA-Z0-9./]{1,16}\\$[a-zA-Z0-9./]{86}$";
    //
    // final Matcher matcher = Pattern.compile("\\$").matcher(yangPattern);
    // assertEquals(expectedJavaPattern, "^" + matcher.replaceAll("\\\\\\$") +
    // "$");
    // testPattern(expectedJavaPattern, testString);
    // }
    //
    // @Test
    // public void testCarret() {
    // testPattern("^[^:]+$", "abc");
    // testPattern("^[$^]+$", "$^");
    //
    // testPattern("^[^:]+$", "a:bc");
    // testPattern("^[$^]+$", "s$^");
    // }
    //
    // @Test
    // public void testSpecial() {
    // testPattern("^\\^\\$$", "^$");
    // }
    //
    // @Test
    // public void testSpecial2() {
    // testPattern("^\\^[^a][a^][\\^][$]\\$$", "^b^^$$");
    // }

    // private void testPattern(final String regex, final String testString) {
    // System.out.println("regex:" + regex);
    // System.out.println("testString:" + testString);
    // System.out.println("matches: " + testString.matches(regex));
    // final Pattern pattern = Pattern.compile(regex);
    // final Matcher matcher = pattern.matcher(testString);
    // System.out.println("matcher find: " + matcher.find());
    // }

    @Test
    public void testCaret() {
        testPattern("^", "\\^");
    }

    @Test
    public void testTextCaret() {
        testPattern("abc^", "abc\\^");
    }

    @Test
    public void testTextDollar() {
        testPattern("abc$", "abc\\$");
    }

    @Test
    public void testCaretCaret() {
        testPattern("^^", "\\^\\^");
    }

    @Test
    public void testCaretDollar() {
        testPattern("^$", "\\^\\$");
    }

    @Test
    public void testDot() {
        testPattern(".", ".");
    }

    @Test
    public void testNotColon() {
        testPattern("[^:]+", "[^:]+");
    }

    @Test
    public void testDollar() {
        testPattern("$", "\\$");
    }

    @Test
    public void testDollarOneDollar() {
        testPattern("$1$", "\\$1\\$");
    }

    @Test
    public void testDollarPercentRange() {
        testPattern("[$-%]+", "[$-%]+");
    }

    @Test
    public void testDollarRange() {
        testPattern("[$$]+", "[$$]+");
    }

    @Test
    public void testDollarCaretRange() {
        testPattern("[$^]+", "[$^]+");
    }

    @Test
    public void testSimple() {
        testPattern("abc", "abc");
    }

    @Test
    public void testDotPlus() {
        testPattern(".+", ".+");
    }

    @Test
    public void testDotStar() {
        testPattern(".*", ".*");
    }

    @Test
    public void testSimpleOptional() {
        testPattern("a?", "a?");
    }

    @Test
    public void testRangeOptional() {
        testPattern("[a-z]?", "[a-z]?");
    }

    private static void testPattern(final String xsdRegex, final String unanchoredJavaRegex) {
        testPattern(xsdRegex, '^' + unanchoredJavaRegex + '$', ImmutableList.of(), ImmutableList.of());
    }
}

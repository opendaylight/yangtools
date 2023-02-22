/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.DOUBLE_TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.TRIPLE_TAB;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.doubleTab;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.getFiles;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.tab;
import static org.opendaylight.mdsal.binding.java.api.generator.FileSearchUtil.tripleTab;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.binding.contract.Naming;

public class SpecializingLeafrefTest extends BaseCompilationTest {
    private static final ParameterizedType SET_STRING_TYPE  = Types.setTypeFor(Types.STRING);

    public static final String BAR_CONT = "BarCont";
    public static final String BOOLEAN_CONT = "BooleanCont";

    private static final String BAR_LST = "BarLst";
    private static final String BAZ_GRP = "BazGrp";
    private static final String FOO_GRP = "FooGrp";
    private static final String RESOLVED_LEAF_GRP = "ResolvedLeafGrp";
    private static final String RESOLVED_LEAFLIST_GRP = "ResolvedLeafListGrp";
    private static final String TRANSITIVE_GROUP = "TransitiveGroup";
    private static final String UNRESOLVED_GROUPING = "UnresolvedGrouping";

    private static final String GET_LEAF1_NAME = "getLeaf1";
    private static final String GET_LEAFLIST1_NAME = "getLeafList1";

    private static final String GET_LEAF1_TYPE_OBJECT = "    Object getLeaf1();";
    private static final String GET_LEAF1_TYPE_STRING = "    String getLeaf1();";
    private static final String GET_LEAFLIST1_WILDCARD = "    @Nullable Set<?> getLeafList1();";
    private static final String GET_LEAFLIST1_STRING = "    @Nullable Set<String> getLeafList1();";
    private static final String GET_LEAFLIST1_DECLARATION = " getLeafList1();";
    private static final String GET_LEAF1_DECLARATION = " getLeaf1();";

    private static final char CLOSING_METHOD_BRACE = '}';
    private static final String TAB_CLOSING_METHOD_BRACE = TAB + CLOSING_METHOD_BRACE;
    private static final String DTAB_CLOSING_METHOD_BRACE = DOUBLE_TAB + CLOSING_METHOD_BRACE;

    private static final String FOO_GRP_REF = "FooGrp";
    private static final String RESOLVED_LEAF_GRP_REF = "ResolvedLeafGrp";
    private static final String UNRESOLVED_GROUPING_REF = "UnresolvedGrouping";

    private static final String LEAF2_ASSIGNMENT = "this._leaf2 = arg.getLeaf2();";

    private static final String TAB_FIELDS_FROM_SIGNATURE = TAB + "public void fieldsFrom(DataObject arg) {";
    private static final String TTAB_SET_IS_VALID_ARG_TRUE = TRIPLE_TAB + "isValidArg = true;";
    private static final String DTAB_INIT_IS_VALID_ARG_FALSE = DOUBLE_TAB + "boolean isValidArg = false;";

    private File sourcesOutputDir;
    private File compiledOutputDir;
    private List<GeneratedType> types;
    private Map<String, File> files;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        types = generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        files = getFiles(sourcesOutputDir);
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testGroupingWithUnresolvedLeafRefs() throws IOException {
        verifyReturnType(FOO_GRP, GET_LEAF1_NAME, Types.objectType());
        verifyReturnType(FOO_GRP, GET_LEAFLIST1_NAME, Types.setTypeWildcard());

        final String content = getFileContent(FOO_GRP);

        assertThat(content, containsString(GET_LEAF1_TYPE_OBJECT));
        assertThat(content, containsString(GET_LEAFLIST1_WILDCARD));
    }

    @Test
    public void testLeafLeafrefPointsLeaf() throws IOException {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertThat(content, containsString(GET_LEAF1_TYPE_STRING));
    }

    @Test
    public void testLeafLeafrefPointsLeafList() throws IOException {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertThat(content, containsString(GET_LEAF1_TYPE_STRING));
    }

    @Test
    public void testLeafListLeafrefPointsLeaf() throws IOException {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAFLIST1_NAME, SET_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertOverriddenGetter(content, GET_LEAFLIST1_STRING);
    }

    @Test
    public void testLeafListLeafrefPointsLeafList() throws IOException {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAFLIST1_NAME, SET_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAFLIST_GRP);

        assertOverriddenGetter(content, GET_LEAFLIST1_STRING);
    }

    @Test
    public void testGroupingWhichInheritUnresolvedLeafrefAndDoesNotDefineIt() throws IOException {
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAF1_NAME);
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAFLIST1_NAME);

        final String content = getFileContent(TRANSITIVE_GROUP);

        assertThat(content, not(containsString(GET_LEAF1_DECLARATION)));
        assertThat(content, not(containsString(GET_LEAFLIST1_DECLARATION)));
    }

    @Test
    public void testLeafrefWhichPointsBoolean() throws IOException {
        verifyReturnType(UNRESOLVED_GROUPING, GET_LEAF1_NAME, Types.objectType());
        verifyReturnType(BOOLEAN_CONT, GET_LEAF1_NAME, Types.BOOLEAN);

        final String unresolvedGrouping = getFileContent(UNRESOLVED_GROUPING);
        final String booleanCont = getFileContent(BOOLEAN_CONT);

        assertNotOverriddenGetter(unresolvedGrouping, GET_LEAF1_TYPE_OBJECT);
        assertThat(booleanCont, containsString(GET_LEAF1_DECLARATION));
    }

    @Test
    public void testGroupingsUsageWhereLeafrefAlreadyResolved() throws IOException {
        leafList1AndLeaf1Absence(BAR_CONT);
        leafList1AndLeaf1Absence(BAR_LST);
        leafList1AndLeaf1Absence(BAZ_GRP);
    }

    private void leafList1AndLeaf1Absence(final String typeName) throws IOException {
        verifyMethodAbsence(typeName, GET_LEAF1_NAME);
        verifyMethodAbsence(typeName, GET_LEAFLIST1_NAME);

        final String content = getFileContent(typeName);

        assertThat(content, not(containsString(GET_LEAF1_DECLARATION)));
        assertThat(content, not(containsString(GET_LEAFLIST1_DECLARATION)));
    }

    private static void assertNotOverriddenGetter(final String fileContent, final String getterString) {
        assertThat(fileContent, not(containsString("@Override" + System.lineSeparator() + getterString)));
        assertThat(fileContent, containsString(getterString));
    }

    private static void assertOverriddenGetter(final String fileContent, final String getterString) {
        assertThat(fileContent, containsString("@Override" + System.lineSeparator() + getterString));
    }

    @Test
    public void barContBuilderDataObjectTest() throws IOException {
        final File file = files.get(getJavaBuilderFileName(BAR_CONT));
        final String content = Files.readString(file.toPath());

        barContBuilderConstructorResolvedLeafGrpTest(file, content);
        barContBuilderConstructorFooGrpTest(file, content);
        barContBuilderFieldsFromTest(file, content);
    }

    private static void barContBuilderConstructorResolvedLeafGrpTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab("public BarContBuilder(" + RESOLVED_LEAF_GRP_REF + " arg) {"),
                doubleTab("this._leaf1 = arg.getLeaf1();"),
                doubleTab("this._leafList1 = arg.getLeafList1();"),
                doubleTab("this._name = arg.getName();"),
                doubleTab(LEAF2_ASSIGNMENT),
                TAB_CLOSING_METHOD_BRACE);
    }

    private static void barContBuilderFieldsFromTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                TAB_FIELDS_FROM_SIGNATURE,
                DTAB_INIT_IS_VALID_ARG_FALSE,
                doubleTab("if (arg instanceof " + FOO_GRP_REF + " castArg) {"),
                tripleTab("this._leaf1 = CodeHelpers.checkFieldCast(String.class, \"leaf1\", castArg.getLeaf1());"),
                tripleTab("this._leafList1 = CodeHelpers.checkSetFieldCast(String.class, \"leafList1\", "
                    + "castArg.getLeafList1());"),
                tripleTab("this._leaf2 = castArg.getLeaf2();"),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab("if (arg instanceof " + RESOLVED_LEAF_GRP_REF + " castArg) {"),
                tripleTab("this._name = castArg.getName();"),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab("CodeHelpers.validValue(isValidArg, arg, \"[" + FOO_GRP_REF + ", " + RESOLVED_LEAF_GRP_REF
                    + "]\");"),
                TAB_CLOSING_METHOD_BRACE);
    }

    private static void barContBuilderConstructorFooGrpTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab("public BarContBuilder(" + FOO_GRP_REF + " arg) {"),
                doubleTab("this._leaf1 = CodeHelpers.checkFieldCast(String.class, \"leaf1\", "
                    + "arg.getLeaf1());"),
                doubleTab("this._leafList1 = CodeHelpers.checkSetFieldCast(String.class, \"leafList1\", "
                    + "arg.getLeafList1());"),
                doubleTab(LEAF2_ASSIGNMENT),
                TAB_CLOSING_METHOD_BRACE);
    }

    @Test
    public void booleanContBuilderDataObjectTest() throws IOException {
        final File file = files.get(getJavaBuilderFileName(BOOLEAN_CONT));
        final String content = Files.readString(file.toPath());

        booleanContBuilderFieldsFromTest(file, content);
        booleanContBuilderConstructorTest(file, content);
    }

    private static void booleanContBuilderFieldsFromTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                TAB_FIELDS_FROM_SIGNATURE,
                DTAB_INIT_IS_VALID_ARG_FALSE,
                doubleTab("if (arg instanceof " + UNRESOLVED_GROUPING_REF + " castArg) {"),
                tripleTab("this._leaf1 = CodeHelpers.checkFieldCast(Boolean.class, \"leaf1\", castArg.getLeaf1());"),
                TTAB_SET_IS_VALID_ARG_TRUE,
                DTAB_CLOSING_METHOD_BRACE,
                doubleTab("CodeHelpers.validValue(isValidArg, arg, \"[" + UNRESOLVED_GROUPING_REF + "]\");"),
                TAB_CLOSING_METHOD_BRACE);
    }

    private static void booleanContBuilderConstructorTest(final File file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
                tab("public BooleanContBuilder(" + UNRESOLVED_GROUPING_REF + " arg) {"),
                doubleTab("this._leaf1 = CodeHelpers.checkFieldCast(Boolean.class, \"leaf1\", "
                    + "arg.getLeaf1());"),
                TAB_CLOSING_METHOD_BRACE);
    }

    private static String getJavaFileName(final String name) {
        return name + ".java";
    }

    private static String getJavaBuilderFileName(final String name) {
        return getJavaFileName(name + Naming.BUILDER_SUFFIX);
    }

    private String getFileContent(final String fileName) throws IOException {
        final File file = files.get(getJavaFileName(fileName));
        assertNotNull(Files.lines(file.toPath()).findFirst());
        final String content = Files.readString(Path.of(file.getAbsolutePath()));
        assertNotNull(content);
        return content;
    }

    private void verifyMethodAbsence(final String typeName, final String getterName) {
        verifyReturnType(typeName, getterName, null);
    }

    private void verifyReturnType(final String typeName, final String getterName, final Type returnType) {
        final GeneratedType generated = typeByName(types, typeName);
        assertNotNull(generated);
        assertEquals(returnType, returnTypeByMethodName(generated, getterName));
    }

    private static GeneratedType typeByName(final List<GeneratedType> types, final String name) {
        for (final GeneratedType type : types) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    private static Type returnTypeByMethodName(final GeneratedType type, final String name) {
        for (final MethodSignature m : type.getMethodDefinitions()) {
            if (m.getName().equals(name)) {
                return m.getReturnType();
            }
        }
        return null;
    }
}

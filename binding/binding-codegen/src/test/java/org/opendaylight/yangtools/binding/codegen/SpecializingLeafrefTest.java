/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.binding.codegen.FileSearchUtil.getFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.Types;

class SpecializingLeafrefTest extends BaseCompilationTest {
    private static final ParameterizedType SET_STRING_TYPE  = Types.setTypeFor(Types.STRING);

    private static final String BAR_CONT = "BarCont";
    private static final String BOOLEAN_CONT = "BooleanCont";

    private static final String RESOLVED_LEAF_GRP = "ResolvedLeafGrp";
    private static final String RESOLVED_LEAFLIST_GRP = "ResolvedLeafListGrp";
    private static final String TRANSITIVE_GROUP = "TransitiveGroup";
    private static final String UNRESOLVED_GROUPING = "UnresolvedGrouping";

    private static final String GET_LEAF1_NAME = "getLeaf1";
    private static final String GET_LEAFLIST1_NAME = "getLeafList1";

    private static final String GET_LEAF1_TYPE_OBJECT = "    Object getLeaf1();";
    private static final String GET_LEAF1_TYPE_STRING = "    String getLeaf1();";
    private static final String GET_LEAFLIST1_STRING = "    @Nullable Set<String> getLeafList1();";
    private static final String GET_LEAFLIST1_DECLARATION = " getLeafList1();";
    private static final String GET_LEAF1_DECLARATION = " getLeaf1();";

    private static Path sourcesOutputDir;
    private static Path compiledOutputDir;
    private static List<Archetype> types;
    private static Map<String, Path> files;

    @BeforeAll
    static void before() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal426");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal426");
        types = generateTestSources("/compilation/mdsal426", sourcesOutputDir);
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
        files = getFiles(sourcesOutputDir);
    }

    @AfterAll
    static void after() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
        sourcesOutputDir = null;
        compiledOutputDir = null;
        types = null;
        files = null;
    }

    @Test
    void testGroupingWithUnresolvedLeafRefs() throws Exception {
        verifyReturnType("FooGrp", GET_LEAF1_NAME, Types.objectType());
        verifyReturnType("FooGrp", GET_LEAFLIST1_NAME, Types.setTypeWildcard());

        final String content = getFileContent("FooGrp");

        assertThat(content).contains(GET_LEAF1_TYPE_OBJECT);
        assertThat(content).contains("    @Nullable Set<?> getLeafList1();");
    }

    @Test
    void testLeafLeafrefPointsLeaf() throws Exception {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertThat(content).contains(GET_LEAF1_TYPE_STRING);
    }

    @Test
    void testLeafLeafrefPointsLeafList() throws Exception {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAF1_NAME, Types.STRING);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertThat(content).contains(GET_LEAF1_TYPE_STRING);
    }

    @Test
    void testLeafListLeafrefPointsLeaf() throws Exception {
        verifyReturnType(RESOLVED_LEAF_GRP, GET_LEAFLIST1_NAME, SET_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAF_GRP);

        assertOverriddenGetter(content, GET_LEAFLIST1_STRING);
    }

    @Test
    void testLeafListLeafrefPointsLeafList() throws Exception {
        verifyReturnType(RESOLVED_LEAFLIST_GRP, GET_LEAFLIST1_NAME, SET_STRING_TYPE);

        final String content = getFileContent(RESOLVED_LEAFLIST_GRP);

        assertOverriddenGetter(content, GET_LEAFLIST1_STRING);
    }

    @Test
    void testGroupingWhichInheritUnresolvedLeafrefAndDoesNotDefineIt() throws Exception {
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAF1_NAME);
        verifyMethodAbsence(TRANSITIVE_GROUP, GET_LEAFLIST1_NAME);

        final String content = getFileContent(TRANSITIVE_GROUP);

        assertThat(content).doesNotContain(GET_LEAF1_DECLARATION);
        assertThat(content).doesNotContain(GET_LEAFLIST1_DECLARATION);
    }

    @Test
    void testLeafrefWhichPointsBoolean() throws Exception {
        verifyReturnType(UNRESOLVED_GROUPING, GET_LEAF1_NAME, Types.objectType());
        verifyReturnType(BOOLEAN_CONT, GET_LEAF1_NAME, Types.BOOLEAN);

        final String unresolvedGrouping = getFileContent(UNRESOLVED_GROUPING);
        final String booleanCont = getFileContent(BOOLEAN_CONT);

        assertNotOverriddenGetter(unresolvedGrouping, GET_LEAF1_TYPE_OBJECT);
        assertThat(booleanCont).contains(GET_LEAF1_DECLARATION);
    }

    @Test
    void testGroupingsUsageWhereLeafrefAlreadyResolved() throws Exception {
        leafList1AndLeaf1Absence(BAR_CONT);
        leafList1AndLeaf1Absence("BarLst");
        leafList1AndLeaf1Absence("BazGrp");
    }

    private static void leafList1AndLeaf1Absence(final String typeName) throws Exception {
        verifyMethodAbsence(typeName, GET_LEAF1_NAME);
        verifyMethodAbsence(typeName, GET_LEAFLIST1_NAME);

        final String content = getFileContent(typeName);

        assertThat(content).doesNotContain(GET_LEAF1_DECLARATION);
        assertThat(content).doesNotContain(GET_LEAFLIST1_DECLARATION);
    }

    private static void assertNotOverriddenGetter(final String fileContent, final String getterString) {
        assertThat(fileContent).doesNotContain("@Override\n" + getterString);
        assertThat(fileContent).contains(getterString);
    }

    private static void assertOverriddenGetter(final String fileContent, final String getterString) {
        assertThat(fileContent).contains("@Override\n" + getterString);
    }

    @Test
    void barContBuilderDataObjectTest() throws Exception {
        final var file = files.get(getJavaBuilderFileName(BAR_CONT));
        final String content = Files.readString(file);

        barContBuilderConstructorResolvedLeafGrpTest(file, content);
        barContBuilderConstructorFooGrpTest(file, content);
        barContBuilderFieldsFromTest(file, content);
    }

    private static void barContBuilderConstructorResolvedLeafGrpTest(final Path file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
            "    public BarContBuilder(ResolvedLeafGrp arg) {",
            "        this._leaf1 = arg.getLeaf1();",
            "        this._leafList1 = arg.getLeafList1();",
            "        this._name = arg.getName();",
            "        this._leaf2 = arg.getLeaf2();",
            "    }");
    }

    private static void barContBuilderFieldsFromTest(final Path file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
            "    public void fieldsFrom(final Grouping arg) {",
            "        boolean isValidArg = false;",
            "        if (arg instanceof FooGrp castArg) {",
            "            this._leaf1 = CodeHelpers.checkFieldCast(String.class, \"leaf1\", castArg.getLeaf1());",
            "            this._leafList1 = CodeHelpers.checkSetFieldCast(String.class, \"leafList1\", "
                + "castArg.getLeafList1());",
            "            this._leaf2 = castArg.getLeaf2();",
            "            isValidArg = true;",
            "        }",
            "        if (arg instanceof ResolvedLeafGrp castArg) {",
            "            this._name = castArg.getName();",
            "            isValidArg = true;",
            "        }",
            "        CodeHelpers.validValue(isValidArg, arg, \"[FooGrp, ResolvedLeafGrp]\");",
            "    }");
    }

    private static void barContBuilderConstructorFooGrpTest(final Path file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
            "    public BarContBuilder(FooGrp arg) {",
            "        this._leaf1 = CodeHelpers.checkFieldCast(String.class, \"leaf1\", arg.getLeaf1());",
            "        this._leafList1 = CodeHelpers.checkSetFieldCast(String.class, \"leafList1\", arg.getLeafList1());",
            "        this._leaf2 = arg.getLeaf2();",
            "    }");
    }

    @Test
    void booleanContBuilderDataObjectTest() throws Exception {
        final var file = files.get(getJavaBuilderFileName(BOOLEAN_CONT));
        final var content = Files.readString(file);

        booleanContBuilderFieldsFromTest(file, content);
        booleanContBuilderConstructorTest(file, content);
    }

    private static void booleanContBuilderFieldsFromTest(final Path file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
            "    public void fieldsFrom(final Grouping arg) {",
            "        boolean isValidArg = false;",
            "        if (arg instanceof UnresolvedGrouping castArg) {",
            "            this._leaf1 = CodeHelpers.checkFieldCast(Boolean.class, \"leaf1\", castArg.getLeaf1());",
            "            isValidArg = true;",
            "        }",
            "        CodeHelpers.validValue(isValidArg, arg, \"[UnresolvedGrouping]\");",
            "    }");
    }

    private static void booleanContBuilderConstructorTest(final Path file, final String content) {
        FileSearchUtil.assertFileContainsConsecutiveLines(file, content,
            "    public BooleanContBuilder(UnresolvedGrouping arg) {",
            "        this._leaf1 = CodeHelpers.checkFieldCast(Boolean.class, \"leaf1\", arg.getLeaf1());",
            "    }");
    }

    private static String getJavaFileName(final String name) {
        return name + ".java";
    }

    private static String getJavaBuilderFileName(final String name) {
        return getJavaFileName(name + Naming.BUILDER_SUFFIX);
    }

    private static String getFileContent(final String fileName) throws Exception {
        final var file = files.get(getJavaFileName(fileName));
        try (var stream = Files.lines(file)) {
            assertTrue(stream.findFirst().isPresent());
        }
        final var content = Files.readString(file.toAbsolutePath());
        assertNotNull(content);
        return content;
    }

    private static void verifyMethodAbsence(final String typeName, final String getterName) {
        verifyReturnType(typeName, getterName, null);
    }

    private static void verifyReturnType(final String typeName, final String getterName, final Type returnType) {
        final var generated = typeByName(typeName);
        assertNotNull(generated);
        assertEquals(returnType, returnTypeByMethodName(generated, getterName));
    }

    private static LegacyArchetype typeByName(final String name) {
        for (var type : types) {
            if (type.simpleName().equals(name)) {
                return assertInstanceOf(LegacyArchetype.class, type);
            }
        }
        return null;
    }

    private static Type returnTypeByMethodName(final LegacyArchetype type, final String name) {
        for (var m : type.getMethodDefinitions()) {
            if (m.getName().equals(name)) {
                return m.getReturnType();
            }
        }
        return null;
    }
}

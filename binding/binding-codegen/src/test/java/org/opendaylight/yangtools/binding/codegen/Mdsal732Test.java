/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Mdsal732Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void before() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal732");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal732");
    }

    @AfterEach
    void after() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testIdentityrefLeafrefSpecialization() throws Exception {
        generateTestSources("/compilation/mdsal732", sourcesOutputDir);
        final var xyzzyBuilder = FileSearchUtil.getFiles(sourcesOutputDir).get("XyzzyBuilder.java");
        assertNotNull(xyzzyBuilder);

        final var content = Files.readString(xyzzyBuilder);
        FileSearchUtil.assertFileContainsConsecutiveLines(xyzzyBuilder, content,
            "    public XyzzyBuilder(Grp arg) {",
            "        this._foo = CodeHelpers.checkFieldCast(Foo.class, \"foo\", arg.getFoo());",
            "        this._bar = CodeHelpers.checkSetFieldCast(Foo.class, \"bar\", arg.getBar());",
            "        this._baz = CodeHelpers.checkListFieldCast(Foo.class, \"baz\", arg.getBaz());",
            "    }");
        FileSearchUtil.assertFileContainsConsecutiveLines(xyzzyBuilder, content,
            "    public void fieldsFrom(final Grouping arg) {",
            "        boolean isValidArg = false;",
            "        if (arg instanceof Grp castArg) {",
            "            this._foo = CodeHelpers.checkFieldCast(Foo.class, \"foo\", castArg.getFoo());",
            "            this._bar = CodeHelpers.checkSetFieldCast(Foo.class, \"bar\", castArg.getBar());",
            "            this._baz = CodeHelpers.checkListFieldCast(Foo.class, \"baz\", castArg.getBaz());",
            "            isValidArg = true;",
            "        }",
            "        CodeHelpers.validValue(isValidArg, arg, \"[Grp]\");",
            "    }");

        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}

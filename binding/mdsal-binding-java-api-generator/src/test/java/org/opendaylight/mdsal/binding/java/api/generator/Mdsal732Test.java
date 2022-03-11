/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Mdsal732Test extends BaseCompilationTest {
    private File sourcesOutputDir;
    private File compiledOutputDir;

    @Before
    public void before() throws IOException, URISyntaxException {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal732");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal732");
    }

    @After
    public void after() {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    public void testIdentityrefLeafrefSpecialization() throws IOException, URISyntaxException {
        generateTestSources("/compilation/mdsal732", sourcesOutputDir);
        final var xyzzyBuilder = FileSearchUtil.getFiles(sourcesOutputDir).get("XyzzyBuilder.java");
        assertNotNull(xyzzyBuilder);

        final var content = Files.readString(xyzzyBuilder.toPath());
        FileSearchUtil.assertFileContainsConsecutiveLines(xyzzyBuilder, content,
            "    public XyzzyBuilder(Grp arg) {",
            "        this._foo = CodeHelpers.checkFieldCastIdentity(Foo.class, \"foo\", arg.getFoo());",
            "        this._bar = CodeHelpers.checkSetFieldCastIdentity(Foo.class, \"bar\", arg.getBar());",
            "        this._baz = CodeHelpers.checkListFieldCastIdentity(Foo.class, \"baz\", arg.getBaz());",
            "    }");
        FileSearchUtil.assertFileContainsConsecutiveLines(xyzzyBuilder, content,
            "    public void fieldsFrom(DataObject arg) {",
            "        boolean isValidArg = false;",
            "        if (arg instanceof Grp) {",
            "            this._foo = CodeHelpers.checkFieldCastIdentity(Foo.class, \"foo\", ((Grp)arg).getFoo());",
            "            this._bar = CodeHelpers.checkSetFieldCastIdentity(Foo.class, \"bar\", ((Grp)arg).getBar());",
            "            this._baz = CodeHelpers.checkListFieldCastIdentity(Foo.class, \"baz\", ((Grp)arg).getBaz());",
            "            isValidArg = true;",
            "        }",
            "        CodeHelpers.validValue(isValidArg, arg, \"[Grp]\");",
            "    }");

        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}

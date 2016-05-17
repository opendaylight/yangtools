/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.COMPILER_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.FS;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.GENERATOR_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.getSourceFiles;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.testCompilation;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class UnionTypedefUnusedImportTest extends BaseCompilationTest {

    @Test
    public void testUnionTypedefUnusedImport() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "union-typedef");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "union-typedef");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/union-typedef");
        final SchemaContext context = RetestUtils.parseYangSources(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);
        final boolean isUsedImport = containsImport("org.opendaylight.yang.gen.v1.org.opendaylight.yangtools.union.typedef.rev130208.TypedefUnion");
        assertFalse(String.format("Class shouldn't contain import for this type '%s'", types.get(1).getName()),
                isUsedImport);

        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    private static String readFile(final String path, final Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static boolean containsImport(final String fullImport) throws URISyntaxException, IOException {
        final String filePath = GENERATOR_OUTPUT_PATH + FS + "union-typedef" + FS + "org" + FS + "opendaylight" + FS + "yang" + FS + "gen" + FS + "v1" + FS + "org" + FS + "opendaylight" + FS + "yangtools" + FS + "union" + FS + "typedef" + FS + "rev141124" + FS + "TypedefUnionBuilder.java";
        final String fileContent = readFile(filePath, StandardCharsets.UTF_8);

        if (fileContent.contains(fullImport)) {
            return true;
        }
        return false;
    }
}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator.test;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.COMPILER_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.FS;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.GENERATOR_OUTPUT_PATH;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.yangtools.sal.java.api.generator.test.CompilationTestUtils.getSourceFiles;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.java.api.generator.GeneratorJavaFile;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class JavaDocGenerationTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws Exception {
        final File sourcesOutputDir = new File(GENERATOR_OUTPUT_PATH + FS + "javadoc-gen");
        assertTrue("Failed to create test file '" + sourcesOutputDir + "'", sourcesOutputDir.mkdir());
        final File compiledOutputDir = new File(COMPILER_OUTPUT_PATH + FS + "javadoc-gen");
        assertTrue("Failed to create test file '" + compiledOutputDir + "'", compiledOutputDir.mkdir());

        final List<File> sourceFiles = getSourceFiles("/compilation/javadoc-gen");
        final SchemaContext context = parser.parseFiles(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(new HashSet<>(types));
        generator.generateToFile(sourcesOutputDir);

        cleanUp(sourcesOutputDir, compiledOutputDir); 
    }
}

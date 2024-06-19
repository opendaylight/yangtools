/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.cleanUp;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.getSourceFiles;
import static org.opendaylight.mdsal.binding.java.api.generator.test.CompilationTestUtils.testCompilation;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test if generated classes from yang file is compilable, generated javadoc comments contains
 * symbols as javadoc comment tag, which caused of compilation problem.
 */
public class EncodingInJavaDocTest extends BaseCompilationTest {

    @Test
    public void testAugmentToUsesInAugment() throws Exception {
        final File sourcesOutputDir = CompilationTestUtils.generatorOutput("encoding-javadoc");
        final File compiledOutputDir = CompilationTestUtils.compilerOutput("encoding-javadoc");
        final List<File> sourceFiles = getSourceFiles("/compilation/encoding-javadoc");
        final SchemaContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);

        // Test if sources are compilable
        testCompilation(sourcesOutputDir, compiledOutputDir);
        cleanUp(sourcesOutputDir, compiledOutputDir);
    }

}

/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator.test;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.mdsal.binding.java.api.generator.GeneratorJavaFile;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public abstract class BaseCompilationTest {

    protected BindingGenerator bindingGenerator;

    @BeforeClass
    public static void createTestDirs() {
        if (CompilationTestUtils.TEST_DIR.exists()) {
            CompilationTestUtils.deleteTestDir(CompilationTestUtils.TEST_DIR);
        }
        assertTrue(CompilationTestUtils.GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(CompilationTestUtils.COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Before
    public void init() {
        bindingGenerator = new BindingGeneratorImpl();
    }

    protected final void generateTestSources(final String resourceDirPath, final File sourcesOutputDir)
            throws IOException, URISyntaxException {
        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        final SchemaContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final List<Type> types = bindingGenerator.generateTypes(context);
        Collections.sort(types, (o1, o2) -> o2.getName().compareTo(o1.getName()));
        final GeneratorJavaFile generator = new GeneratorJavaFile(ImmutableSet.copyOf(types));
        generator.generateToFile(sourcesOutputDir);
    }
}

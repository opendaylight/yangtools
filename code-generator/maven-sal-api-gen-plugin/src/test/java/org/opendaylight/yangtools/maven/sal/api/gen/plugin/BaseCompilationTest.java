/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.maven.sal.api.gen.plugin;

import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.maven.sal.api.gen.plugin.CompilationTestUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

public abstract class BaseCompilationTest {

    protected YangParserImpl parser;
    protected BindingGenerator bindingGenerator;

    @BeforeClass
    public static void createTestDirs() {
        if (TEST_DIR.exists()) {
            deleteTestDir(TEST_DIR);
        }
        assertTrue(GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(COMPILER_OUTPUT_DIR.mkdirs());
    }

    @Before
    public void init() {
        parser = new YangParserImpl();
        bindingGenerator = new BindingGeneratorImpl();
    }

    protected void generateTestSources(SchemaContext context, Set<Module> modulesToBuild, File sourcesOutputDir) throws IOException {
        CodeGeneratorImpl cg = new CodeGeneratorImpl();
        Map<String, String> additionalConfig = new HashMap<>();
        additionalConfig.put("persistentSourcesDir", sourcesOutputDir.getAbsolutePath());
        cg.setAdditionalConfig(additionalConfig);
        cg.setBuildContext(new DefaultBuildContext());
        cg.generateSources(context, sourcesOutputDir, modulesToBuild);
    }

}

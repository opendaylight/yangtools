/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.io.Resources;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

public class GenerateSourcesTest extends AbstractCodeGeneratorTest {
    private YangToSourcesMojo mojo;
    private File outDir;

    @Before
    public void setUp() throws Exception {
        outDir = new File("/outputDir");
        mojo = setupMojo(new YangToSourcesProcessor(
            new File(Resources.getResource(GenerateSourcesTest.class, "/yang").toURI()), List.of(),
            List.of(new FileGeneratorArg(GeneratorMock.class.getSimpleName())), project, false, yangProvider));
    }

    @Test
    public void test() throws Exception {
        mojo.execute();
        assertEquals(outDir, GeneratorMock.outputDir);
        assertTrue(GeneratorMock.additionalCfg.isEmpty());
        assertThat(GeneratorMock.resourceBaseDir.toString(), containsString("target" + File.separator
                + "generated-sources" + File.separator + "spi"));
    }

    public static class GeneratorMock implements FileGenerator {
        private static int called = 0;
        private static File outputDir;
        private static Map<String, String> additionalCfg;
        private static File resourceBaseDir;

        public GeneratorMock(final Map<String, String> additionalConfiguration) {
            GeneratorMock.additionalCfg = additionalConfiguration;
        }

        @Override
        public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(
            final EffectiveModelContext context, final Set<Module> localModules,
            final ModuleResourceResolver moduleResourcePathResolver) {
            called++;
            return ImmutableTable.of();
        }
    }
}

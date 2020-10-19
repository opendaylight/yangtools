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

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.opendaylight.yangtools.yang2sources.spi.ModuleResourceResolver;

public class GenerateSourcesTest extends AbstractCodeGeneratorTest {
    private YangToSourcesMojo mojo;
    private File outDir;

    @Before
    public void setUp() throws Exception {
        outDir = new File("/outputDir");
        this.mojo = setupMojo(new YangToSourcesProcessor(
            new File(Resources.getResource(GenerateSourcesTest.class, "/yang").toURI()), List.of(),
            List.of(new CodeGeneratorArg(GeneratorMock.class.getName(), "outputDir")), this.project, false,
            yangProvider));
    }

    @Test
    public void test() throws Exception {
        mojo.execute();
        assertEquals(outDir, GeneratorMock.outputDir);
        assertEquals(project, GeneratorMock.project);
        assertTrue(GeneratorMock.additionalCfg.isEmpty());
        assertThat(GeneratorMock.resourceBaseDir.toString(), containsString("target" + File.separator
                + "generated-sources" + File.separator + "spi"));
    }

    public static class GeneratorMock implements BasicCodeGenerator, MavenProjectAware {
        private static int called = 0;
        private static File outputDir;
        private static Map<String, String> additionalCfg;
        private static File resourceBaseDir;
        private static MavenProject project;

        @Override
        public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
            GeneratorMock.additionalCfg = additionalConfiguration;
        }

        @Override
        public void setResourceBaseDir(final File resourceBaseDir) {
            GeneratorMock.resourceBaseDir = resourceBaseDir;

        }

        @Override
        public void setMavenProject(final MavenProject mavenProject) {
            GeneratorMock.project = mavenProject;
        }

        @Override
        public Collection<File> generateSources(final EffectiveModelContext context, final File outputBaseDir,
                final Set<Module> currentModules, final ModuleResourceResolver moduleResourcePathResolver)
                        throws IOException {
            called++;
            outputDir = outputBaseDir;
            return new ArrayList<>();
        }
    }
}

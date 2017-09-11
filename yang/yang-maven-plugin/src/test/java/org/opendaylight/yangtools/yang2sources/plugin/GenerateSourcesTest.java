/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;

public class GenerateSourcesTest {

    private String yang;
    private YangToSourcesMojo mojo;
    private File outDir;

    @Mock
    private MavenProject project;

    @Mock
    private Build build;

    @Mock
    private Plugin plugin;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        yang = new File(getClass().getResource("/yang/mock.yang").toURI()).getParent();
        outDir = new File("/outputDir");
        final YangProvider mock = mock(YangProvider.class);
        doNothing().when(mock).addYangsToMetaInf(any(MavenProject.class), any(Collection.class));

        final YangToSourcesProcessor processor = new YangToSourcesProcessor(new File(this.yang), ImmutableList.of(),
                ImmutableList.of(new CodeGeneratorArg(GeneratorMock.class.getName(), "outputDir")), this.project, false,
                mock);
        this.mojo = new YangToSourcesMojo(processor);
        doReturn(new File("")).when(this.project).getBasedir();
        doReturn("target/").when(this.build).getDirectory();
        doReturn(this.build).when(this.project).getBuild();
        doReturn(Collections.emptyList()).when(this.plugin).getDependencies();
        doReturn(this.plugin).when(this.project).getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        this.mojo.setProject(this.project);
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
        public void setMavenProject(final MavenProject project) {
            GeneratorMock.project = project;
        }

        @Override
        public Collection<File> generateSources(SchemaContext context, File outputBaseDir, Set<Module> currentModules,
                Function<Module, Optional<String>> moduleResourcePathResolver) throws IOException {
            called++;
            outputDir = outputBaseDir;
            return Lists.newArrayList();
        }
    }

}

/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang2sources.plugin.GenerateSourcesTest.GeneratorMock;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

@RunWith(MockitoJUnitRunner.class)
public class YangToSourcesMojoTest {

    private YangToSourcesMojo mojo;

    @Mock
    private MavenProject project;

    @Mock
    private Plugin plugin;

    private YangToSourcesProcessor proc;

    @Test
    public void yangToSourceMojoTest() throws Exception {
        doReturn(plugin).when(project).getPlugin(YangToSourcesMojo.PLUGIN_NAME);

        mojo = new YangToSourcesMojo();
        mojo.setProject(project);
        mojo.buildContext = new DefaultBuildContext();
        mojo.execute();
        assertNotNull(mojo);

        final YangToSourcesProcessor processor = Mockito.mock(YangToSourcesProcessor.class);
        mojo = new YangToSourcesMojo(processor);
        mojo.setProject(project);
        mojo.execute();
        verify(processor).conditionalExecute(false);
    }

    @Test
    public void test() throws Exception {
        prepareProcessor();
        assertNotNull(proc);
        mojo = new YangToSourcesMojo(proc);
        mojo.setProject(project);
        mojo.execute();
        assertNotNull(mojo);
    }

    private void prepareProcessor() {
        final File file = new File(getClass().getResource("/yang").getFile());
        final File excludedYang = new File(getClass().getResource("/yang/excluded-file.yang").getFile());
        final Build build = new Build();
        build.setDirectory("testDir");
//        doReturn(build).when(project).getBuild();
        final boolean dependencies = true;
        proc = new YangToSourcesProcessor(file, List.of(excludedYang),
            List.of(new FileGeneratorArg(GeneratorMock.class.getSimpleName())), project, dependencies,
            YangProvider.getInstance());
    }
}

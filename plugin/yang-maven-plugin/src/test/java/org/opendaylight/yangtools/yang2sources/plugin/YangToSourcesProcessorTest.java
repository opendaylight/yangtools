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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang2sources.plugin.GenerateSourcesTest.GeneratorMock;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class YangToSourcesProcessorTest {
    @Mock
    public File buildContext;
    @Mock
    public MavenProject project;
    @Mock
    public YangProvider inspectDependencies;

    private final boolean dep = false;
    private List<File> yangFilesRootDir;

    @Before
    public void before() {
        yangFilesRootDir = ImmutableList.of(buildContext);
    }

    @Test
    public void yangToSourcesProcessotTest() {
        YangToSourcesProcessor processor = new YangToSourcesProcessor(buildContext, yangFilesRootDir,
            ImmutableList.of(), project, dep, inspectDependencies);
        assertNotNull(processor);

        processor = new YangToSourcesProcessor(buildContext, yangFilesRootDir, ImmutableList.of(), project, dep,
            inspectDependencies);
        assertNotNull(processor);
    }

    @Test
    public void test() throws Exception {
        final File file = new File(getClass().getResource("/yang").getFile());
        final File excludedYang = new File(getClass().getResource("/yang/excluded-file.yang").getFile());
        final Build build = new Build();
        build.setDirectory("foo");
        doReturn(build).when(project).getBuild();
        final boolean dependencies = true;
        final YangToSourcesProcessor proc = new YangToSourcesProcessor(file, List.of(excludedYang),
            List.of(new FileGeneratorArg(GeneratorMock.class.getSimpleName())), project, dependencies,
            YangProvider.getInstance());
        assertNotNull(proc);
        proc.execute();
    }
}

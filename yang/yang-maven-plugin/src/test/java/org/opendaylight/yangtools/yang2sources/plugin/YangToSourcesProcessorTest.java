/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.plugin.GenerateSourcesTest.GeneratorMock;

@RunWith(MockitoJUnitRunner.class)
public class YangToSourcesProcessorTest {

    private final File buildContext = Mockito.mock(File.class);
    private final List<File> yangFilesRootDir = ImmutableList.of(buildContext);
    private final MavenProject project = Mockito.mock(MavenProject.class);
    private final boolean dep = false;
    private final YangProvider inspectDependencies = Mockito.mock(YangProvider.class);

    @Test
    public void yangToSourcesProcessotTest() {
        Mockito.when(this.buildContext.getPath()).thenReturn("path");
        YangToSourcesProcessor processor = new YangToSourcesProcessor(buildContext, yangFilesRootDir,
            ImmutableList.of(), project, dep, inspectDependencies);
        Assert.assertNotNull(processor);

        processor = new YangToSourcesProcessor(buildContext, yangFilesRootDir, ImmutableList.of(), project, dep,
            inspectDependencies);
        Assert.assertNotNull(processor);
    }

    @Test
    public void test() throws Exception {
        final File file = new File(getClass().getResource("/yang").getFile());
        final File excludedYang = new File(getClass().getResource("/yang/excluded-file.yang").getFile());
        final String path = file.getPath();
        final CodeGeneratorArg codeGeneratorArg = new CodeGeneratorArg(GeneratorMock.class.getName(),
                "target/YangToSourcesProcessorTest-outputBaseDir");
        final List<CodeGeneratorArg> codeGenerators = ImmutableList.of(codeGeneratorArg);
        final MavenProject mvnProject = Mockito.mock(MavenProject.class);
        final Build build = new Build();
        Mockito.when(mvnProject.getBuild()).thenReturn(build);
        final boolean dependencies = true;
        final YangToSourcesProcessor proc = new YangToSourcesProcessor(file, ImmutableList.of(excludedYang),
            codeGenerators, mvnProject, dependencies, YangProvider.getInstance());
        Assert.assertNotNull(proc);
        proc.execute();
    }

}

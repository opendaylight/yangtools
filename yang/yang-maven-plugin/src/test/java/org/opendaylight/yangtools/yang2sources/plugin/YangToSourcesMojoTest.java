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
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.plugin.GenerateSourcesTest.GeneratorMock;

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
        Mockito.when(this.project.getPlugin(YangToSourcesMojo.PLUGIN_NAME)).thenReturn(this.plugin);

        this.mojo = new YangToSourcesMojo();
        this.mojo.setProject(this.project);
        this.mojo.execute();
        Assert.assertNotNull(this.mojo);

        final YangToSourcesProcessor processor = Mockito.mock(YangToSourcesProcessor.class);
        this.mojo = new YangToSourcesMojo(processor);
        this.mojo.setProject(this.project);
        this.mojo.execute();
        Mockito.verify(processor).conditionalExecute(false);
    }

    @Test
    public void test() throws Exception {
        prepareProcessor();
        Assert.assertNotNull(this.proc);
        this.mojo = new YangToSourcesMojo(this.proc);
        this.mojo.setProject(this.project);
        this.mojo.execute();
        Assert.assertNotNull(this.mojo);
    }

    private void prepareProcessor() {
        final File file = new File(getClass().getResource("/yang").getFile());
        final File excludedYang = new File(getClass().getResource("/yang/excluded-file.yang").getFile());
        final String path = file.getPath();
        final List<CodeGeneratorArg> codeGenerators = new ArrayList<>();
        final CodeGeneratorArg codeGeneratorArg = new CodeGeneratorArg(GeneratorMock.class.getName(),
                "target/YangToSourcesMojoTest-outputBaseDir");
        codeGenerators.add(codeGeneratorArg);
        final MavenProject mvnProject = Mockito.mock(MavenProject.class);
        final Build build = new Build();
        Mockito.when(mvnProject.getBuild()).thenReturn(build);
        final boolean dependencies = true;
        this.proc = new YangToSourcesProcessor(file, ImmutableList.of(excludedYang), codeGenerators,
                mvnProject, dependencies, new YangProvider());
    }
}

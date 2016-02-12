/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.plugin.GenerateSourcesTest.GeneratorMock;
import org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.YangProvider;

public class YangToSourcesProcessorTest {

    @Test
    public void test() throws Exception {
        final File file = new File(getClass().getResource("/yang").getFile());
        final File excludedYang = new File(getClass().getResource("/yang/excluded-file.yang").getFile());
        final String path = file.getPath();
        final File[] yangFilesRootDir = { excludedYang };
        final List<CodeGeneratorArg> codeGenerators = new ArrayList<>();
        final CodeGeneratorArg codeGeneratorArg = new CodeGeneratorArg(GeneratorMock.class.getName(), path);
        codeGenerators.add(codeGeneratorArg);
        final MavenProject mvnProject = Mockito.mock(MavenProject.class);
        final Build build = new Build();
        Mockito.when(mvnProject.getBuild()).thenReturn(build);
        final boolean dependencies = true;
        final YangToSourcesProcessor proc = new YangToSourcesProcessor(file, yangFilesRootDir, codeGenerators,
                mvnProject, dependencies, new YangProvider());
        Assert.assertNotNull(proc);
        proc.execute();
    }

}

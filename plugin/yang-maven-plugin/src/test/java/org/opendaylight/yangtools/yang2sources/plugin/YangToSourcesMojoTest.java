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

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

@RunWith(MockitoJUnitRunner.class)
public class YangToSourcesMojoTest {
    @Mock
    private MavenProject project;
    @Mock
    private Plugin plugin;
    @Mock
    private Build build;

    @Test
    public void yangToSourceMojoTest() throws Exception {
        doReturn(plugin).when(project).getPlugin(YangToSourcesMojo.PLUGIN_NAME);
        doReturn("target/").when(build).getDirectory();
        doReturn(build).when(project).getBuild();

        YangToSourcesMojo mojo = new YangToSourcesMojo();
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
}

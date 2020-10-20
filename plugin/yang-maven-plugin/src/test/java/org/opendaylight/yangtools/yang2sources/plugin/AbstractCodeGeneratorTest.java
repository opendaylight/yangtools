/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public abstract class AbstractCodeGeneratorTest {
    @Mock
    MavenProject project;
    @Mock
    YangProvider yangProvider;
    @Mock
    private Build build;
    @Mock
    private Plugin plugin;

    final YangToSourcesMojo setupMojo(final YangToSourcesProcessor processor) throws IOException {
        doReturn("target/").when(build).getDirectory();
        doReturn(new File("")).when(project).getBasedir();
        doReturn(build).when(project).getBuild();

        doReturn(Collections.emptyList()).when(plugin).getDependencies();
        doReturn(plugin).when(project).getPlugin(YangToSourcesMojo.PLUGIN_NAME);

        doNothing().when(yangProvider).addYangsToMetaInf(any(MavenProject.class), any(Collection.class));

        final YangToSourcesMojo mojo = new YangToSourcesMojo(processor);
        mojo.setProject(project);
        return mojo;
    }
}

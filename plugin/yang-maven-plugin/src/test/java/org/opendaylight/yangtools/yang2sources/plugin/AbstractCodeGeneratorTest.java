/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.google.common.collect.Iterators;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class AbstractCodeGeneratorTest {
    @Mock
    MavenProject project;
    @Mock
    YangProvider yangProvider;
    @Mock
    private Build build;
    @Mock
    private Plugin plugin;
    @Mock
    private BuildContext buildContext;

    @Before
    public void beforeEach() {
        new File("target/" + RebuildContext.PERSISTENCE_FILE_NAME).delete(); // avoid rebuild context impact
    }

    @Before
    public void before() {
        doReturn("target/").when(build).getDirectory();
        doReturn(build).when(project).getBuild();

        try {
            lenient().when(yangProvider.addYangsToMetaInf(any(MavenProject.class), anyCollection()))
                    .thenReturn(List.of());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    static final void assertMojoExecution(final YangToSourcesProcessor processor, final Prepare prepare,
            final Verify verify) {
        try (MockedStatic<?> staticLoader = mockStatic(ServiceLoader.class)) {
            final FileGenerator generator = mock(FileGenerator.class);
            doCallRealMethod().when(generator).importResolutionMode();

            final FileGeneratorFactory factory = mock(FileGeneratorFactory.class);
            doReturn("mockGenerator").when(factory).getIdentifier();

            try {
                doReturn(generator).when(factory).newFileGenerator(anyMap());
            } catch (FileGeneratorException e) {
                throw new AssertionError(e);
            }

            final ServiceLoader<?> loader = mock(ServiceLoader.class);
            doReturn(Iterators.singletonIterator(factory)).when(loader).iterator();
            staticLoader.when(() -> ServiceLoader.load(FileGeneratorFactory.class)).thenReturn(loader);

            try {
                prepare.prepare(generator);
                processor.execute();
                verify.verify(generator);
            } catch (FileGeneratorException | MojoExecutionException | MojoFailureException e) {
                throw new AssertionError(e);
            }
        }
    }

    @FunctionalInterface
    interface Prepare {
        void prepare(FileGenerator mock) throws FileGeneratorException;
    }

    @FunctionalInterface
    interface Verify {
        void verify(FileGenerator mock) throws FileGeneratorException;
    }
}

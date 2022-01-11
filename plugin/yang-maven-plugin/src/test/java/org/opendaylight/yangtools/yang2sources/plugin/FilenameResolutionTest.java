/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FilenameResolutionTest extends AbstractCodeGeneratorTest {
    @Test
    public void testResolveSubmoduleResource() throws URISyntaxException, AbstractMojoExecutionException, IOException {
        assertMojoExecution(new YangToSourcesProcessor(
            new File(Resources.getResource(FilenameResolutionTest.class, "/filename").toURI()), List.of(),
            List.of(new FileGeneratorArg("mockGenerator")), project, false, yangProvider), mock -> {
                doAnswer(invocation -> {
                    final EffectiveModelContext context = invocation.getArgument(0);
                    final ModuleResourceResolver resolver = invocation.getArgument(2);

                    final var module = Iterables.getOnlyElement(context.getModules());
                    assertEquals(Optional.of("/META-INF/yang/foo@2020-10-13.yang"),
                        resolver.findModuleResourcePath(module, YangTextSchemaSource.class));

                    final var submodule = Iterables.getOnlyElement(module.getSubmodules());
                    assertEquals(Optional.of("/META-INF/yang/foo-submodule@2020-10-12.yang"),
                        resolver.findModuleResourcePath(submodule, YangTextSchemaSource.class));

                    return ImmutableTable.of();
                }).when(mock).generateFiles(any(), any(), any());
            }, mock -> { });
    }
}

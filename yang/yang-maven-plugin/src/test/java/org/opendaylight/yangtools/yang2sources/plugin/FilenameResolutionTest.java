/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.ModuleResourceResolver;

public class FilenameResolutionTest extends AbstractCodeGeneratorTest {
    @Test
    public void testResolveSubmoduleResource() throws URISyntaxException, AbstractMojoExecutionException, IOException {
        setupMojo(new YangToSourcesProcessor(
            new File(Resources.getResource(FilenameResolutionTest.class, "/filename").toURI()), ImmutableList.of(),
            List.of(new CodeGeneratorArg(Generator.class.getName(), "outputDir")), project, false, yangProvider))
            .execute();
    }

    public static final class Generator implements BasicCodeGenerator {
        @Override
        public Collection<File> generateSources(final EffectiveModelContext context, final File outputBaseDir,
                final Set<Module> currentModules, final ModuleResourceResolver moduleResourcePathResolver)  {
            final var module = Iterables.getOnlyElement(context.getModules());
            assertEquals(Optional.of("/META-INF/yang/foo@2020-10-13.yang"),
                moduleResourcePathResolver.findModuleResourcePath(module, YangTextSchemaSource.class));

            final var submodule = Iterables.getOnlyElement(module.getSubmodules());
            assertEquals(Optional.of("/META-INF/yang/foo-submodule@2020-10-12.yang"),
                moduleResourcePathResolver.findModuleResourcePath(submodule, YangTextSchemaSource.class));
            return List.of();
        }

        @Override
        public void setAdditionalConfig(final Map<String, String> additionalConfiguration) {
            // Noop
        }

        @Override
        public void setResourceBaseDir(final File resourceBaseDir) {
            // Noop
        }
    }
}

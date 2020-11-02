/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.junit.Test;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class FilenameResolutionTest extends AbstractCodeGeneratorTest {
    @Test
    public void testResolveSubmoduleResource() throws URISyntaxException, AbstractMojoExecutionException, IOException {
        setupMojo(new YangToSourcesProcessor(
            new File(Resources.getResource(FilenameResolutionTest.class, "/filename").toURI()), List.of(),
            List.of(new FileGeneratorArg(Generator.class.getSimpleName())), project, false, yangProvider))
            .execute();
    }

    public static final class Generator implements FileGenerator {
        @Override
        public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(
                final EffectiveModelContext context, final Set<Module> localModules,
                final ModuleResourceResolver moduleResourcePathResolver) throws FileGeneratorException {
            final var module = Iterables.getOnlyElement(context.getModules());
            assertEquals(Optional.of("/META-INF/yang/foo@2020-10-13.yang"),
                moduleResourcePathResolver.findModuleResourcePath(module, YangTextSchemaSource.class));

            final var submodule = Iterables.getOnlyElement(module.getSubmodules());
            assertEquals(Optional.of("/META-INF/yang/foo-submodule@2020-10-12.yang"),
                moduleResourcePathResolver.findModuleResourcePath(submodule, YangTextSchemaSource.class));
            return ImmutableTable.of();
        }
    }

    @MetaInfServices
    public static final class GeneratorFactory implements FileGeneratorFactory {
        @Override
        public String getIdentifier() {
            return Generator.class.getSimpleName();
        }

        @Override
        public FileGenerator newFileGenerator(final Map<String, String> configuration) {
            return new Generator();
        }
    }
}

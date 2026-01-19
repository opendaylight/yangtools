/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.BindingGenerator;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

abstract class BaseCompilationTest {
    private static BindingGenerator BINDING_GENERATOR;

    @BeforeAll
    static final void initBindingGenerator() {
        BINDING_GENERATOR = ServiceLoader.load(BindingGenerator.class).findFirst().orElseThrow();
    }

    @AfterAll
    static final void clearBindingGenerator() {
        BINDING_GENERATOR = null;
    }

    @BeforeAll
    static final void createTestDirs() throws Exception {
        CompilationTestUtils.cleanUp(CompilationTestUtils.TEST_DIR);
        Files.createDirectories(CompilationTestUtils.COMPILER_OUTPUT_DIR);
        Files.createDirectories(CompilationTestUtils.GENERATOR_OUTPUT_DIR);
    }

    static final void generateTestSources(final List<GeneratedType> types, final Path sourcesOutputDir)
            throws IOException {
        types.sort(Comparator.comparing(GeneratedType::getName).reversed());

        final var generatedFiles = JavaFileGenerator.generateFiles(types, true);
        for (var cell : generatedFiles.cellSet()) {
            final var target = sourcesOutputDir.resolve(cell.getColumnKey().getPath());
            Files.createDirectories(target.getParent());
            try (var os = Files.newOutputStream(target)) {
                cell.getValue().writeBody(os);
            }
        }
    }

    static final List<GeneratedType> generateTestSources(final String resourceDirPath, final Path sourcesOutputDir) {
        final var context = YangParserTestUtils.parseYangResourceDirectory(resourceDirPath);
        final var types = BINDING_GENERATOR.generateTypes(context);
        try {
            generateTestSources(types, sourcesOutputDir);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        // Also generate YangModuleInfo
        for (var module : context.getModules()) {
            final var template = new YangModuleInfoTemplate(module, context,
                mod -> Optional.of("fake/" + mod.getName()));

            final var file = sourcesOutputDir
                .resolve(Naming.getServicePackageName(module.getQNameModule()).replace('.', File.separatorChar))
                .resolve(Naming.MODULE_INFO_CLASS_NAME + ".java");


            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, template.generate());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        return types;
    }
}

/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.nio.file.Files.newOutputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

abstract class BaseCompilationTest {
    @BeforeAll
    static final void createTestDirs() {
        if (CompilationTestUtils.TEST_DIR.exists()) {
            CompilationTestUtils.deleteTestDir(CompilationTestUtils.TEST_DIR);
        }
        assertTrue(CompilationTestUtils.GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(CompilationTestUtils.COMPILER_OUTPUT_DIR.mkdirs());
    }

    static final void generateTestSources(final List<GeneratedType> types, final File sourcesOutputDir)
            throws IOException {
        types.sort(Comparator.comparing(GeneratedType::getName).reversed());

        final Table<?, GeneratedFilePath, GeneratedFile> generatedFiles = JavaFileGenerator.generateFiles(types, true);
        for (Cell<?, GeneratedFilePath, GeneratedFile> cell : generatedFiles.cellSet()) {
            final File target = new File(sourcesOutputDir, cell.getColumnKey().getPath());
            Files.createParentDirs(target);

            try (OutputStream os = newOutputStream(target.toPath())) {
                cell.getValue().writeBody(os);
            }
        }
    }

    static final List<GeneratedType> generateTestSources(final String resourceDirPath, final File sourcesOutputDir) {
        final List<File> sourceFiles;
        try {
            sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        } catch (FileNotFoundException | URISyntaxException e) {
            throw new AssertionError(e);
        }

        final var context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final var types = new DefaultBindingGenerator().generateTypes(context);
        try {
            generateTestSources(types, sourcesOutputDir);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        // Also generate YangModuleInfo
        for (var module : context.getModules()) {
            final var template = new YangModuleInfoTemplate(module, context,
                mod -> Optional.of("fake/" + mod.getName()));

            final var file = new File(new File(sourcesOutputDir,
                Naming.getServicePackageName(module.getQNameModule()).replace('.', File.separatorChar)),
                Naming.MODULE_INFO_CLASS_NAME + ".java");
            try {
                Files.createParentDirs(file);
                Files.asCharSink(file, StandardCharsets.UTF_8).write(template.generate());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        return types;
    }
}

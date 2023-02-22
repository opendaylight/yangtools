/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.nio.file.Files.newOutputStream;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public abstract class BaseCompilationTest {

    @BeforeClass
    public static void createTestDirs() {
        if (CompilationTestUtils.TEST_DIR.exists()) {
            CompilationTestUtils.deleteTestDir(CompilationTestUtils.TEST_DIR);
        }
        assertTrue(CompilationTestUtils.GENERATOR_OUTPUT_DIR.mkdirs());
        assertTrue(CompilationTestUtils.COMPILER_OUTPUT_DIR.mkdirs());
    }

    protected static final void generateTestSources(final List<GeneratedType> types, final File sourcesOutputDir)
            throws IOException {
        types.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));

        final Table<?, GeneratedFilePath, GeneratedFile> generatedFiles = JavaFileGenerator.generateFiles(types, true);
        for (Cell<?, GeneratedFilePath, GeneratedFile> cell : generatedFiles.cellSet()) {
            final File target = new File(sourcesOutputDir, cell.getColumnKey().getPath());
            Files.createParentDirs(target);

            try (OutputStream os = newOutputStream(target.toPath())) {
                cell.getValue().writeBody(os);
            }
        }
    }

    protected static final List<GeneratedType> generateTestSources(final String resourceDirPath,
            final File sourcesOutputDir) throws IOException, URISyntaxException {
        final List<File> sourceFiles = CompilationTestUtils.getSourceFiles(resourceDirPath);
        final EffectiveModelContext context = YangParserTestUtils.parseYangFiles(sourceFiles);
        final List<GeneratedType> types = new DefaultBindingGenerator().generateTypes(context);
        generateTestSources(types, sourcesOutputDir);

        // Also generate YangModuleInfo
        for (Module module : context.getModules()) {
            final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, context,
                mod -> Optional.of("fake/" + mod.getName()));

            final File file = new File(new File(sourcesOutputDir,
                Naming.getRootPackageName(module.getQNameModule()).replace('.', File.separatorChar)),
                Naming.MODULE_INFO_CLASS_NAME + ".java");
            Files.createParentDirs(file);
            Files.asCharSink(file, StandardCharsets.UTF_8).write(template.generate());
        }

        return types;
    }
}

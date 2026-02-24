/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.HashBasedTable;
import java.io.File;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single attempt at generation. This class is split out of {@link JavaFileGenerator} for code clarity reasons.
 */
@NonNullByDefault
final class BindingJavaFileGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BindingJavaFileGenerator.class);
    private static final List<CodeGenerator> GENERATORS = List.of(
        new InterfaceGenerator(), new TOGenerator(), new EnumGenerator(), new BuilderGenerator());

    private final HashBasedTable<GeneratedFileType, GeneratedFilePath, GeneratedFile> result = HashBasedTable.create();
    private final boolean ignoreDuplicateFiles;

    private BindingJavaFileGenerator(final boolean ignoreDuplicateFiles) {
        this.ignoreDuplicateFiles = ignoreDuplicateFiles;
    }

    static HashBasedTable<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(
            final boolean ignoreDuplicateFiles, final List<GeneratedType> types) {
        final var tmp = new BindingJavaFileGenerator(ignoreDuplicateFiles);
        tmp.generateFiles(types);
        return tmp.result;
    }

    private void generateFiles(final List<GeneratedType> types) {
        for (var type : types) {
            for (var generator : GENERATORS) {
                if (generator.isAcceptable(type)) {
                    generateFile(generator, type);
                }
            }
        }
    }

    private void generateFile(final CodeGenerator generator, final GeneratedType type) {
        final var file =  GeneratedFilePath.ofFilePath(type.packageName()
            .replace('.', File.separatorChar) + File.separator + generator.getUnitName(type) + ".java");

        if (result.contains(GeneratedFileType.SOURCE, file)) {
            if (ignoreDuplicateFiles) {
                LOG.warn("Naming conflict for type '{}': file with same name already exists and will not be "
                        + "generated.", type.fullyQualifiedName());
                return;
            }
            throw new IllegalStateException("Duplicate file '" + file.getPath() + "' for "
                + type.fullyQualifiedName());
        }

        result.put(GeneratedFileType.SOURCE, file,
            new CodeGeneratorGeneratedFile(GeneratedFileLifecycle.TRANSIENT, generator, type));
    }
}

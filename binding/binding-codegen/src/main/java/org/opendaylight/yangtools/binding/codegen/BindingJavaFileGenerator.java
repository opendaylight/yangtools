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
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
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
    private static final JavaTypeName AUGMENTABLE = JavaTypeName.create(Augmentable.class);
    private static final JavaTypeName AUGMENTATION = JavaTypeName.create(Augmentation.class);
    private static final JavaTypeName ENTRY_OBJECT = JavaTypeName.create(EntryObject.class);
    private static final JavaTypeName YANG_DATA = JavaTypeName.create(YangData.class);

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
            if (type instanceof EnumTypeObjectArchetype etoa) {
                generateFile(new EnumTypeObjectGenerator(etoa));
            } else if (type instanceof GeneratedTransferObject gto) {
                generateFile(new TOGenerator(gto));
            } else {
                generateFile(new InterfaceGenerator(type));

                for (var impl : type.getImplements()) {
                    // "rpc" and "grouping" elements do not implement Augmentable
                    final var name = impl.name();
                    if (name.equals(AUGMENTABLE) || name.equals(AUGMENTATION) || name.equals(ENTRY_OBJECT)
                        || name.equals(YANG_DATA)) {
                        generateFile(new BuilderGenerator(type));
                        break;
                    }
                }
            }
        }
    }

    private void generateFile(final Generator generator) {
        final var typeName = generator.type().name();
        final var file =  GeneratedFilePath.ofFilePath(typeName.packageName().replace('.', File.separatorChar)
            + File.separator + generator.getUnitName() + ".java");

        if (result.contains(GeneratedFileType.SOURCE, file)) {
            if (ignoreDuplicateFiles) {
                LOG.warn(
                    "Naming conflict for type '{}': file with same name already exists and will not be generated.",
                    typeName.fullyQualifiedName());
                return;
            }
            throw new IllegalStateException(
                "Duplicate file '" + file.getPath() + "' for " + typeName.fullyQualifiedName());
        }

        result.put(GeneratedFileType.SOURCE, file,
            new CodeGeneratorGeneratedFile(GeneratedFileLifecycle.TRANSIENT, generator));
    }
}

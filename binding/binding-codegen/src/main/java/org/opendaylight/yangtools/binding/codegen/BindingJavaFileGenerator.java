/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.HashBasedTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.codegen.DataRootTemplate.Builder;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTOBuilder.AbstractGeneratedTransferObject;
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
    private record Index(Map<String, ? extends DataRootTemplate.@Nullable Builder> map) implements ModuleIndex {
        Index {
            requireNonNull(map);
        }

        @Override
        public Builder moduleFor(final JavaTypeName typeName) {
            var rootPackage = Naming.getModelRootPackageName(typeName.packageName());
            var builder = map.get(rootPackage);
            if (builder == null) {
                throw new VerifyException("No DataRootTemplate for " + rootPackage);
            }
            return builder;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BindingJavaFileGenerator.class);

    // "rpc" and "grouping" elements do not implement Augmentable
    private static final Set<JavaTypeName> BUILDER_INTERFACES = Set.of(
        JavaTypeName.create(Augmentable.class),
        JavaTypeName.create(Augmentation.class),
        JavaTypeName.create(EntryObject.class),
        JavaTypeName.create(YangData.class));

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
        // first pass: catch all DataRootTemplates, as they provide ModuleEffectiveStatement for other templates to use
        final var modules = new HashMap<String, DataRootTemplate.Builder>();
        final var moduleIndex = new Index(modules);
        for (var type : types) {
            if (type instanceof DataRootArchetype archetype) {
                final var builder = new DataRootTemplate.Builder(archetype);
                final var rootPackage = archetype.name().packageName();
                final var prev = modules.putIfAbsent(rootPackage, builder);
                if (prev != null) {
                    throw new VerifyException(
                        "Duplicate package " + rootPackage + " between " + archetype + " and " + prev.type());
                }
            }
        }

        // second pass: process all other types
        for (var type : types) {
            switch (type) {
                case Archetype archetype -> generateArchetype(moduleIndex, archetype);
                default -> {
                    generateBuilder(type);
                    generateFile(new InterfaceTemplate.Builder(type));
                }
            }
        }

        for (var module : modules.values()) {
            generateBuilder(module.type());
            generateFile(module);
        }
    }

    private void generateArchetype(final ModuleIndex moduleIndex, final Archetype type) {
        switch (type) {
            case BitsTypeObjectArchetype archetype -> generateFile(new BitsTypeObjectTemplate.Builder(archetype));
            case DataRootArchetype archetype -> {
                // processed separately
            }
            case EnumTypeObjectArchetype archetype -> generateFile(new EnumTypeObjectTemplate.Builder(archetype));
            case FeatureArchetype archetype -> generateFile(new FeatureTemplate.Builder(archetype));
            case KeyArchetype archetype -> generateFile(new KeyTemplate.Builder(archetype));
            case ScalarTypeObjectArchetype archetype -> generateFile(new ScalarTypeObjectTemplate.Builder(archetype));
            case UnionTypeObjectArchetype archetype -> generateFile(new UnionTypeObjectTemplate.Builder(archetype));
            case AbstractGeneratedTransferObject<?> gto -> throw new VerifyException("Unsupported " + gto);
        }
    }

    private void generateBuilder(final GeneratedType type) {
        // FIXME: express this in GeneratedType hierarchy as a marker interface
        for (var iface : type.getImplements()) {
            if (BUILDER_INTERFACES.contains(iface.name())) {
                generateFile(new BuilderTemplate.Builder(type));
                return;
            }
        }
    }

    private void generateFile(final Template.Builder builder) {
        final var template = builder.build();
        final var typeName = template.typeName();
        final var file = GeneratedFilePath.ofDirectoryFile(
            typeName.packageName().replace('.', GeneratedFilePath.SEPARATOR),
            typeName.simpleName() + ".java");

        if (result.contains(GeneratedFileType.SOURCE, file)) {
            if (ignoreDuplicateFiles) {
                LOG.warn("Naming conflict for type '{}': file with same name already exists and will not be generated.",
                    typeName);
                return;
            }
            throw new IllegalStateException("Duplicate file '" + file.path() + "' for " + typeName);
        }

        result.put(GeneratedFileType.SOURCE, file,
            new CodeGeneratorGeneratedFile(GeneratedFileLifecycle.TRANSIENT, template));
    }
}

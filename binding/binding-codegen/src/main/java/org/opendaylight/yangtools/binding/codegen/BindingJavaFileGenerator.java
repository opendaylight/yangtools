/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import com.google.common.collect.HashBasedTable;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.ActionArchetype;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.KeyedListActionArchetype;
import org.opendaylight.yangtools.binding.model.KeyedListNotificationArchetype;
import org.opendaylight.yangtools.binding.model.NotificationArchetype;
import org.opendaylight.yangtools.binding.model.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.OpaqueObjectArchetype;
import org.opendaylight.yangtools.binding.model.RpcArchetype;
import org.opendaylight.yangtools.binding.model.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.YangDataArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
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
    private final HashBasedTable<GeneratedFileType, GeneratedFilePath, GeneratedFile> result = HashBasedTable.create();
    private final boolean ignoreDuplicateFiles;

    private BindingJavaFileGenerator(final boolean ignoreDuplicateFiles) {
        this.ignoreDuplicateFiles = ignoreDuplicateFiles;
    }

    static HashBasedTable<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(
            final boolean ignoreDuplicateFiles, final List<Archetype> types) {
        final var tmp = new BindingJavaFileGenerator(ignoreDuplicateFiles);
        tmp.generateFiles(types);
        return tmp.result;
    }

    private void generateFiles(final List<Archetype> types) {
        // First pass: catch all
        //   - DataRootArchetypes, as they provide ModuleEffectiveStatement for other templates to use
        //   - KeyArchetype, as they provide KeyArchetype binding
        final var modules = new HashMap<String, @Nullable DataRootArchetype>();
        final var entryToKey = new HashMap<TypeName, KeyArchetype>();
        final var choiceByName = new HashMap<TypeName, ChoiceInArchetype>();
        for (var type : types) {
            switch (type) {
                case ChoiceInArchetype archetype -> {
                    final var name = archetype.name();
                    final var prev = choiceByName.putIfAbsent(name, archetype);
                    if (prev != null) {
                        throw new VerifyException("Conflicing ChoiceIn " + archetype + " and " + prev);
                    }
                }
                case DataRootArchetype archetype -> {
                    final var rootPackage = archetype.name().packageName();
                    final var prev = modules.putIfAbsent(rootPackage, archetype);
                    if (prev != null) {
                        throw new VerifyException(
                            "Duplicate package " + rootPackage + " between " + archetype + " and " + prev);
                    }
                }
                case KeyArchetype archetype -> {
                    final var entryName = archetype.entryObject().name();
                    final var prev = entryToKey.putIfAbsent(entryName, archetype);
                    if (prev != null) {
                        throw new VerifyException(
                            "Conflicing KeyArchetype " + entryName + " keys " + entryName + " and " + prev);
                    }
                }
                default -> {
                    // no-op
                }
            }
        }

        // second pass: process all other types
        for (var type : types) {
            final var rootPackage = Naming.getModelRootPackageName(type.packageName());
            final var root = modules.get(rootPackage);
            if (root == null) {
                throw new VerifyException("No DataRootArchetype for " + rootPackage);
            }

            final var template = switch (type) {
                case ActionArchetype archetype -> new ActionTemplate(root, archetype);
                case AugmentationArchetype archetype -> new AugmentationTemplate(root, archetype);
                case BitsTypeObjectArchetype archetype -> BitsTypeObjectTemplate.of(root, archetype);
                case CaseObjectArchetype archetype ->
                    new CaseObjectTemplate(root, archetype, choiceByName.get(archetype.parentName()));
                case ChoiceInArchetype archetype -> new ChoiceInTemplate(root, archetype);
                case ContainerObjectArchetype archetype -> new ContainerObjectTemplate(root, archetype);
                case DataRootArchetype archetype -> DataRootTemplate.of(root, archetype);
                case EntryObjectArchetype archetype ->
                    new EntryObjectTemplate(root, archetype, entryToKey.get(archetype.name()));
                case EnumTypeObjectArchetype archetype -> EnumTypeObjectTemplate.of(root, archetype);
                case FeatureArchetype archetype -> new FeatureTemplate(root, archetype);
                case GroupingArchetype archetype -> new GroupingTemplate(root, archetype);
                case IdentityArchetype archetype -> new IdentityTemplate(root, archetype);
                case InstanceNotificationArchetype archetype -> new InstanceNotificationTemplate(root, archetype);
                case ItemObjectArchetype archetype -> new ItemObjectTemplate(root, archetype);
                case KeyArchetype archetype -> new KeyTemplate(root, archetype);
                case KeyedListActionArchetype archetype ->
                    new KeyedListActionTemplate(root, archetype, entryToKey.get(archetype.parentName()));
                case KeyedListNotificationArchetype archetype ->
                    new KeyedListNotificationTemplate(root, archetype, entryToKey.get(archetype.parentName()));
                case NotificationArchetype archetype -> new NotificationTemplate(root, archetype);
                case NotificationBodyArchetype archetype -> new NotificationBodyTemplate(root, archetype);
                case OpaqueObjectArchetype<?> archetype -> new OpaqueObjectTemplate(root, archetype);
                case RpcArchetype archetype -> new RpcTemplate(root, archetype);
                case RpcInputArchetype archetype -> new RpcInputTemplate(root, archetype);
                case RpcOutputArchetype archetype -> new RpcOutputTemplate(root, archetype);
                case ScalarTypeObjectArchetype archetype -> ScalarTypeObjectTemplate.of(root, archetype);
                case UnionTypeObjectArchetype archetype -> UnionTypeObjectTemplate.of(root, archetype);
                case YangDataArchetype archetype -> new YangDataTemplate(root, archetype);
            };

            generateFile(template);
            if (template instanceof ArchetypeTemplate.WithBuilder withBuilder) {
                generateFile(withBuilder.newBuilderTemplate());
            }
        }
    }

    private void generateFile(final Template template) {
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

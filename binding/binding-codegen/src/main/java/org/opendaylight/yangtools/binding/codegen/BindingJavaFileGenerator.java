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
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.ActionArchetype;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyedListNotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationArchetype;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.api.OpaqueObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangDataArchetype;
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
        //   - EntryObjectArchetypes, as they provide KeyArchetype binding
        final var modules = new HashMap<String, DataRootTemplate.Builder>();
        final var entryToKey = new HashMap<JavaTypeName, JavaTypeName>();
        for (var type : types) {
            switch (type) {
                case DataRootArchetype archetype -> {
                    final var builder = new DataRootTemplate.Builder(archetype);
                    final var rootPackage = archetype.name().packageName();
                    final var prev = modules.putIfAbsent(rootPackage, builder);
                    if (prev != null) {
                        throw new VerifyException(
                            "Duplicate package " + rootPackage + " between " + archetype + " and " + prev.type());
                    }
                }
                case EntryObjectArchetype archetype -> {
                    final var entryName = archetype.name();
                    final var keyName = archetype.keyName();
                    final var prev = entryToKey.putIfAbsent(entryName, keyName);
                    if (prev != null) {
                        throw new VerifyException(
                            "Conflicing EntryObjectArchetype" + entryName + " keys " + keyName + " and " + prev);
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
            final var rootBuilder = modules.get(rootPackage);
            if (rootBuilder == null) {
                throw new VerifyException("No DataRootTemplate for " + rootPackage);
            }

            final var root = rootBuilder.type();
            switch (type) {
                case DataRootArchetype archetype -> {
                    // processed separately
                }

                // TypeObject specializations
                case BitsTypeObjectArchetype btao -> generateFile(new BitsTypeObjectTemplate.Builder(btao, root));
                case EnumTypeObjectArchetype etao -> generateFile(new EnumTypeObjectTemplate.Builder(etao, root));
                case ScalarTypeObjectArchetype stao -> generateFile(new ScalarTypeObjectTemplate.Builder(stao, root));
                case UnionTypeObjectArchetype utao -> generateFile(new UnionTypeObjectTemplate.Builder(utao, root));

                // everything else
                case ActionArchetype archetype -> generateFile(new ActionTemplate.Builder(archetype, root));
                case AugmentationArchetype archetype ->
                    generateBoth(AugmentationTemplate.Builder::new, archetype, root);
                case CaseObjectArchetype archetype -> generateBoth(CaseObjectTemplate.Builder::new, archetype, root);
                case ChoiceInArchetype archetype -> generateFile(new ChoiceInTemplate.Builder(archetype, root));
                case ContainerObjectArchetype archetype ->
                    generateBoth(ContainerObjectTemplate.Builder::new, archetype, root);
                case EntryObjectArchetype archetype -> generateBoth(EntryObjectTemplate.Builder::new, archetype, root);
                case FeatureArchetype archetype -> generateFile(new FeatureTemplate.Builder(archetype, root));
                case GroupingArchetype archetype -> generateFile(new GroupingTemplate.Builder(archetype, root));
                case IdentityArchetype archetype -> generateFile(new IdentityTemplate.Builder(archetype, root));
                case InstanceNotificationArchetype archetype ->
                    generateBoth(InstanceNotificationTemplate.Builder::new, archetype, root);
                case ItemObjectArchetype archetype -> generateBoth(ItemObjectTemplate.Builder::new, archetype, root);
                case KeyArchetype archetype -> generateFile(new KeyTemplate.Builder(archetype, root));
                case KeyedListActionArchetype archetype ->
                    generateFile(new KeyedListActionTemplate.Builder(archetype, root,
                        entryToKey.get(archetype.parentName())));
                case KeyedListNotificationArchetype archetype ->
                    generateBoth(new KeyedListNotificationTemplate.Builder(archetype, root,
                        entryToKey.get(archetype.parentName())).build(), root);
                case NotificationArchetype archetype ->
                    generateBoth(NotificationTemplate.Builder::new, archetype, root);
                case NotificationBodyArchetype archetype ->
                    generateFile(new NotificationBodyTemplate.Builder(archetype, root));
                case OpaqueObjectArchetype<?> archetype ->
                    generateFile(new OpaqueObjectTemplate.Builder(archetype, root));
                case RpcArchetype archetype -> generateFile(new RpcTemplate.Builder(archetype, root));
                case RpcInputArchetype archetype -> generateBoth(RpcInputTemplate.Builder::new, archetype, root);
                case RpcOutputArchetype archetype -> generateBoth(RpcOutputTemplate.Builder::new, archetype, root);
                case YangDataArchetype archetype -> generateBoth(YangDataTemplate.Builder::new, archetype, root);
            }
        }

        // third pass: process DataRootTemplates last
        for (var module : modules.values()) {
            generateFile(module);
        }
    }

    private <A extends DataContainerArchetype> void generateBoth(
            final BiFunction<A, DataRootArchetype, Template.Builder> builderConstructor,
            final A archetype, final DataRootArchetype root) {
        final var template = builderConstructor.apply(archetype, root).build();
        if (!(template instanceof InterfaceTemplate ifaceTemplate)) {
            throw new VerifyException("Unexpected template " + template);
        }
        generateBoth(ifaceTemplate, root);
    }

    private <A extends DataContainerArchetype> void generateBoth(final InterfaceTemplate<?> template,
            final DataRootArchetype root) {
        final var builderTarget = template.builderTarget();
        if (builderTarget == null) {
            throw new VerifyException("Unneeded builder for " + template);
        }
        generateFile(new BuilderTemplate.Builder(builderTarget));
        generateFile(template);
    }

    private void generateFile(final Template.Builder builder) {
        generateFile(builder.build());
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

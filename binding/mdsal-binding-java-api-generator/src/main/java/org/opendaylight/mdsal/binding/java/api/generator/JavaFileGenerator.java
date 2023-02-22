/*
 * Copyright (c) 2020 PATHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.opendaylight.mdsal.binding.generator.BindingGenerator;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JavaFileGenerator implements FileGenerator {
    public static final String CONFIG_IGNORE_DUPLICATE_FILES = "ignoreDuplicateFiles";

    private static final Logger LOG = LoggerFactory.getLogger(JavaFileGenerator.class);
    private static final CharMatcher DOT_MATCHER = CharMatcher.is('.');
    private static final String MODULE_INFO = Naming.MODULE_INFO_CLASS_NAME + ".java";
    private static final String MODEL_BINDING_PROVIDER = Naming.MODEL_BINDING_PROVIDER_CLASS_NAME + ".java";
    private static final GeneratedFilePath MODEL_BINDING_PROVIDER_SERVICE =
        GeneratedFilePath.ofPath("META-INF/services/" + YangModelBindingProvider.class.getName());
    private static final List<CodeGenerator> GENERATORS = List.of(
        new InterfaceGenerator(), new TOGenerator(), new EnumGenerator(), new BuilderGenerator());

    private final BindingGenerator bindingGenerator;
    private final boolean ignoreDuplicateFiles;

    JavaFileGenerator(final Map<String, String> configuration) {
        final String ignoreDuplicateFilesString = configuration.get(CONFIG_IGNORE_DUPLICATE_FILES);
        if (ignoreDuplicateFilesString != null) {
            ignoreDuplicateFiles = Boolean.parseBoolean(ignoreDuplicateFilesString);
        } else {
            ignoreDuplicateFiles = true;
        }
        bindingGenerator = ServiceLoader.load(BindingGenerator.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No BindingGenerator implementation found"));
    }

    @Override
    public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(final EffectiveModelContext context,
            final Set<Module> localModules, final ModuleResourceResolver moduleResourcePathResolver)
                throws FileGeneratorException {
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> result =
            generateFiles(bindingGenerator.generateTypes(context, localModules), ignoreDuplicateFiles);

        // YangModuleInfo files
        final Builder<String> bindingProviders = ImmutableSet.builder();
        for (Module module : localModules) {
            final YangModuleInfoTemplate template = new YangModuleInfoTemplate(module, context,
                mod -> moduleResourcePathResolver.findModuleResourcePath(mod, YangTextSchemaSource.class));
            final String path = DOT_MATCHER.replaceFrom(template.getPackageName(), '/') + "/";

            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODULE_INFO),
                new SupplierGeneratedFile(GeneratedFileLifecycle.TRANSIENT, template::generate));
            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODEL_BINDING_PROVIDER),
                new SupplierGeneratedFile(GeneratedFileLifecycle.TRANSIENT, template::generateModelProvider));

            bindingProviders.add(template.getModelBindingProviderName());
        }

        // META-INF/services entries, sorted to make the build predictable
        final List<String> sorted = new ArrayList<>(bindingProviders.build());
        sorted.sort(String::compareTo);

        result.put(GeneratedFileType.RESOURCE, MODEL_BINDING_PROVIDER_SERVICE,
            GeneratedFile.of(GeneratedFileLifecycle.TRANSIENT, String.join("\n", sorted)));

        return ImmutableTable.copyOf(result);
    }

    @VisibleForTesting
    static Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(final List<GeneratedType> types,
            final boolean ignoreDuplicateFiles) {
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> result = HashBasedTable.create();

        for (Type type : types) {
            for (CodeGenerator generator : GENERATORS) {
                if (!generator.isAcceptable(type)) {
                    continue;
                }

                final GeneratedFilePath file =  GeneratedFilePath.ofFilePath(
                    type.getPackageName().replace('.', File.separatorChar)
                    + File.separator + generator.getUnitName(type) + ".java");

                if (result.contains(GeneratedFileType.SOURCE, file)) {
                    if (ignoreDuplicateFiles) {
                        LOG.warn("Naming conflict for type '{}': file with same name already exists and will not be "
                                + "generated.", type.getFullyQualifiedName());
                        continue;
                    }
                    throw new IllegalStateException("Duplicate file '" + file.getPath() + "' for "
                        + type.getFullyQualifiedName());
                }

                result.put(GeneratedFileType.SOURCE, file,
                    new CodeGeneratorGeneratedFile(GeneratedFileLifecycle.TRANSIENT, generator, type));
            }
        }

        return result;
    }
}

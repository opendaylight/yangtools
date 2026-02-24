/*
 * Copyright (c) 2020 PATHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.BindingGenerator;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

final class JavaFileGenerator implements FileGenerator {
    public static final String CONFIG_IGNORE_DUPLICATE_FILES = "ignoreDuplicateFiles";

    private static final CharMatcher DOT_MATCHER = CharMatcher.is('.');
    private static final String MODULE_INFO = Naming.MODULE_INFO_CLASS_NAME + ".java";
    private static final String MODEL_BINDING_PROVIDER = Naming.MODEL_BINDING_PROVIDER_CLASS_NAME + ".java";
    private static final GeneratedFilePath MODEL_BINDING_PROVIDER_SERVICE =
        GeneratedFilePath.ofPath("META-INF/services/" + YangModelBindingProvider.class.getName());

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
        final var result = BindingJavaFileGenerator.generateFiles(ignoreDuplicateFiles,
            bindingGenerator.generateTypes(context, localModules));

        // YangModuleInfo files
        final var bindingProviders = ImmutableSet.<String>builder();
        for (final var module : localModules) {
            final var template = new YangModuleInfoTemplate(module, context,
                mod -> moduleResourcePathResolver.findModuleResourcePath(mod, YangTextSource.class));
            final var path = DOT_MATCHER.replaceFrom(template.packageName(), '/') + "/";

            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODULE_INFO),
                new SupplierGeneratedFile(GeneratedFileLifecycle.TRANSIENT, template::generate));
            result.put(GeneratedFileType.SOURCE, GeneratedFilePath.ofPath(path + MODEL_BINDING_PROVIDER),
                new SupplierGeneratedFile(GeneratedFileLifecycle.TRANSIENT, template::generateModelProvider));

            bindingProviders.add(template.modelBindingProviderName());
        }

        // META-INF/services entries, sorted to make the build predictable
        final var sorted = new ArrayList<>(bindingProviders.build());
        sorted.sort(String::compareTo);

        result.put(GeneratedFileType.RESOURCE, MODEL_BINDING_PROVIDER_SERVICE,
            GeneratedFile.of(GeneratedFileLifecycle.TRANSIENT, String.join("\n", sorted)));

        return ImmutableTable.copyOf(result);
    }
}

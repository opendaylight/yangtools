/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;

/**
 * Simple helpers to help with reconstruction of BindingRuntimeContext from generated binding classes. These involve
 * reflection and YANG model assembly, hence should not be used without any caching whatsoever or any support for
 * dynamic schema updates.
 */
@Beta
public final class BindingRuntimeHelpers {
    private BindingRuntimeHelpers() {
        // Hidden on purpose
    }

    public static @NonNull EffectiveModelContext createEffectiveModel(final Class<?>... classes) {
        return createEffectiveModel(Arrays.stream(classes)
            .map(BindingRuntimeHelpers::getYangModuleInfo)
            .collect(Collectors.toList()));
    }

    public static @NonNull EffectiveModelContext createEffectiveModel(
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        try {
            return createEffectiveModel(ServiceLoaderState.ParserFactory.INSTANCE, moduleInfos);
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to parse models", e);
        }
    }

    public static @NonNull EffectiveModelContext createEffectiveModel(final YangParserFactory parserFactory,
            final Iterable<? extends YangModuleInfo> moduleInfos) throws YangParserException {
        return prepareContext(parserFactory, moduleInfos).modelContext();
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext() {
        final ModuleInfoSnapshot infos;
        try {
            infos = prepareContext(ServiceLoaderState.ParserFactory.INSTANCE, loadModuleInfos());
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to parse models", e);
        }
        return new DefaultBindingRuntimeContext(ServiceLoaderState.Generator.INSTANCE.generateTypeMapping(
            infos.modelContext()), infos);
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final Class<?>... classes) {
        try {
            return createRuntimeContext(ServiceLoaderState.ParserFactory.INSTANCE,
                ServiceLoaderState.Generator.INSTANCE, classes);
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to parse models", e);
        }
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(
            final Collection<? extends YangModuleInfo> infos) {
        final ModuleInfoSnapshot snapshot;

        try {
            snapshot = prepareContext(ServiceLoaderState.ParserFactory.INSTANCE, infos);
        } catch (YangParserException e) {
            throw new IllegalStateException("Failed to parse models", e);
        }

        return new DefaultBindingRuntimeContext(
            ServiceLoaderState.Generator.INSTANCE.generateTypeMapping(snapshot.modelContext()), snapshot);
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final YangParserFactory parserFactory,
            final BindingRuntimeGenerator generator, final Class<?>... classes) throws YangParserException {
        return createRuntimeContext(parserFactory, generator, Arrays.asList(classes));
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final YangParserFactory parserFactory,
            final BindingRuntimeGenerator generator, final Collection<Class<?>> classes) throws YangParserException {
        final var infos = prepareContext(parserFactory, classes.stream()
            .map(BindingRuntimeHelpers::getYangModuleInfo)
            .collect(Collectors.toList()));
        return new DefaultBindingRuntimeContext(generator.generateTypeMapping(infos.modelContext()), infos);
    }

    public static @NonNull YangModuleInfo getYangModuleInfo(final Class<?> clazz) {
        final var modelPackage = Naming.rootToServicePackageName(clazz.getPackage().getName());

        for (var bindingProvider : ServiceLoader.load(YangModelBindingProvider.class, clazz.getClassLoader())) {
            var moduleInfo = bindingProvider.getModuleInfo();
            if (modelPackage.equals(moduleInfo.getClass().getPackage().getName())) {
                return moduleInfo;
            }
        }
        throw new IllegalStateException("Failed to find YangModuleInfo in package " + modelPackage + " for " + clazz);
    }

    public static @NonNull ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads {@link YangModuleInfo} infos available on supplied classloader.
     *
     * <p>
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for {@link YangModelBindingProvider}.
     * {@link YangModelBindingProvider} are simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * <p>
     * When {@link YangModuleInfo} is available, all dependencies are recursively collected into returning set by
     * collecting results of {@link YangModuleInfo#getImportedModules()}.
     *
     * @param classLoader Classloader for which {@link YangModuleInfo} should be retrieved.
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    public static @NonNull ImmutableSet<YangModuleInfo> loadModuleInfos(final ClassLoader classLoader) {
        final var moduleInfoSet = ImmutableSet.<YangModuleInfo>builder();
        for (var bindingProvider : ServiceLoader.load(YangModelBindingProvider.class, classLoader)) {
            var moduleInfo = bindingProvider.getModuleInfo();
            checkState(moduleInfo != null, "Module Info for %s is not available.", bindingProvider.getClass());
            collectYangModuleInfo(bindingProvider.getModuleInfo(), moduleInfoSet);
        }
        return moduleInfoSet.build();
    }

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final ImmutableSet.Builder<YangModuleInfo> moduleInfoSet) {
        moduleInfoSet.add(moduleInfo);
        for (var dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

    private static @NonNull ModuleInfoSnapshot prepareContext(final YangParserFactory parserFactory,
            final Iterable<? extends YangModuleInfo> moduleInfos) throws YangParserException {
        return new ModuleInfoSnapshotBuilder(parserFactory).add(moduleInfos).build();
    }
}

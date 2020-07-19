/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

/**
 * Simple helpers to help with reconstruction of BindingRuntimeContext from generated binding classes. These involve
 * reflection and YANG model assembly, hence should not be used without any caching whatsoever or any support for
 * dynamic schema updates.
 */
@Beta
public final class BindingRuntimeHelpers {
    private BindingRuntimeHelpers() {

    }

    public static @NonNull EffectiveModelContext createEffectiveModel(final Class<?>... classes) {
        return createEffectiveModel(Arrays.stream(classes)
            .map(BindingRuntimeHelpers::extractYangModuleInfo)
            .collect(Collectors.toList()));
    }

    public static @NonNull EffectiveModelContext createEffectiveModel(
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        return createEffectiveModel(ServiceLoaderState.ParserFactory.INSTANCE, moduleInfos);
    }

    public static @NonNull EffectiveModelContext createEffectiveModel(final YangParserFactory parserFactory,
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        return prepareContext(parserFactory, moduleInfos).getEffectiveModelContext();
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext() {
        final ModuleInfoSnapshot infos = prepareContext(ServiceLoaderState.ParserFactory.INSTANCE,
            BindingReflections.loadModuleInfos());
        return DefaultBindingRuntimeContext.create(ServiceLoaderState.Generator.INSTANCE.generateTypeMapping(
            infos.getEffectiveModelContext()), infos);
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final Class<?>... classes) {
        return createRuntimeContext(ServiceLoaderState.ParserFactory.INSTANCE, ServiceLoaderState.Generator.INSTANCE,
            classes);
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final YangParserFactory parserFactory,
            final BindingRuntimeGenerator generator, final Class<?>... classes) {
        return createRuntimeContext(parserFactory, generator, Arrays.asList(classes));
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final YangParserFactory parserFactory,
            final BindingRuntimeGenerator generator, final Collection<Class<?>> classes) {
        final ModuleInfoSnapshot infos = prepareContext(parserFactory, classes.stream()
            .map(BindingRuntimeHelpers::extractYangModuleInfo)
            .collect(Collectors.toList()));
        return DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(infos.getEffectiveModelContext()), infos);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    static @NonNull YangModuleInfo extractYangModuleInfo(final Class<?> clazz) {
        try {
            return BindingReflections.getModuleInfo(clazz);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to extract module info from " + clazz, e);
        }
    }

    private static @NonNull ModuleInfoSnapshot prepareContext(final YangParserFactory parserFactory,
            final Iterable<? extends YangModuleInfo> moduleInfos) {
        final ModuleInfoSnapshotBuilder ctx = new ModuleInfoSnapshotBuilder("helper", parserFactory);
        ctx.registerModuleInfos(moduleInfos);
        return ctx.build();
    }
}

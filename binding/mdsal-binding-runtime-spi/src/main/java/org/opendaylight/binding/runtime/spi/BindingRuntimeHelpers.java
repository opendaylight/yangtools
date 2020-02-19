/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.spi;

import com.google.common.annotations.Beta;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

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
        return prepareContext(moduleInfos).tryToCreateModelContext().orElseThrow();
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final BindingRuntimeGenerator generator) {
        final ModuleInfoBackedContext ctx = prepareContext(BindingReflections.loadModuleInfos());
        return BindingRuntimeContext.create(generator.generateTypeMapping(ctx.tryToCreateModelContext().orElseThrow()),
            ctx);
    }

    public static @NonNull BindingRuntimeContext createRuntimeContext(final BindingRuntimeGenerator generator,
            final Class<?>... classes) {
        final ModuleInfoBackedContext ctx = prepareContext(Arrays.stream(classes)
            .map(BindingRuntimeHelpers::extractYangModuleInfo)
            .collect(Collectors.toList()));
        return BindingRuntimeContext.create(generator.generateTypeMapping(ctx.tryToCreateModelContext().orElseThrow()),
            ctx);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    static YangModuleInfo extractYangModuleInfo(final Class<?> clazz) {
        try {
            return BindingReflections.getModuleInfo(clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract module info from " + clazz, e);
        }
    }

    private static ModuleInfoBackedContext prepareContext(final Iterable<? extends YangModuleInfo> moduleInfos) {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create(
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
        ctx.addModuleInfos(moduleInfos);
        return ctx;
    }
}

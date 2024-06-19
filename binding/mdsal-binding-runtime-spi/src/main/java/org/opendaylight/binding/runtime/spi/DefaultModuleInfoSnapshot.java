/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

final class DefaultModuleInfoSnapshot extends GeneratedClassLoadingStrategy implements ModuleInfoSnapshot {
    private final ImmutableMap<SourceIdentifier, YangModuleInfo> moduleInfos;
    private final ImmutableMap<String, ClassLoader> classLoaders;
    private final @NonNull EffectiveModelContext effectiveModel;

    DefaultModuleInfoSnapshot(final EffectiveModelContext effectiveModel,
            final Map<SourceIdentifier, YangModuleInfo> moduleInfos, final Map<String, ClassLoader> classLoaders) {
        this.effectiveModel = requireNonNull(effectiveModel);
        this.moduleInfos = ImmutableMap.copyOf(moduleInfos);
        this.classLoaders = ImmutableMap.copyOf(classLoaders);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return effectiveModel;
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        final YangModuleInfo info = moduleInfos.get(sourceIdentifier);
        if (info == null) {
            return Futures.immediateFailedFuture(
                new MissingSchemaSourceException("No source registered", sourceIdentifier));
        }
        return Futures.immediateFuture(YangTextSchemaSource.delegateForByteSource(sourceIdentifier,
                    info.getYangTextByteSource()));
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        final String packageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);
        final ClassLoader loader = classLoaders.get(packageName);
        if (loader == null) {
            throw new ClassNotFoundException("Package " + packageName + " not found");
        }
        return loader.loadClass(fullyQualifiedName);
    }
}

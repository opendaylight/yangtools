/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;
import org.opendaylight.yangtools.yang.model.spi.source.DelegatedYangTextSource;

final class DefaultModuleInfoSnapshot implements ModuleInfoSnapshot {
    private final ImmutableMap<SourceIdentifier, YangModuleInfo> moduleInfos;
    private final ImmutableMap<String, ClassLoader> classLoaders;
    private final @NonNull EffectiveModelContext modelContext;

    DefaultModuleInfoSnapshot(final EffectiveModelContext modelContext,
            final Map<SourceIdentifier, YangModuleInfo> moduleInfos, final Map<String, ClassLoader> classLoaders) {
        this.modelContext = requireNonNull(modelContext);
        this.moduleInfos = ImmutableMap.copyOf(moduleInfos);
        this.classLoaders = ImmutableMap.copyOf(classLoaders);
    }

    @Override
    public EffectiveModelContext modelContext() {
        return modelContext;
    }

    @Override
    public YangTextSource yangTextSource(final SourceIdentifier sourceId) {
        final var info = moduleInfos.get(sourceId);
        return info == null ? null : new DelegatedYangTextSource(sourceId, info.getYangTextCharSource());
    }

    @Override
    public YangTextSource getYangTextSource(final SourceIdentifier sourceId) throws MissingSchemaSourceException {
        final var source = yangTextSource(sourceId);
        if (source == null) {
            throw new MissingSchemaSourceException(sourceId, "No source registered");
        }
        return source;
    }

    @Override
    public <T> Class<T> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        final var packageName = Naming.getModelRootPackageName(fullyQualifiedName);
        final var loader = classLoaders.get(packageName);
        if (loader == null) {
            throw new ClassNotFoundException("Package " + packageName + " not found");
        }
        @SuppressWarnings("unchecked")
        final var loaded = (Class<T>) loader.loadClass(fullyQualifiedName);
        return loaded;
    }
}

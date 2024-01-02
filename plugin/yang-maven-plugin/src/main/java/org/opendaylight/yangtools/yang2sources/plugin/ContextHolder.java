/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.spi.source.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSchemaSource;

final class ContextHolder implements Immutable, ModuleResourceResolver {
    private final @NonNull EffectiveModelContext context;
    private final @NonNull ImmutableSet<Module> modules;
    private final ImmutableSet<SourceIdentifier> sources;

    ContextHolder(final EffectiveModelContext context, final Set<Module> modules, final Set<SourceIdentifier> sources) {
        this.context = requireNonNull(context);
        this.modules = ImmutableSet.copyOf(modules);
        this.sources = ImmutableSet.copyOf(sources);
    }

    @Override
    public Optional<String> findModuleResourcePath(final ModuleLike module,
            final Class<? extends SchemaSourceRepresentation> representation) {
        checkArgument(YangTextSchemaSource.class.equals(requireNonNull(representation)),
            "Unsupported representation %s", representation);
        final SourceIdentifier id = Util.moduleToIdentifier(module);
        return sources.contains(id)
                ? Optional.of("/" + YangToSourcesProcessor.META_INF_YANG_STRING_JAR + "/" + id.toYangFilename())
                        : Optional.empty();
    }

    @NonNull EffectiveModelContext getContext() {
        return context;
    }

    @NonNull ImmutableSet<Module> getYangModules() {
        return modules;
    }
}

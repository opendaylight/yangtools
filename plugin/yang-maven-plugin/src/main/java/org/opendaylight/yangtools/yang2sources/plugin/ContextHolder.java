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

import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

record ContextHolder(
        @NonNull EffectiveModelContext modelContext,
        @NonNull Set<Module> modules,
        @NonNull Set<SourceIdentifier> sources) implements Immutable, ModuleResourceResolver {
    ContextHolder {
        requireNonNull(modelContext);
        modules = Set.copyOf(modules);
        sources = Set.copyOf(sources);
    }

    @Override
    public Optional<String> findModuleResourcePath(final ModuleLike module,
            final Class<? extends SourceRepresentation> representation) {
        checkArgument(YangTextSource.class.equals(requireNonNull(representation)),
            "Unsupported representation %s", representation);
        final var id = module.getSourceIdentifier();
        return sources.contains(id)
                ? Optional.of("/" + YangToSourcesProcessor.META_INF_YANG_STRING_JAR + "/" + id.toYangFilename())
                        : Optional.empty();
    }
}

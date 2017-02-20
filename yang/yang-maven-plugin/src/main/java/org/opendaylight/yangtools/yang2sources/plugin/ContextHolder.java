/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

final class ContextHolder implements Immutable {
    private final SchemaContext context;
    private final Set<Module> modules;
    private final Set<SourceIdentifier> sources;

    ContextHolder(final SchemaContext context, final Set<Module> modules, final Set<SourceIdentifier> sources) {
        this.context = Preconditions.checkNotNull(context);
        this.modules = ImmutableSet.copyOf(modules);
        this.sources = ImmutableSet.copyOf(sources);
    }

    SchemaContext getContext() {
        return context;
    }

    Set<Module> getYangModules() {
        return modules;
    }

    Optional<String> moduleToResourcePath(final Module mod) {
        final SourceIdentifier id = Util.moduleToIdentifier(mod);
        return sources.contains(id)
                ? Optional.of("/" + YangToSourcesProcessor.META_INF_YANG_STRING_JAR + "/" + id.toYangFilename())
                        : Optional.empty();
    }
}
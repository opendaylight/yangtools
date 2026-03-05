/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Resolved linkage for a {@link SourceSpecificContext}.
 *
 * @param importedModules the {@link SourceSpecificContext} accessible by being imported
 * @param includedSubmodules the {@link SourceSpecificContext} accessible by being included
 * @param belongsTo the {@link SourceSpecificContext} accessible by being the {@code belongs-to} module
 */
@NonNullByDefault
record SourceLinkage(
        Map<Unqualified, SourceSpecificContext> importedModules,
        Set<SourceSpecificContext> includedSubmodules,
        @Nullable Entry<Unqualified, SourceSpecificContext> belongsTo) {
    SourceLinkage {
        importedModules = Map.copyOf(importedModules);
        includedSubmodules = Set.copyOf(includedSubmodules);
        final var local = belongsTo;
        if (local != null) {
            requireNonNull(local.getKey());
            requireNonNull(local.getValue());
        }
    }

    SourceLinkage(final Map<Unqualified, SourceSpecificContext> importedModules,
            final Set<SourceSpecificContext> includedSubmodules) {
        this(importedModules, includedSubmodules, null);
    }
}

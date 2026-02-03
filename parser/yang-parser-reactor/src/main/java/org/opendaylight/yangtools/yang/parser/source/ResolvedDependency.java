/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A resolved {@link SourceDependency}.
 */
@NonNullByDefault
public sealed interface ResolvedDependency extends Immutable {
    /**
     * A resolved {@link BelongsTo}.
     */
    record ResolvedBelongsTo(BelongsTo dependency, QNameModule parentModuleQname) implements ResolvedDependency {
        public ResolvedBelongsTo {
            requireNonNull(dependency);
            requireNonNull(parentModuleQname);
        }
    }

    /**
     * A resolved {@link Import}.
     */
    record ResolvedImport(
            Import dependency,
            SourceIdentifier sourceId,
            QNameModule qname) implements ResolvedDependency {
        public ResolvedImport {
            requireNonNull(dependency);
            requireNonNull(sourceId);
            requireNonNull(qname);
        }
    }

    /**
     * A resolved {@link Include}.
     */
    record ResolvedInclude(
            Include dependency,
            SourceIdentifier sourceId,
            QNameModule qname) implements ResolvedDependency {
        public ResolvedInclude {
            requireNonNull(dependency);
            requireNonNull(sourceId);
            requireNonNull(qname);
        }
    }

    /**
     * {@return the {@link SourceDependency} which is resolved}
     */
    SourceDependency dependency();
}

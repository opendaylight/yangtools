/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;

/**
 * Common interface expressing a dependency on a source, be it a {@link ModuleStatement}
 * or a {@link SubmoduleStatement}.
 */
@NonNullByDefault
public sealed interface SourceDependency extends Serializable
        permits SourceDependency.Import, SourceDependency.Include, SourceDependency.BelongsTo {
    /**
     * The name of the required source.
     *
     * @return name of the required source
     */
    Unqualified name();

    /**
     * Returns required source revision. If specified, this dependency can be satisfied only by the specified revision
     * or its semantic equivalent (think semantic version of imports). If unspecified, this dependency can be satisfied
     * by any source with a matching {@link #name()}.
     *
     * @return required source revision, {@code null} if unspecified
     */
    @Nullable Revision revision();

    /**
     * A dependency created by a {@link BelongsToStatement}.
     */
    record BelongsTo(Unqualified name, Unqualified prefix) implements SourceDependency {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public BelongsTo {
            requireNonNull(name);
            requireNonNull(prefix);
        }

        @Override
        public @Nullable Revision revision() {
            return null;
        }
    }

    /**
     * A dependency created by an {@link ImportStatement}.
     */
    record Import(Unqualified name, Unqualified prefix, @Nullable Revision revision) implements SourceDependency {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public Import {
            requireNonNull(name);
            requireNonNull(prefix);
        }

        public Import(final Unqualified name, final Unqualified prefix) {
            this(name, prefix, null);
        }
    }

    /**
     * A dependency created by an {@link IncludeStatement}.
     */
    record Include(Unqualified name, @Nullable Revision revision) implements SourceDependency {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public Include {
            requireNonNull(name);
        }

        public Include(final Unqualified name) {
            this(name, null);
        }
    }
}

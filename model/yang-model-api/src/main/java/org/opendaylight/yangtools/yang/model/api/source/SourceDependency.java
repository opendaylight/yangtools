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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
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
     * <p>Satisfaction criteria can be easily be valuated via {@link #isSatisfiedBy(SourceIdentifier)}.
     *
     * @return required source revision, {@code null} if unspecified
     */
    @Nullable Revision revision();

    /**
     * {@return the {@code StatementSourceReference} to the statement causing this dependency, or {@code null} if not
     * available}
     * @since 15.0.0
     */
    @Nullable StatementSourceReference sourceRef();

    /**
     * Check if a given {@link SourceIdentifier} satisfies the needs of this dependency.
     *
     * @param sourceId given {@link SourceIdentifier}
     * @return {@code true} if the {@link SourceIdentifier} satisfies this dependency
     * @throws NullPointerException if {@code sourceId} is {@code null}
     */
    default boolean isSatisfiedBy(final SourceIdentifier sourceId) {
        if (name().equals(sourceId.name())) {
            final var revision = revision();
            if (revision == null || revision.equals(sourceId.revision())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A dependency created by a {@link BelongsToStatement}.
     */
    record BelongsTo(
            Unqualified name,
            Unqualified prefix,
            @Nullable StatementSourceReference sourceRef) implements SourceDependency {
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

        @Override
        public int hashCode() {
            return Objects.hash(name, prefix);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof BelongsTo other && name.equals(other.name)
                && prefix.equals(other.prefix);
        }

        @Override
        public String toString() {
            return new StringBuilder().append("BelongsTo[name=").append(name.getLocalName())
                .append(", prefix=").append(prefix.getLocalName())
                .append(']').toString();
        }

        @java.io.Serial
        Object writeReplace() {
            return new DBTv1(this);
        }
    }

    /**
     * A dependency created by an {@link ImportStatement}.
     */
    record Import(
            Unqualified name,
            Unqualified prefix,
            @Nullable Revision revision,
            @Nullable StatementSourceReference sourceRef) implements SourceDependency {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public Import {
            requireNonNull(name);
            requireNonNull(prefix);
        }

        public Import(final Unqualified name, final Unqualified prefix,
                final @Nullable StatementSourceReference sourceRef) {
            this(name, prefix, null, sourceRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, prefix, revision);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof Import other && name.equals(other.name) && prefix.equals(other.prefix)
                && Objects.equals(revision, other.revision);
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder().append("Import[name=").append(name.getLocalName())
                .append(", prefix=").append(prefix.getLocalName());
            final var rev = revision;
            if (rev != null) {
                sb.append(", revision=").append(revision);
            }
            return sb.append(']').toString();
        }

        @java.io.Serial
        Object writeReplace() {
            return new DIMv1(this);
        }
    }

    /**
     * A dependency created by an {@link IncludeStatement}.
     */
    record Include(
            Unqualified name,
            @Nullable Revision revision,
            @Nullable StatementSourceReference sourceRef) implements SourceDependency {
        @java.io.Serial
        private static final long serialVersionUID = 0L;

        public Include {
            requireNonNull(name);
        }

        public Include(final Unqualified name, final @Nullable StatementSourceReference sourceRef) {
            this(name, null, sourceRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, revision);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof Include other && name.equals(other.name)
                && Objects.equals(revision, other.revision);
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder().append("Include[name=").append(name.getLocalName());
            final var rev = revision;
            if (rev != null) {
                sb.append(", revision=").append(revision);
            }
            return sb.append(']').toString();
        }

        @java.io.Serial
        Object writeReplace() {
            return new DINv1(this);
        }
    }
}

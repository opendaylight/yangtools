/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementException;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.meta.UncheckedStatementException;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RootDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;

/**
 * Linkage information about a particular {@link SourceRepresentation}. It has two specializations
 * <ol>
 *   <li>{@link SourceInfo.Module} pertaining to {@link SourceRepresentation} which have {@code module} as its root
 *       statement</li>
 *   <li>{@link SourceInfo.Submodule} pertaining to {@link SourceRepresentation} which have {@code submodule} as its
 *       root statement</li>
 * </ol>
 *
 * <p>This interface captures the basic metadata needed for interpretation and linkage of the source, as represented by
 * the following ABNF constructs placed at the start of a YANG file:
 * <ul>
 *   <li>{@code module-header-stmts} or {@code submodule-header-stmts}</li>
 *   <li>{@code linkage-stmts}</li>
 *   <li>{@code revision-stmts}<li>
 * </ul>
 */
@NonNullByDefault
public sealed interface SourceInfo permits SourceInfo.Module, SourceInfo.Submodule {
    /**
     * Return the {@link SourceIdentifier} of this source, as expressed by {@link RootDeclaredStatement#argument()}
     * contract coupled with the first entry in {@link #revisions()}.
     *
     * @return name of this source.
     */
    SourceIdentifier sourceId();

    /**
     * Return {@link YangVersion} of the source. If no {@link YangVersionStatement} is present, this method will return
     * {@link YangVersion#VERSION_1}.
     *
     * @return {@link YangVersion} of the source
     */
    YangVersion yangVersion();

    /**
     * The set of all {@link RevisionDateStatement} mentioned in {@link RevisionStatement}s. The returned set is ordered
     * in reverse order, i.e. newest revision is encountered first.
     *
     * @return all revisions known by this source
     */
    ImmutableSet<Revision> revisions();

    /**
     * Return all {@link Import} dependencies.
     *
     * @return all import dependencies
     */
    ImmutableSet<Import> imports();

    /**
     * Return all {@link Include} dependencies.
     *
     * @return all include dependencies
     */
    ImmutableSet<Include> includes();

    /**
     * A {@link SourceInfo} about a {@link ModuleStatement}-backed source.
     */
    record Module(
            SourceIdentifier sourceId,
            YangVersion yangVersion,
            XMLNamespace namespace,
            Unqualified prefix,
            ImmutableSet<Revision> revisions,
            ImmutableSet<Import> imports,
            ImmutableSet<Include> includes) implements SourceInfo {
        public Module {
            requireNonNull(sourceId);
            requireNonNull(yangVersion);
            requireNonNull(namespace);
            requireNonNull(prefix);
            requireNonNull(revisions);
            requireNonNull(imports);
            requireNonNull(includes);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder extends SourceInfo.Builder<Builder, Module> {
            @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
                justification = "https://github.com/spotbugs/spotbugs/issues/743")
            private @Nullable XMLNamespace namespace;
            @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
                justification = "https://github.com/spotbugs/spotbugs/issues/743")
            private @Nullable Unqualified prefix;

            Builder() {
                // Hidden on purpose
            }

            public Builder setNamespace(final XMLNamespace namespace) {
                this.namespace = requireNonNull(namespace);
                return this;
            }

            public Builder setPrefix(final Unqualified prefix) {
                this.prefix = requireNonNull(prefix);
                return this;
            }

            @Override
            Module buildInstance(final SourceIdentifier sourceId, final YangVersion yangVersion,
                    final ImmutableSet<Revision> revisions, final ImmutableSet<Import> imports,
                    final ImmutableSet<Include> includes) {
                return new Module(sourceId, yangVersion, requireNonNull(namespace), requireNonNull(prefix), revisions,
                    imports, includes);
            }
        }
    }

    /**
     * A {@link SourceInfo} about a {@code submodule}.
     */
    record Submodule(
            SourceIdentifier sourceId,
            YangVersion yangVersion,
            BelongsTo belongsTo,
            ImmutableSet<Revision> revisions,
            ImmutableSet<Import> imports,
            ImmutableSet<Include> includes) implements SourceInfo {
        public Submodule {
            requireNonNull(sourceId);
            requireNonNull(yangVersion);
            requireNonNull(belongsTo);
            requireNonNull(revisions);
            requireNonNull(imports);
            requireNonNull(imports);
            requireNonNull(includes);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder extends SourceInfo.Builder<Builder, Submodule> {
            @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
                justification = "https://github.com/spotbugs/spotbugs/issues/743")
            private @Nullable BelongsTo belongsTo;

            Builder() {
                // Hidden on purpose
            }

            public Builder setBelongsTo(final BelongsTo belongsTo) {
                this.belongsTo = requireNonNull(belongsTo);
                return this;
            }

            @Override
            Submodule buildInstance(final SourceIdentifier sourceId, final YangVersion yangVersion,
                    final ImmutableSet<Revision> revisions, final ImmutableSet<Import> imports,
                    final ImmutableSet<Include> includes) {
                return new Submodule(sourceId, yangVersion, requireNonNull(belongsTo), revisions, imports, includes);
            }
        }
    }

    abstract sealed class Builder<B extends Builder<B, I>, I extends SourceInfo> {
        private final ImmutableSet.Builder<Import> imports = ImmutableSet.builder();
        private final ImmutableSet.Builder<Include> includes = ImmutableSet.builder();
        private final ArrayList<Revision> revisions = new ArrayList<>();
        private YangVersion yangVersion = YangVersion.VERSION_1;
        @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "https://github.com/spotbugs/spotbugs/issues/743")
        private @Nullable Unqualified name;

        public final B setName(final Unqualified newName) {
            name = requireNonNull(newName);
            return thisInstance();
        }

        public final B setYangVersion(final YangVersion newYangVersion) {
            yangVersion = requireNonNull(newYangVersion);
            return thisInstance();
        }

        public final B addImport(final Import importDep) {
            imports.add(importDep);
            return thisInstance();
        }

        public final B addInclude(final Include includeDep) {
            includes.add(includeDep);
            return thisInstance();
        }

        public final B addRevision(final Revision revision) {
            revisions.add(revision);
            return thisInstance();
        }

        public final I build() {
            final var sorted = revisions.stream()
                .sorted(Comparator.reverseOrder())
                .collect(ImmutableSet.toImmutableSet());

            return buildInstance(
                new SourceIdentifier(requireNonNull(name), sorted.isEmpty() ? null : sorted.iterator().next()),
                yangVersion, sorted, imports.build(), includes.build());
        }

        abstract I buildInstance(SourceIdentifier sourceId, YangVersion yangVersion, ImmutableSet<Revision> revisions,
            ImmutableSet<Import> imports, ImmutableSet<Include> includes);

        @SuppressWarnings("unchecked")
        private B thisInstance() {
            return (B) this;
        }
    }

    /**
     * An entity capable of extracting {@link SourceInfo} from its state.
     */
    interface Extractor {
        /**
         * {@return extracted {@link SourceInfo}}
         * @throws ExtractorException if the {@link SourceInfo} cannot be extracted
         */
        SourceInfo extractSourceInfo() throws ExtractorException;
    }

    /**
     * A {@link StatementException} reported by when the {@link SourceInfo} cannot be extracted. Subclasses of this
     * exception are not serializable.
     */
    abstract sealed class ExtractorException extends StatementException
            permits ExtractorInvalidRootException, ExtractorMalformedArgumentException,
                    ExtractorMissingArgumentException, ExtractorMissingStatementException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }

    /**
     * An {@link ExtractorException} reported when the {@link SourceRepresentation}'s top-level statement is not a
     * {@code module} or a {@code submodule}. Instances of this exception are not serializable.
     */
    final class ExtractorInvalidRootException extends ExtractorException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }

    /**
     * An {@link ExtractorException} reported when a YANG statement has a malformed argument. Instances of this
     * exception are not serializable.
     */
    final class ExtractorMalformedArgumentException extends ExtractorException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public ExtractorMalformedArgumentException(final StatementSourceReference sourceRef, final String statement,
                final Exception cause) {

        }
    }

    /**
     * An {@link ExtractorException} reported when a YANG statement is missing its argument. Instances of this
     * exception are not serializable.
     */
    final class ExtractorMissingArgumentException extends ExtractorException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public ExtractorMissingArgumentException(final StatementSourceReference sourceRef, final String statement) {

        }
    }

    /**
     * An {@link ExtractorException} reported when a required YANG statement is missing. Instances of this exception are
     * not serializable.
     */
    final class ExtractorMissingStatementException extends ExtractorException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public ExtractorMissingStatementException(final StatementSourceReference sourceRef, final String statement) {

        }
    }

    /**
     * An {@link UncheckedStatementException} wrapping an {@link ExtractorException}. Instances of this exception are
     * not serializable.
     */
    final class UncheckedExtractorException extends UncheckedStatementException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        public UncheckedExtractorException(final ExtractorException cause) {
            super(cause);
        }
    }
}

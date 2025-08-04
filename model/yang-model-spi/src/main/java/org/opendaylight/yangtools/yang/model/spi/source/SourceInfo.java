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
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
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
    Referenced<YangVersion> yangVersion();

    /**
     * The set of all {@link RevisionDateStatement} mentioned in {@link RevisionStatement}s. The returned set is ordered
     * in reverse order, i.e. newest revision is encountered first.
     *
     * @return all revisions known by this source
     */
    ImmutableSet<Referenced<Revision>> revisions();

    /**
     * Return all {@link Import} dependencies.
     *
     * @return all import dependencies
     */
    ImmutableSet<Referenced<Import>> imports();

    /**
     * Return all {@link Include} dependencies.
     *
     * @return all include dependencies
     */
    ImmutableSet<Referenced<Include>> includes();

    IRStatement rootStatement();

    /**
     * A {@link SourceInfo} about a {@link ModuleStatement}-backed source.
     */
    record Module(
            IRStatement rootStatement,
            SourceIdentifier sourceId,
            Referenced<YangVersion> yangVersion,
            Referenced<XMLNamespace> namespace,
            Referenced<Unqualified> prefix,
            ImmutableSet<Referenced<Revision>> revisions,
            ImmutableSet<Referenced<Import>> imports,
            ImmutableSet<Referenced<Include>> includes) implements SourceInfo {
        public Module {
            //TODO: not required just for testing
//            requireNonNull(rootStatement);
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
            private @Nullable Referenced<XMLNamespace> namespace;
            @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
                justification = "https://github.com/spotbugs/spotbugs/issues/743")
            private @Nullable Referenced<Unqualified> prefix;

            Builder() {
                // Hidden on purpose
            }

            public Builder setNamespace(final XMLNamespace namespace, final StatementSourceReference ref) {
                this.namespace = new Referenced<>(requireNonNull(namespace), requireNonNull(ref));
                return this;
            }

            public Builder setPrefix(final Unqualified prefix, final StatementSourceReference ref) {
                this.prefix = new Referenced<>(requireNonNull(prefix), requireNonNull(ref));
                return this;
            }

            @Override
            Module buildInstance(final IRStatement root, final SourceIdentifier sourceId, final Referenced<YangVersion> yangVersion,
                    final ImmutableSet<Referenced<Revision>> revisions, final ImmutableSet<Referenced<Import>> imports,
                    final ImmutableSet<Referenced<Include>> includes) {
                return new Module(root, sourceId, yangVersion, requireNonNull(namespace), requireNonNull(prefix),
                        revisions, imports, includes);
            }
        }
    }

    /**
     * A {@link SourceInfo} about a {@code submodule}.
     */
    record Submodule(
            IRStatement rootStatement,
            SourceIdentifier sourceId,
            Referenced<YangVersion> yangVersion,
            Referenced<BelongsTo> belongsTo,
            ImmutableSet<Referenced<Revision>> revisions,
            ImmutableSet<Referenced<Import>> imports,
            ImmutableSet<Referenced<Include>> includes) implements SourceInfo {
        public Submodule {
//            requireNonNull(rootStatement);
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
            private @Nullable Referenced<BelongsTo> belongsTo;

            Builder() {
                // Hidden on purpose
            }

            public Builder setBelongsTo(final BelongsTo belongsTo, StatementSourceReference ref) {
                this.belongsTo = new Referenced<>(requireNonNull(belongsTo), requireNonNull(ref));
                return this;
            }

            @Override
            Submodule buildInstance(final IRStatement root, final SourceIdentifier sourceId, final Referenced<YangVersion> yangVersion,
                    final ImmutableSet<Referenced<Revision>> revisions, final ImmutableSet<Referenced<Import>> imports,
                    final ImmutableSet<Referenced<Include>> includes) {
                return new Submodule(root, sourceId, yangVersion, requireNonNull(belongsTo), revisions, imports, includes);
            }
        }
    }

    abstract sealed class Builder<B extends Builder<B, I>, I extends SourceInfo> {
        private final ImmutableSet.Builder<Referenced<Import>> imports = ImmutableSet.builder();
        private final ImmutableSet.Builder<Referenced<Include>> includes = ImmutableSet.builder();
        private final ArrayList<Referenced<Revision>> revisions = new ArrayList<>();
        private Referenced<YangVersion> yangVersion = new Referenced<>(YangVersion.VERSION_1, null);
        @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "https://github.com/spotbugs/spotbugs/issues/743")
        private @Nullable Unqualified name;
        private IRStatement rootStatement;

        public final B setRootStatement(final IRStatement root) {
            rootStatement = requireNonNull(root);
            return thisInstance();
        }

        public final B setName(final Unqualified newName) {
            name = requireNonNull(newName);
            return thisInstance();
        }

        public final B setYangVersion(final YangVersion newYangVersion, StatementSourceReference ref) {
            yangVersion = new Referenced<>(requireNonNull(newYangVersion), requireNonNull(ref));
            return thisInstance();
        }

        public final B addImport(final Import importDep, StatementSourceReference ref) {
            imports.add(new Referenced<>(importDep, requireNonNull(ref)));
            return thisInstance();
        }

        public final B addInclude(final Include includeDep, StatementSourceReference ref) {
            includes.add(new Referenced<>(includeDep, requireNonNull(ref)));
            return thisInstance();
        }

        public final B addRevision(final Revision revision, StatementSourceReference ref) {
            revisions.add(new Referenced<>(revision, requireNonNull(ref)));
            return thisInstance();
        }

        public final I build() {
            final var sorted = revisions.stream()
                .sorted((first, second) -> second.value().compareTo(first.value()))
                .collect(ImmutableSet.toImmutableSet());

            return buildInstance(rootStatement,
                new SourceIdentifier(requireNonNull(name), sorted.isEmpty() ? null :
                    sorted.iterator().next().value()),
                yangVersion, sorted, imports.build(), includes.build());
        }

        abstract I buildInstance(IRStatement root, SourceIdentifier sourceId, Referenced<YangVersion> yangVersion,
            ImmutableSet<Referenced<Revision>> revisions,
            ImmutableSet<Referenced<Import>> imports, ImmutableSet<Referenced<Include>> includes);

        @SuppressWarnings("unchecked")
        private B thisInstance() {
            return (B) this;
        }
    }
}

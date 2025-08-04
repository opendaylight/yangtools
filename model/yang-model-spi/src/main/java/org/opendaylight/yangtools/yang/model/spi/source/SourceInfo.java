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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
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
    Referenced<SourceIdentifier> sourceId();

    /**
     * Return {@link YangVersion} of the source. If no {@link YangVersionStatement} is present, this method will return
     * {@link YangVersion#VERSION_1}.
     *
     * @return {@link YangVersion} of the source
     */
    Referenced<YangVersion> yangVersion();

    Referenced<String> contact();

    Referenced<String> organization();

    Referenced<String> description();

    Referenced<String> reference();



    /**
     * The set of all {@link RevisionDateStatement} mentioned in {@link RevisionStatement}s. The returned set is ordered
     * in reverse order, i.e. newest revision is encountered first.
     *
     * @return all revisions known by this source
     */
    ImmutableSet<DetailedRevision> revisions();

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
        Referenced<SourceIdentifier> sourceId,
        Referenced<YangVersion> yangVersion,
        Referenced<XMLNamespace> namespace,
        Referenced<Unqualified> prefix,
        ImmutableSet<DetailedRevision> revisions,
        ImmutableSet<Import> imports,
        ImmutableSet<Include> includes,
        Referenced<String> contact,
        Referenced<String> organization,
        Referenced<String> description,
        Referenced<String> reference) implements SourceInfo {
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

            public Builder setNamespace(final Referenced<XMLNamespace> referencedNamespace) {
                this.namespace = referencedNamespace;
                return this;
            }

            public Builder setPrefix(final Unqualified prefix, final StatementSourceReference ref) {
                this.prefix = new Referenced<>(requireNonNull(prefix), requireNonNull(ref));
                return this;
            }

            public Builder setPrefix(final Referenced<Unqualified> prefix) {
                this.prefix = prefix;
                return this;
            }

            @Override
            Module buildInstance(Referenced<SourceIdentifier> sourceId, Referenced<YangVersion> yangVersion,
                ImmutableSet<DetailedRevision> revisions, ImmutableSet<Import> imports,
                ImmutableSet<Include> includes, Referenced<String> contact, Referenced<String> organization,
                Referenced<String> description, Referenced<String> reference) {
                return new Module(sourceId, yangVersion, requireNonNull(namespace), requireNonNull(prefix), revisions,
                    imports, includes, contact, organization, description, reference);
            }
        }
    }

    /**
     * A {@link SourceInfo} about a {@code submodule}.
     */
    record Submodule(
        Referenced<SourceIdentifier> sourceId,
        Referenced<YangVersion> yangVersion,
        BelongsTo belongsTo,
        ImmutableSet<DetailedRevision> revisions,
        ImmutableSet<Import> imports,
        ImmutableSet<Include> includes,
        Referenced<String> contact,
        Referenced<String> organization,
        Referenced<String> description,
        Referenced<String> reference) implements SourceInfo {
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
            Submodule buildInstance(Referenced<SourceIdentifier> sourceId, Referenced<YangVersion> yangVersion,
                ImmutableSet<DetailedRevision> revisions, ImmutableSet<Import> imports, ImmutableSet<Include> includes,
                Referenced<String> contact, Referenced<String> organization, Referenced<String> description,
                Referenced<String> reference) {
                return new Submodule(sourceId, yangVersion, requireNonNull(belongsTo), revisions, imports, includes,
                    contact, organization, description, reference);
            }
        }
    }

    abstract sealed class Builder<B extends Builder<B, I>, I extends SourceInfo> {
        private final ImmutableSet.Builder<Import> imports = ImmutableSet.builder();
        private final ImmutableSet.Builder<Include> includes = ImmutableSet.builder();
        private final ArrayList<DetailedRevision> revisions = new ArrayList<>();
        private Referenced<YangVersion> yangVersion = new Referenced<>(YangVersion.VERSION_1, null);
        @SuppressFBWarnings(value = "NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR",
            justification = "https://github.com/spotbugs/spotbugs/issues/743")
        private @Nullable Referenced<Unqualified> name;
        private @Nullable Referenced<String> contact;
        private @Nullable Referenced<String> organization;
        private @Nullable Referenced<String> description;
        private @Nullable Referenced<String> reference;

        public final B setName(final Unqualified newName, StatementSourceReference ref) {
            name = new Referenced<>(requireNonNull(newName), requireNonNull(ref));
            return thisInstance();
        }

        //TODO: we might end-up needing just one of these
        public final B setName(final Referenced<Unqualified> newName) {
            name = newName;
            return thisInstance();
        }



        public final B setYangVersion(final YangVersion newYangVersion, StatementSourceReference ref) {
            yangVersion = new Referenced<>(requireNonNull(newYangVersion), requireNonNull(ref));
            return thisInstance();
        }

        public final B setYangVersion(final Referenced<YangVersion> newYangVersion) {
            yangVersion = requireNonNull(newYangVersion);
            return thisInstance();
        }

        public final B setContact(final String newContact, StatementSourceReference ref) {
            contact = new Referenced<>(requireNonNull(newContact), requireNonNull(ref));
            return thisInstance();
        }

        public final B setContact(final Referenced<String> newContact) {
            contact = newContact;
            return thisInstance();
        }

        public final B setOrganization(final Referenced<String> newOrganization) {
            organization = newOrganization;
            return thisInstance();
        }

//        public final B setOrganization(final String newOrganization, StatementSourceReference ref) {
//            organization = new Referenced<>(requireNonNull(newOrganization), requireNonNull(ref));
//            return thisInstance();
//        }

        public final B setDescription(final String newDescription, StatementSourceReference ref) {
            description = new Referenced<>(requireNonNull(newDescription), requireNonNull(ref));
            return thisInstance();
        }

        public final B setDescription(final Referenced<String> newDescription) {
            description = newDescription;
            return thisInstance();
        }

        public final B setReference(final String newReference, StatementSourceReference ref) {
            reference = new Referenced<>(requireNonNull(newReference), requireNonNull(ref));
            return thisInstance();
        }

        public final B setReference(final Referenced<String> newReference) {
            reference = newReference;
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

        public final B addRevision(final DetailedRevision revision) {
            revisions.add(revision);
            return thisInstance();
        }

        public final I build() {
            final var sorted = revisions.stream()
                .sorted((first, second) -> second.getRevision().value().compareTo(first.getRevision().value()))
                .collect(ImmutableSet.toImmutableSet());
            return buildInstance(new Referenced<>(new SourceIdentifier(name.value(), sorted.isEmpty() ? null :
                    sorted.iterator().next().getRevision().value()), name.reference()),
                yangVersion, sorted, imports.build(), includes.build(), contact, organization, description, reference);
        }

        abstract I buildInstance(Referenced<SourceIdentifier> sourceId, Referenced<YangVersion> yangVersion,
            ImmutableSet<DetailedRevision> revisions, ImmutableSet<Import> imports,
            ImmutableSet<Include> includes, Referenced<String> contact, Referenced<String> organization, Referenced<String> description,
            Referenced<String> reference);

        @SuppressWarnings("unchecked")
        private B thisInstance() {
            return (B) this;
        }
    }
}

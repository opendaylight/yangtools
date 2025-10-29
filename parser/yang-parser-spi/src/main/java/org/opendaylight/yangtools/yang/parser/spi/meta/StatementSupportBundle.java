/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import java.util.HashMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consistent set of {@link StatementSupport}s and {@link ParserNamespace} {@link NamespaceBehaviour}s.
 */
public final class StatementSupportBundle implements Immutable {
    /**
     * The set of versions including all versions known as of RFC7950, e.g. {@link YangVersion#VERSION_1}
     * and {@link YangVersion#VERSION_1_1}.
     *
     * @since 14.0.20
     */
    @NonNullByDefault
    public static final ImmutableSet<YangVersion> VERSIONS_RFC7950 =
        // Note: we could do Sets.immutableEnumSet(), but we really have only 2 versions, so going through EnumSet is
        //       wasteful. We have the same iteration order anyway.
        ImmutableSet.of(YangVersion.VERSION_1, YangVersion.VERSION_1_1);

    /**
     * The set of versions including all {@link YangVersion}s.
     *
     * @since 14.0.20
     */
    @NonNullByDefault
    public static final ImmutableSet<YangVersion> VERSIONS_ALL = VERSIONS_RFC7950;

    static {
        // Consistency check for when YangVersion expands
        for (var version : YangVersion.values()) {
            verify(VERSIONS_ALL.contains(version), "VERSIONS_ALL does not include %s", version);
        }
    }

    private static final StatementSupportBundle EMPTY = new StatementSupportBundle(null, ImmutableSet.of(),
            ImmutableMap.of(), ImmutableMap.of(), ImmutableTable.of());

    private final StatementSupportBundle parent;
    private final @NonNull ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions;
    private final @NonNull ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificDefinitions;
    private final @NonNull ImmutableMap<ParserNamespace<?, ?>, NamespaceBehaviour<?, ?>> namespaceDefinitions;
    private final @NonNull ImmutableSet<YangVersion> supportedVersions;

    private StatementSupportBundle(final StatementSupportBundle parent,
            final ImmutableSet<YangVersion> supportedVersions,
            final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions,
            final ImmutableMap<ParserNamespace<?, ?>, NamespaceBehaviour<?, ?>> namespaceDefinitions,
            final ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificDefinitions) {
        this.parent = parent;
        this.supportedVersions = requireNonNull(supportedVersions);
        this.commonDefinitions = requireNonNull(commonDefinitions);
        this.namespaceDefinitions = requireNonNull(namespaceDefinitions);
        this.versionSpecificDefinitions = requireNonNull(versionSpecificDefinitions);
    }

    /**
     * {@return a new {@link Builder} working with {@link #VERSIONS_ALL}}
     * @since 14.0.20
     */
    public static @NonNull Builder builder() {
        return builder(VERSIONS_ALL);
    }

    /**
     * Return a new {@link Builder} working with specified supported {@link YangVersion}s.
     *
     * @param supportedVersions supported versions
     * @return A new {@link Builder}
     */
    public static @NonNull Builder builder(final Set<YangVersion> supportedVersions) {
        return builder(ImmutableSet.copyOf(supportedVersions));
    }

    /**
     * Return a new {@link Builder} working with specified supported {@link YangVersion}s.
     *
     * @param supportedVersions supported versions
     * @return A new {@link Builder}
     */
    public static @NonNull Builder builder(final ImmutableSet<YangVersion> supportedVersions) {
        return new Builder(supportedVersions, EMPTY);
    }

    /**
     * Return a new {@link Builder} for a {@link StatementSupportBundle} derived from specific parent.
     *
     * @param parent the parent
     * @return A new {@link Builder}
     * @since 14.0.20
     */
    public static @NonNull Builder builderDerivedFrom(final StatementSupportBundle parent) {
        return new Builder(parent.getSupportedVersions(), parent);
    }

    /**
     * Return a new {@link Builder} for a {@link StatementSupportBundle} derived from specific parent.
     *
     * @param parent the parent
     * @return A new {@link Builder}
     * @deprecated Use {@link #builderDerivedFrom(StatementSupportBundle)} instead.
     */
    @Deprecated(since = "14.0.20", forRemoval = true)
    public static @NonNull Builder derivedFrom(final StatementSupportBundle parent) {
        return builderDerivedFrom(parent);
    }

    /**
     * {@return statement definitions common for all versions}
     */
    public @NonNull ImmutableMap<QName, StatementSupport<?, ?, ?>> getCommonDefinitions() {
        return commonDefinitions;
    }

    /**
     * Returns statement definitions specific for requested version. Result of this method does nit include common
     * statement definitions.
     *
     * @param version requested version
     * @return map of statement definitions specific for requested version, it does not include common statement
     *         definitions
     */
    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getDefinitionsSpecificForVersion(final YangVersion version) {
        return versionSpecificDefinitions.row(version);
    }

    /**
     * Returns all version specific statement definitions. Result of this method does not include common statement
     * definitions.
     *
     * @return table of all version specific statement definitions, it does not include common statement definitions
     */
    public @NonNull ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> getAllVersionSpecificDefinitions() {
        return versionSpecificDefinitions;
    }

    public @NonNull ImmutableMap<ParserNamespace<?, ?>, NamespaceBehaviour<?, ?>> getNamespaceDefinitions() {
        return namespaceDefinitions;
    }

    /**
     * {@return the set of all {@link YangVersion}s supported by this bundle}
     */
    public @NonNull ImmutableSet<YangVersion> getSupportedVersions() {
        return supportedVersions;
    }

    public <K, V> @Nullable NamespaceBehaviour<K, V> namespaceBehaviourOf(final ParserNamespace<K, V> namespace) {
        final var potential = namespaceDefinitions.get(namespace);
        if (potential == null) {
            return parent == null ? null : parent.namespaceBehaviourOf(namespace);
        }

        checkState(namespace.equals(potential.namespace()));
        // Safe cast, previous checkState checks equivalence of key from which type argument are derived
        @SuppressWarnings("unchecked")
        final var ret = (NamespaceBehaviour<K, V>) potential;
        return ret;
    }

    public boolean hasNamespaceBehaviour(final ParserNamespace<?, ?> namespace) {
        if (namespaceDefinitions.containsKey(namespace)) {
            return true;
        }
        return parent == null ? false : parent.hasNamespaceBehaviour(namespace);
    }

    public @Nullable StatementSupport<?, ?, ?> getStatementDefinition(final YangVersion version, final QName stmtName) {
        final var versionSpecific = getVersionSpecificStatementDefinition(version, stmtName);
        return versionSpecific != null ? versionSpecific : getCommonStatementDefinition(stmtName);
    }

    private @Nullable StatementSupport<?, ?, ?> getCommonStatementDefinition(final QName stmtName) {
        final var potential = commonDefinitions.get(stmtName);
        if (potential != null) {
            return potential;
        }
        return parent == null ? null : parent.getCommonStatementDefinition(stmtName);
    }

    private @Nullable StatementSupport<?, ?, ?> getVersionSpecificStatementDefinition(final YangVersion version,
            final QName stmtName) {
        final var potential = versionSpecificDefinitions.get(version, stmtName);
        if (potential != null) {
            return potential;
        }
        return parent == null ? null : parent.getVersionSpecificStatementDefinition(version, stmtName);
    }

    /**
     * A builder for {@link StatementSupportBundle}s.
     */
    public static final class Builder implements Mutable {
        private static final Logger LOG = LoggerFactory.getLogger(Builder.class);

        private final HashMap<QName, StatementSupport<?, ?, ?>> commonStatements = new HashMap<>();
        private final HashBasedTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificStatements =
            HashBasedTable.create();
        private final HashMap<ParserNamespace<?, ?>, NamespaceBehaviour<?, ?>> namespaces = new HashMap<>();
        private final @NonNull ImmutableSet<YangVersion> supportedVersions;

        private StatementSupportBundle parent;

        Builder(final ImmutableSet<YangVersion> supportedVersions, final StatementSupportBundle parent) {
            this.parent = requireNonNull(parent);
            this.supportedVersions = requireNonNull(supportedVersions);
        }

        public @NonNull Builder addSupport(final StatementSupport<?, ?, ?> support) {
            final QName identifier = support.statementName();
            checkNoParentDefinition(identifier);

            checkState(!commonStatements.containsKey(identifier),
                    "Statement %s already defined in common statement bundle.", identifier);
            commonStatements.put(identifier, support);
            return this;
        }

        public @NonNull Builder addSupport(final NamespaceBehaviour<?, ?> namespaceSupport) {
            final var namespace = namespaceSupport.namespace();
            checkState(!namespaces.containsKey(namespace));
            checkState(!parent.hasNamespaceBehaviour(namespace));
            namespaces.put(namespace, namespaceSupport);
            return this;
        }

        public @NonNull Builder addVersionSpecificSupport(final YangVersion version,
                final StatementSupport<?, ?, ?> support) {
            checkArgument(supportedVersions.contains(requireNonNull(version)));

            final var identifier = support.statementName();
            checkState(!commonStatements.containsKey(identifier),
                    "Statement %s already defined in common statement bundle.", identifier);
            checkState(!versionSpecificStatements.contains(version, identifier),
                    "Statement %s already defined for version %s.", identifier, version);
            checkNoParentDefinition(identifier);
            checkState(parent.getVersionSpecificStatementDefinition(version, identifier) == null,
                    "Statement %s already defined for version %s in parent's statement bundle.", identifier, version);
            versionSpecificStatements.put(version, identifier, support);
            return this;
        }

        public @NonNull Set<YangVersion> getSupportedVersions() {
            return supportedVersions;
        }

        public @NonNull Builder setParent(final StatementSupportBundle parent) {
            this.parent = parent;
            return this;
        }

        public @NonNull Builder overrideSupport(final StatementSupport<?, ?, ?> support) {
            final var identifier = support.statementName();
            checkNoParentDefinition(identifier);

            final var previousSupport = commonStatements.replace(identifier, support);
            checkState(previousSupport != null, "Statement %s was not previously defined", identifier);
            LOG.debug("Changed statement {} support from {} to {}", identifier, previousSupport, support);
            return this;
        }

        /**
         * Create a {@link StatementSupportBundle} from the contents of this builder.
         *
         * @return A StatementSupportBundle
         * @throws IllegalStateException if parent has not been set
         */
        public @NonNull StatementSupportBundle build() {
            checkState(parent != null, "Parent must not be null");
            return new StatementSupportBundle(parent, supportedVersions, ImmutableMap.copyOf(commonStatements),
                    ImmutableMap.copyOf(namespaces), ImmutableTable.copyOf(versionSpecificStatements));
        }

        private void checkNoParentDefinition(final QName identifier) {
            checkState(parent.getCommonStatementDefinition(identifier) == null,
                    "Statement %s is defined in parent's common statement bundle", identifier);
        }
    }
}

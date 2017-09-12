/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public final class StatementSupportBundle implements Immutable, NamespaceBehaviour.Registry {

    private static final StatementSupportBundle EMPTY = new StatementSupportBundle(null, null, ImmutableMap.of(),
            ImmutableMap.of(), ImmutableTable.of());

    private final StatementSupportBundle parent;
    private final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions;
    private final ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificDefinitions;
    private final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaceDefinitions;
    private final Set<YangVersion> supportedVersions;

    private StatementSupportBundle(final StatementSupportBundle parent,
            final Set<YangVersion> supportedVersions,
            final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonStatements,
            final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces,
            final ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificStatements) {
        this.parent = parent;
        this.supportedVersions = supportedVersions;
        this.commonDefinitions = commonStatements;
        this.namespaceDefinitions = namespaces;
        this.versionSpecificDefinitions = versionSpecificStatements;
    }

    /**
     * Returns statement definitions common for all versions.
     *
     * @return map of common statement definitions
     */
    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getCommonDefinitions() {
        return commonDefinitions;
    }

    /**
     * Returns statement definitions specific for requested version. Result of this method does nit include common
     * statement definitions.
     *
     * @param version
     *            requested version
     * @return map of statement definitions specific for requested version, it
     *         doesn't include common statement definitions.
     */
    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getDefinitionsSpecificForVersion(final YangVersion version) {
        return versionSpecificDefinitions.row(version);
    }

    /**
     * Returns all version specific statement definitions. Result of this method does not include common statement
     * definitions.
     *
     * @return table of all version specific statement definitions, it doesn't
     *         include common statement definitions.
     */
    public ImmutableTable<YangVersion, QName, StatementSupport<?, ?, ?>> getAllVersionSpecificDefinitions() {
        return versionSpecificDefinitions;
    }

    public ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> getNamespaceDefinitions() {
        return namespaceDefinitions;
    }

    public static Builder builder(final Set<YangVersion> supportedVersions) {
        return new Builder(supportedVersions, EMPTY);
    }

    public static Builder derivedFrom(final StatementSupportBundle parent) {
        Preconditions.checkNotNull(parent);
        return new Builder(parent.getSupportedVersions(), parent);
    }

    public Set<YangVersion> getSupportedVersions() {
        return supportedVersions;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(
            final Class<N> namespace) throws NamespaceNotAvailableException {
        final NamespaceBehaviour<?, ?, ?> potential = namespaceDefinitions.get(namespace);
        if (potential != null) {
            Preconditions.checkState(namespace.equals(potential.getIdentifier()));

            // Safe cast, previous checkState checks equivalence of key from which type argument are derived
            return (NamespaceBehaviour<K, V, N>) potential;
        }
        if (parent != null) {
            return parent.getNamespaceBehaviour(namespace);
        }
        return null;
    }

    public <K, V, N extends IdentifierNamespace<K, V>> boolean hasNamespaceBehaviour(final Class<N> namespace) {
        if (namespaceDefinitions.containsKey(namespace)) {
            return true;
        }
        if (parent != null) {
            return parent.hasNamespaceBehaviour(namespace);
        }
        return false;
    }

    public StatementSupport<?, ?, ?> getStatementDefinition(final YangVersion version, final QName stmtName) {
        StatementSupport<?, ?, ?> result = getVersionSpecificStatementDefinition(version, stmtName);
        if (result == null) {
            result = getCommonStatementDefinition(stmtName);
        }

        return result;
    }

    private StatementSupport<?, ?, ?> getCommonStatementDefinition(final QName stmtName) {
        final StatementSupport<?, ?, ?> potential = commonDefinitions.get(stmtName);
        if (potential != null) {
            return potential;
        }
        if (parent != null) {
            return parent.getCommonStatementDefinition(stmtName);
        }
        return null;
    }

    private StatementSupport<?, ?, ?> getVersionSpecificStatementDefinition(final YangVersion version,
            final QName stmtName) {
        final StatementSupport<?, ?, ?> potential = versionSpecificDefinitions.get(version, stmtName);
        if (potential != null) {
            return potential;
        }

        if (parent != null) {
            return parent.getVersionSpecificStatementDefinition(version, stmtName);
        }
        return null;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<StatementSupportBundle> {
        private final Map<QName, StatementSupport<?, ?, ?>> commonStatements = new HashMap<>();
        private final Table<YangVersion, QName, StatementSupport<?, ?, ?>> versionSpecificStatements = HashBasedTable
                .create();
        private final Map<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces = new HashMap<>();

        private final Set<YangVersion> supportedVersions;
        private StatementSupportBundle parent;

        Builder(final Set<YangVersion> supportedVersions, final StatementSupportBundle parent) {
            this.parent = Preconditions.checkNotNull(parent);
            this.supportedVersions = ImmutableSet.copyOf(supportedVersions);
        }

        public Builder addSupport(final StatementSupport<?, ?, ?> definition) {
            final QName identifier = definition.getStatementName();
            Preconditions.checkState(!commonStatements.containsKey(identifier),
                    "Statement %s already defined in common statement bundle.", identifier);
            Preconditions.checkState(parent.getCommonStatementDefinition(identifier) == null,
                    "Statement %s already defined.", identifier);
            commonStatements.put(identifier, definition);
            return this;
        }

        public Builder addVersionSpecificSupport(final YangVersion version,
                final StatementSupport<?, ?, ?> definition) {
            Preconditions.checkNotNull(version);
            Preconditions.checkNotNull(definition);
            Preconditions.checkArgument(supportedVersions.contains(version));

            final QName identifier = definition.getStatementName();
            Preconditions.checkState(!commonStatements.containsKey(identifier),
                    "Statement %s already defined in common statement bundle.", identifier);
            Preconditions.checkState(!versionSpecificStatements.contains(version, identifier),
                    "Statement %s already defined for version %s.", identifier, version);
            Preconditions.checkState(parent.getCommonStatementDefinition(identifier) == null,
                    "Statement %s already defined in parent's common statement bundle.", identifier);
            Preconditions.checkState(parent.getVersionSpecificStatementDefinition(version, identifier) == null,
                    "Statement %s already defined for version %s in parent's statement bundle.", identifier, version);
            versionSpecificStatements.put(version, identifier, definition);
            return this;
        }

        public <K, V, N extends IdentifierNamespace<K, V>> Builder addSupport(
                final NamespaceBehaviour<K, V, N> namespaceSupport) {
            final Class<N> identifier = namespaceSupport.getIdentifier();
            Preconditions.checkState(!namespaces.containsKey(identifier));
            Preconditions.checkState(!parent.hasNamespaceBehaviour(identifier));
            namespaces.put(identifier, namespaceSupport);
            return this;
        }

        public Set<YangVersion> getSupportedVersions() {
            return supportedVersions;
        }

        public Builder setParent(final StatementSupportBundle parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public StatementSupportBundle build() {
            Preconditions.checkState(parent != null, "Parent must not be null");
            return new StatementSupportBundle(parent, supportedVersions, ImmutableMap.copyOf(commonStatements),
                    ImmutableMap.copyOf(namespaces), ImmutableTable.copyOf(versionSpecificStatements));
        }
    }
}

/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public final class StatementSupportBundle implements Immutable, NamespaceBehaviour.Registry {

    private static final StatementSupportBundle EMPTY = new StatementSupportBundle(null, null, ImmutableMap.of(),
            ImmutableMap.of(), ImmutableMap.of(), ImmutableMap.of());

    private final StatementSupportBundle parent;
    private final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions;
    private final ImmutableMap<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> versionSpecificDefinitions;
    private final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> commonNamespaceDefinitions;
    private final ImmutableMap<SemVer, ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>>> versionSpecificNamespaceDefinitions;
    private final SupportedVersionsBundle supportedVersionBundle;

    private StatementSupportBundle(final StatementSupportBundle parent,
            final SupportedVersionsBundle supportedVersionBundle, final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonStatements,
            final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> commonNamespaces,
            final ImmutableMap<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> versionSpecificStatements,
            final ImmutableMap<SemVer, ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>>> versionSpecificNamespaces) {
        this.parent = parent;
        this.supportedVersionBundle = supportedVersionBundle;
        this.commonDefinitions = commonStatements;
        this.commonNamespaceDefinitions = commonNamespaces;
        this.versionSpecificDefinitions = versionSpecificStatements;
        this.versionSpecificNamespaceDefinitions = versionSpecificNamespaces;
    }

    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getCommonDefinitions() {
        return commonDefinitions;
    }

    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getDefinitionsForVersion(final SemVer version) {
        return versionSpecificDefinitions.get(version);
    }

    public ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> getCommonNamespaceDefinitions() {
        return commonNamespaceDefinitions;
    }

    public ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> getNamespaceDefinitionsForVersion(final SemVer version) {
        return versionSpecificNamespaceDefinitions.get(version);
    }

    public static Builder builder(final SupportedVersionsBundle supportedVersionBundle) {
        return new Builder(supportedVersionBundle, EMPTY);
    }

    public static Builder derivedFrom(final StatementSupportBundle parent) {
        Preconditions.checkNotNull(parent);
        return new Builder(parent.getSupportedVersionBundle(), parent);
    }

    public SupportedVersionsBundle getSupportedVersionBundle() {
        return supportedVersionBundle;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(
            final SemVer version, final Class<N> namespace) throws NamespaceNotAvailableException {
        NamespaceBehaviour<K, V, N> result = getNamespaceBehaviourForVersion(version, namespace);
        if (result == null) {
            result = getCommonNamespaceBehaviour(namespace);
        }

        return result;
    }

    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getCommonNamespaceBehaviour(
            final Class<N> namespace) throws NamespaceNotAvailableException {
        final NamespaceBehaviour<?, ?, ?> potential = commonNamespaceDefinitions.get(namespace);
        if (potential != null) {
            Preconditions.checkState(namespace.equals(potential.getIdentifier()));

            /*
             * Safe cast, previous checkState checks equivalence of key from
             * which type argument are derived
             */
            return (NamespaceBehaviour<K, V, N>) potential;
        }
        if (parent != null) {
            return parent.getCommonNamespaceBehaviour(namespace);
        }
        return null;
    }

    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviourForVersion(
            final SemVer version, final Class<N> namespace) throws NamespaceNotAvailableException {
        final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaceDefs = versionSpecificNamespaceDefinitions
                .get(version);
        if (namespaceDefs != null) {
            final NamespaceBehaviour<?, ?, ?> potential = namespaceDefs.get(namespace);
            if (potential != null) {
                Preconditions.checkState(namespace.equals(potential.getIdentifier()));

                /*
                 * Safe cast, previous checkState checks equivalence of key from
                 * which type argument are derived
                 */
                return (NamespaceBehaviour<K, V, N>) potential;
            }
        }
        if (parent != null) {
            return parent.getCommonNamespaceBehaviour(namespace);
        }
        return null;
    }

    public <K, V, N extends IdentifierNamespace<K, V>> boolean hasCommonNamespaceBehaviour(final Class<N> namespace) {
        if (commonNamespaceDefinitions.containsKey(namespace)) {
            return true;
        }
        if (parent != null) {
            return parent.hasCommonNamespaceBehaviour(namespace);
        }
        return false;
    }

    public <K, V, N extends IdentifierNamespace<K, V>> boolean hasNamespaceBehaviourForVersion(final SemVer version,
            final Class<N> namespace) {
        final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaceDefs = versionSpecificNamespaceDefinitions
                .get(version);
        if (namespaceDefs != null) {
            if (namespaceDefs.containsKey(namespace)) {
                return true;
            }
        }

        if (parent != null) {
            return parent.hasNamespaceBehaviourForVersion(version, namespace);
        }
        return false;
    }

    public StatementSupport<?, ?, ?> getStatementDefinition(final SemVer version, final QName stmtName) {
        StatementSupport<?, ?, ?> result = getStatementDefinitionForVersion(version, stmtName);
        if (result == null) {
            result = getCommonStatementDefinition(stmtName);
        }

        return result;
    }

    public StatementSupport<?, ?, ?> getCommonStatementDefinition(final QName stmtName) {
        final StatementSupport<?, ?, ?> potential = commonDefinitions.get(stmtName);
        if (potential != null) {
            return potential;
        }
        if (parent != null) {
            return parent.getCommonStatementDefinition(stmtName);
        }
        return null;
    }

    public StatementSupport<?, ?, ?> getStatementDefinitionForVersion(final SemVer version, final QName stmtName) {
        final ImmutableMap<QName, StatementSupport<?, ?, ?>> stmtDefsForVersion = versionSpecificDefinitions
                .get(version);
        if (stmtDefsForVersion != null) {
            final StatementSupport<?, ?, ?> potential = stmtDefsForVersion.get(stmtName);
            if (potential != null) {
                return potential;
            }
        }

        if (parent != null) {
            return parent.getStatementDefinitionForVersion(version, stmtName);
        }
        return null;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<StatementSupportBundle> {
        private final Map<QName, StatementSupport<?, ?, ?>> commonStatements = new HashMap<>();
        private final Map<SemVer, Map<QName, StatementSupport<?, ?, ?>>> versionSpecificStatements = new HashMap<>();
        private final Map<Class<?>, NamespaceBehaviour<?, ?, ?>> commonNamespaces = new HashMap<>();
        private final Map<SemVer, Map<Class<?>, NamespaceBehaviour<?, ?, ?>>> versionSpecificNamespaces = new HashMap<>();

        private StatementSupportBundle parent;
        private final SupportedVersionsBundle supportedVersionBundle;

        Builder(final SupportedVersionsBundle supportedVersionBundle, final StatementSupportBundle parent) {
            this.parent = Preconditions.checkNotNull(parent);
            this.supportedVersionBundle = Preconditions.checkNotNull(supportedVersionBundle);
            for (final SemVer version : supportedVersionBundle.getAll()) {
                versionSpecificStatements.put(version, new HashMap<>());
                versionSpecificNamespaces.put(version, new HashMap<>());
            }
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

        public Builder addVersionSpecificSupport(final SemVer version, final StatementSupport<?, ?, ?> definition) {
            Preconditions.checkNotNull(version);
            Preconditions.checkNotNull(definition);
            Preconditions.checkArgument(supportedVersionBundle.contains(version));

            final QName identifier = definition.getStatementName();
            Preconditions.checkState(!commonStatements.containsKey(identifier),
                    "Statement %s already defined in common statement bundle.", identifier);
            Preconditions.checkState(!versionSpecificStatements.get(version).containsKey(identifier),
                    "Statement %s already defined for version %s.", identifier, version);
            Preconditions.checkState(parent.getCommonStatementDefinition(identifier) == null,
                    "Statement %s already defined in parent's common statement bundle.", identifier);
            Preconditions.checkState(parent.getStatementDefinitionForVersion(version, identifier) == null,
                    "Statement %s already defined for version %s in parent's statement bundle.", identifier, version);
            versionSpecificStatements.get(version).put(identifier, definition);
            return this;
        }

        public <K, V, N extends IdentifierNamespace<K, V>> Builder addSupport(
                final NamespaceBehaviour<K, V, N> namespaceSupport) {
            final Class<N> identifier = namespaceSupport.getIdentifier();
            Preconditions.checkState(!commonNamespaces.containsKey(identifier));
            Preconditions.checkState(!parent.hasCommonNamespaceBehaviour(identifier));
            commonNamespaces.put(identifier, namespaceSupport);
            return this;
        }

        public <K, V, N extends IdentifierNamespace<K, V>> Builder addVersionSpecificSupport(final SemVer version,
                final NamespaceBehaviour<K, V, N> namespaceSupport) {
            Preconditions.checkNotNull(version);
            Preconditions.checkNotNull(namespaceSupport);
            Preconditions.checkArgument(supportedVersionBundle.contains(version));

            final Class<N> identifier = namespaceSupport.getIdentifier();
            Preconditions.checkState(!commonNamespaces.containsKey(identifier),
                    "Namespace support %s already defined in common namespace bundle.", identifier);
            Preconditions.checkState(!versionSpecificNamespaces.get(version).containsKey(identifier),
                    "Namespace support %s already defined for version %s.", identifier, version);
            Preconditions.checkState(!parent.hasCommonNamespaceBehaviour(identifier),
                    "Namespace support %s already defined in parent's common namespace bundle.", identifier);
            Preconditions.checkState(!parent.hasNamespaceBehaviourForVersion(version, identifier),
                    "Namespace support %s already defined for version %s in parent's namespace bundle.", identifier,
                    version);
            versionSpecificNamespaces.get(version).put(identifier, namespaceSupport);
            return this;
        }

        public Builder setParent(final StatementSupportBundle parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public StatementSupportBundle build() {
            final Map<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> immutableVersionSpecificStmts = new HashMap<>();
            final Map<SemVer, ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>>> immutableVersionSpecificNs = new HashMap<>();
            for (final SemVer version : supportedVersionBundle.getAll()) {
                immutableVersionSpecificStmts.put(version, ImmutableMap.copyOf(versionSpecificStatements.get(version)));
                immutableVersionSpecificNs.put(version, ImmutableMap.copyOf(versionSpecificNamespaces.get(version)));
            }

            return new StatementSupportBundle(parent, supportedVersionBundle, ImmutableMap.copyOf(commonStatements),
                    ImmutableMap.copyOf(commonNamespaces), ImmutableMap.copyOf(immutableVersionSpecificStmts),
                    ImmutableMap.copyOf(immutableVersionSpecificNs));
        }
    }
}

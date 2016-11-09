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
            ImmutableMap.of(), ImmutableMap.of());

    private final StatementSupportBundle parent;
    private final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonDefinitions;
    private final ImmutableMap<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> versionSpecificDefinitions;
    private final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaceDefinitions;
    private final SupportedVersionsBundle supportedVersionBundle;

    private StatementSupportBundle(final StatementSupportBundle parent,
            final SupportedVersionsBundle supportedVersionBundle,
            final ImmutableMap<QName, StatementSupport<?, ?, ?>> commonStatements,
            final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces,
            final ImmutableMap<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> versionSpecificStatements) {
        this.parent = parent;
        this.supportedVersionBundle = supportedVersionBundle;
        this.commonDefinitions = commonStatements;
        this.namespaceDefinitions = namespaces;
        this.versionSpecificDefinitions = versionSpecificStatements;
    }

    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getCommonDefinitions() {
        return commonDefinitions;
    }

    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getDefinitionsSpecificForVersion(final SemVer version) {
        return versionSpecificDefinitions.get(version);
    }

    public ImmutableMap<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> getAllVersionSpecificDefinitions() {
        return versionSpecificDefinitions;
    }

    public ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> getNamespaceDefinitions() {
        return namespaceDefinitions;
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
            final Class<N> namespace) throws NamespaceNotAvailableException {
        final NamespaceBehaviour<?, ?, ?> potential = namespaceDefinitions.get(namespace);
        if (potential != null) {
            Preconditions.checkState(namespace.equals(potential.getIdentifier()));

            /*
             * Safe cast, previous checkState checks equivalence of key from
             * which type argument are derived
             */
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

    public StatementSupport<?, ?, ?> getStatementDefinition(final SemVer version, final QName stmtName) {
        StatementSupport<?, ?, ?> result = getVersionSpecificStatementDefinition(version, stmtName);
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

    public StatementSupport<?, ?, ?> getVersionSpecificStatementDefinition(final SemVer version, final QName stmtName) {
        final ImmutableMap<QName, StatementSupport<?, ?, ?>> stmtDefsForVersion = versionSpecificDefinitions
                .get(version);
        if (stmtDefsForVersion != null) {
            final StatementSupport<?, ?, ?> potential = stmtDefsForVersion.get(stmtName);
            if (potential != null) {
                return potential;
            }
        }

        if (parent != null) {
            return parent.getVersionSpecificStatementDefinition(version, stmtName);
        }
        return null;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<StatementSupportBundle> {
        private final Map<QName, StatementSupport<?, ?, ?>> commonStatements = new HashMap<>();
        private final Map<SemVer, Map<QName, StatementSupport<?, ?, ?>>> versionSpecificStatements = new HashMap<>();
        private final Map<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces = new HashMap<>();

        private StatementSupportBundle parent;
        private final SupportedVersionsBundle supportedVersionBundle;

        Builder(final SupportedVersionsBundle supportedVersionBundle, final StatementSupportBundle parent) {
            this.parent = Preconditions.checkNotNull(parent);
            this.supportedVersionBundle = Preconditions.checkNotNull(supportedVersionBundle);
            for (final SemVer version : supportedVersionBundle.getAll()) {
                versionSpecificStatements.put(version, new HashMap<>());
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
            Preconditions.checkState(parent.getVersionSpecificStatementDefinition(version, identifier) == null,
                    "Statement %s already defined for version %s in parent's statement bundle.", identifier, version);
            versionSpecificStatements.get(version).put(identifier, definition);
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

        public SupportedVersionsBundle getSupportedVersionBundle() {
            return supportedVersionBundle;
        }

        public Builder setParent(final StatementSupportBundle parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public StatementSupportBundle build() {
            Preconditions.checkState(parent != null, "Parent must not be null");
            final Map<SemVer, ImmutableMap<QName, StatementSupport<?, ?, ?>>> immutableVersionSpecificStmts = new HashMap<>();
            for (final SemVer version : supportedVersionBundle.getAll()) {
                immutableVersionSpecificStmts.put(version, ImmutableMap.copyOf(versionSpecificStatements.get(version)));
            }

            return new StatementSupportBundle(parent, supportedVersionBundle, ImmutableMap.copyOf(commonStatements),
                    ImmutableMap.copyOf(namespaces), ImmutableMap.copyOf(immutableVersionSpecificStmts));
        }
    }
}

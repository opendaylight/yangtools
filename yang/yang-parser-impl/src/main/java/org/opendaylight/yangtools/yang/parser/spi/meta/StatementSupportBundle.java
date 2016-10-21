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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public final class StatementSupportBundle implements Immutable,NamespaceBehaviour.Registry {

    private static final StatementSupportBundle EMPTY = new StatementSupportBundle(null, ImmutableMap.of(), ImmutableMap.of());

    private final StatementSupportBundle parent;
    private final ImmutableMap<QName, StatementSupport<?,?,?>> definitions;
    private final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaceDefinitions;

    private StatementSupportBundle(final StatementSupportBundle parent,
                                   final ImmutableMap<QName, StatementSupport<?, ?, ?>> statements,
                                   final ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces) {
        this.parent = parent;
        this.definitions = statements;
        this.namespaceDefinitions = namespaces;
    }

    public ImmutableMap<QName, StatementSupport<?, ?, ?>> getDefinitions() {
        return definitions;
    }

    public ImmutableMap<Class<?>, NamespaceBehaviour<?, ?, ?>> getNamespaceDefinitions() {
        return namespaceDefinitions;
    }

    public static Builder builder() {
        return new Builder(EMPTY);
    }

    public static Builder derivedFrom(final StatementSupportBundle parent) {
        return new Builder(parent);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> NamespaceBehaviour<K, V, N> getNamespaceBehaviour(final Class<N> namespace)
            throws NamespaceNotAvailableException {
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

    public StatementSupport<?, ?,?> getStatementDefinition(final QName stmtName) {
        final StatementSupport<?,?, ?> potential = definitions.get(stmtName);
        if (potential != null) {
            return potential;
        }
        if (parent != null) {
            return parent.getStatementDefinition(stmtName);
        }
        return null;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<StatementSupportBundle> {
        private final Map<QName, StatementSupport<?, ?, ?>> statements = new HashMap<>();
        private final Map<Class<?>, NamespaceBehaviour<?, ?, ?>> namespaces = new HashMap<>();

        private StatementSupportBundle parent;

        Builder(final StatementSupportBundle parent) {
            this.parent = parent;
        }

        public Builder addSupport(final StatementSupport<?, ?, ?> definition) {
            final QName identifier = definition.getStatementName();
            Preconditions.checkState(!statements.containsKey(identifier), "Statement %s already defined.", identifier);
            Preconditions.checkState(parent.getStatementDefinition(identifier) == null,
                    "Statement %s already defined.", identifier);
            statements.put(identifier, definition);
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

        public Builder setParent(final StatementSupportBundle parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public StatementSupportBundle build() {
            return new StatementSupportBundle(parent, ImmutableMap.copyOf(statements), ImmutableMap.copyOf(namespaces));
        }
    }

}

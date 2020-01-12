/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

@Beta
public abstract class AbstractDeclaredEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> {
    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }

    @Override
    public abstract @NonNull D getDeclared();

    public abstract static class WithSchemaTree<A, D extends DeclaredStatement<A>,
            E extends SchemaTreeAwareEffectiveStatement<A, D>> extends AbstractDeclaredEffectiveStatement<A, D> {
        @Override
        @SuppressWarnings("unchecked")
        protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
                final Class<N> namespace) {
            if (SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
                return Optional.of((Map<K, V>) schemaTreeNamespace());
            }
            return super.getNamespaceContents(namespace);
        }

        protected abstract ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace();
    }

    public abstract static class WithDataTree<A, D extends DeclaredStatement<A>,
            E extends DataTreeAwareEffectiveStatement<A, D>> extends WithSchemaTree<A, D, E> {
        @Override
        @SuppressWarnings("unchecked")
        protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
                final Class<N> namespace) {
            if (DataTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
                return Optional.of((Map<K, V>) dataTreeNamespace());
            }
            return super.getNamespaceContents(namespace);
        }

        protected abstract ImmutableMap<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace();
    }

    public abstract static class Default<A, D extends DeclaredStatement<A>>
            extends AbstractDeclaredEffectiveStatement<A, D> {
        private final @NonNull D declared;

        protected Default(final D declared) {
            this.declared = requireNonNull(declared);
        }

        @Override
        public final D getDeclared() {
            return declared;
        }
    }

    public abstract static class DefaultArgument<A, D extends DeclaredStatement<A>> extends Default<A, D> {
        protected DefaultArgument(final D declared) {
            super(declared);
        }

        @Override
        public final @Nullable A argument() {
            return getDeclared().argument();
        }
    }

    public abstract static class DefaultWithSchemaTree<A, D extends DeclaredStatement<A>,
            E extends SchemaTreeAwareEffectiveStatement<A, D>> extends WithSchemaTree<A, D, E> {
        private final @NonNull ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull D declared;

        protected DefaultWithSchemaTree(final D declared, final StatementSourceReference ref,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            this.declared = requireNonNull(declared);
            this.schemaTree = AbstractSchemaEffectiveDocumentedNode.createSchemaTreeNamespace(ref, substatements);
        }

        @Override
        public final D getDeclared() {
            return declared;
        }

        @Override
        protected final ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace() {
            return schemaTree;
        }
    }

    public abstract static class DefaultWithDataTree<A, D extends DeclaredStatement<A>,
            E extends DataTreeAwareEffectiveStatement<A, D>> extends WithDataTree<A, D, E> {
        private final @NonNull ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull ImmutableMap<QName, DataTreeEffectiveStatement<?>> dataTree;
        private final @NonNull D declared;

        protected DefaultWithDataTree(final D declared, final StatementSourceReference ref,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            this.declared = requireNonNull(declared);
            this.schemaTree = AbstractSchemaEffectiveDocumentedNode.createSchemaTreeNamespace(ref, substatements);
            this.dataTree = AbstractSchemaEffectiveDocumentedNode.createDataTreeNamespace(ref, schemaTree);
        }

        @Override
        public final D getDeclared() {
            return declared;
        }

        @Override
        protected final ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace() {
            return schemaTree;
        }

        @Override
        protected final ImmutableMap<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace() {
            return dataTree;
        }
    }
}

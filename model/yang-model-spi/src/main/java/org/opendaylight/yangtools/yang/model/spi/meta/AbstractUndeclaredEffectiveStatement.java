/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

@Beta
public abstract class AbstractUndeclaredEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D>  {
    @Override
    public final D getDeclared() {
        return null;
    }

    /**
     * Base stateless superclass form {@link SchemaTreeAwareEffectiveStatement}s. It maintains the contents of schema
     * tree namespace based of effective substatements.
     *
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
    public abstract static class WithSchemaTree<A, D extends DeclaredStatement<A>,
            E extends SchemaTreeAwareEffectiveStatement<A, D>> extends AbstractUndeclaredEffectiveStatement<A, D> {
        @Override
        @SuppressWarnings("unchecked")
        protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
                final Class<N> namespace) {
            if (SchemaTreeAwareEffectiveStatement.Namespace.class.equals(namespace)) {
                return Optional.of((Map<K, V>) schemaTreeNamespace());
            }
            return super.getNamespaceContents(namespace);
        }

        /**
         * Indexing support for {@link DataNodeContainer#findDataChildByName(QName)}.
         */
        protected final @Nullable DataSchemaNode dataSchemaNode(final QName name) {
            // Only DataNodeContainer subclasses should be calling this method
            verify(this instanceof DataNodeContainer);
            final SchemaTreeEffectiveStatement<?> child = schemaTreeNamespace().get(requireNonNull(name));
            return child instanceof DataSchemaNode ? (DataSchemaNode) child : null;
        }

        protected abstract Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace();
    }

    /**
     * Base stateless superclass form {@link DataTreeAwareEffectiveStatement}s. It maintains the contents of data tree
     * namespace based of effective substatements.
     *
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
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

        protected abstract Map<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace();
    }

    /**
     * Stateful version of {@link WithSchemaTree}. Schema tree namespace is eagerly instantiated (and checked).
     *
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
    public abstract static class DefaultWithSchemaTree<A, D extends DeclaredStatement<A>,
            E extends SchemaTreeAwareEffectiveStatement<A, D>> extends WithSchemaTree<A, D, E> {
        private final @NonNull Map<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull Object substatements;

        protected DefaultWithSchemaTree(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            this.substatements = maskList(substatements);
            this.schemaTree = immutableNamespaceOf(createSchemaTreeNamespace(substatements));
        }

        protected DefaultWithSchemaTree(final DefaultWithSchemaTree<A, D, E> original) {
            this.schemaTree = original.schemaTree;
            this.substatements = original.substatements;
        }

        @Override
        public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
            return unmaskList(substatements);
        }

        @Override
        protected final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace() {
            return schemaTree;
        }
    }

    /**
     * Stateful version of {@link WithDataTree}. Schema tree and data tree namespaces are eagerly instantiated
     * (and checked).
     *
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
    public abstract static class DefaultWithDataTree<A, D extends DeclaredStatement<A>,
            E extends DataTreeAwareEffectiveStatement<A, D>> extends WithDataTree<A, D, E> {
        private final @NonNull Map<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull Map<QName, DataTreeEffectiveStatement<?>> dataTree;
        private final @NonNull Object substatements;

        protected DefaultWithDataTree(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            final Map<QName, SchemaTreeEffectiveStatement<?>> schema = createSchemaTreeNamespace(substatements);
            this.schemaTree = immutableNamespaceOf(schema);
            this.dataTree = createDataTreeNamespace(schema.values(), schemaTree);
            this.substatements = maskList(substatements);
        }

        protected DefaultWithDataTree(final DefaultWithDataTree<A, D, E> original) {
            this.schemaTree = original.schemaTree;
            this.dataTree = original.dataTree;
            this.substatements = original.substatements;
        }

        @Override
        public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
            return unmaskList(substatements);
        }

        @Override
        protected final Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace() {
            return schemaTree;
        }

        @Override
        protected final Map<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace() {
            return dataTree;
        }
    }
}

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
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Base stateless superclass for statements which (logically) always have an associated {@link DeclaredStatement}. This
 * is notably not true for all {@code case} statements, some of which may actually be implied.
 *
 * <p>
 * Note implementations are not strictly required to make the declared statement available, they are free to throw
 * {@link UnsupportedOperationException} from {@link #getDeclared()}, rendering any services relying on declared
 * statement to be not available.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
@Beta
public abstract class AbstractDeclaredEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> {
    @Override
    public final StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public abstract @NonNull D getDeclared();

    /**
     * Base stateless superclass form {@link SchemaTreeAwareEffectiveStatement}s. It maintains the contents of schema
     * tree namespace based of effective substatements.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
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

        /**
         * Indexing support for {@link DataNodeContainer#dataChildByName(QName)}.
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
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
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
     * A stateful version of {@link AbstractDeclaredEffectiveStatement}, which holds (and requires) a declared
     * statement.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public abstract static class Default<A, D extends DeclaredStatement<A>>
            extends AbstractDeclaredEffectiveStatement<A, D> {
        private final @NonNull D declared;

        protected Default(final D declared) {
            this.declared = requireNonNull(declared);
        }

        protected Default(final Default<A, D> original) {
            this.declared = original.declared;
        }

        @Override
        public final D getDeclared() {
            return declared;
        }
    }

    /**
     * An extra building block on top of {@link Default}, which is wiring {@link #argument()} to the declared statement.
     * This is mostly useful for arguments that are not subject to inference transformation -- for example Strings in
     * {@code description}, etc. This explicitly is not true of statements which underwent namespace binding via
     * {@code uses} or similar.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public abstract static class DefaultArgument<A, D extends DeclaredStatement<A>> extends Default<A, D> {
        public abstract static class WithSubstatements<A, D extends DeclaredStatement<A>>
                extends DefaultArgument<A, D> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final D declared,
                    final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
                super(declared);
                this.substatements = maskList(substatements);
            }

            protected WithSubstatements(final WithSubstatements<A, D> original) {
                super(original);
                this.substatements = original.substatements;
            }

            @Override
            public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
                return unmaskList(substatements);
            }
        }

        protected DefaultArgument(final D declared) {
            super(declared);
        }

        protected DefaultArgument(final DefaultArgument<A, D> original) {
            super(original);
        }

        @Override
        public final A argument() {
            return getDeclared().argument();
        }
    }

    /**
     * A building block on top of {@link Default}, which adds an explicit argument value, which is not related to the
     * context. This is mostly useful when the effective argument value reflects additional statements and similar.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public abstract static class DefaultWithArgument<A, D extends DeclaredStatement<A>> extends Default<A, D> {
        public abstract static class WithSubstatements<A, D extends DeclaredStatement<A>>
                extends DefaultWithArgument<A, D> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final D declared, final A argument,
                    final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
                super(declared, argument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final A argument;

        protected DefaultWithArgument(final D declared, final A argument) {
            super(declared);
            this.argument = argument;
        }

        @Override
        public final A argument() {
            return argument;
        }
    }

    /**
     * Stateful version of {@link WithSchemaTree}. Schema tree namespace is eagerly instantiated (and checked).
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
    public abstract static class DefaultWithSchemaTree<A, D extends DeclaredStatement<A>,
            E extends SchemaTreeAwareEffectiveStatement<A, D>> extends WithSchemaTree<A, D, E> {
        public abstract static class WithSubstatements<A, D extends DeclaredStatement<A>,
                E extends SchemaTreeAwareEffectiveStatement<A, D>> extends DefaultWithSchemaTree<A, D, E> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final D declared,
                    final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
                super(declared, substatements);
                this.substatements = maskList(substatements);
            }

            protected WithSubstatements(final WithSubstatements<A, D, E> original) {
                super(original);
                this.substatements = original.substatements;
            }

            @Override
            public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final @NonNull ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull D declared;

        protected DefaultWithSchemaTree(final D declared,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            this.declared = requireNonNull(declared);
            this.schemaTree = ImmutableMap.copyOf(createSchemaTreeNamespace(substatements));
        }

        protected DefaultWithSchemaTree(final DefaultWithSchemaTree<A, D, E> original) {
            this.declared = original.declared;
            this.schemaTree = original.schemaTree;
        }

        @Override
        public final D getDeclared() {
            return declared;
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
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     * @param <E> Class representing effective version of this statement.
     */
    public abstract static class DefaultWithDataTree<A, D extends DeclaredStatement<A>,
            E extends DataTreeAwareEffectiveStatement<A, D>> extends WithDataTree<A, D, E> {
        public abstract static class WithSubstatements<A, D extends DeclaredStatement<A>,
                E extends DataTreeAwareEffectiveStatement<A, D>> extends DefaultWithDataTree<A, D, E> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final D declared,
                    final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
                super(declared, substatements);
                this.substatements = maskList(substatements);
            }

            protected WithSubstatements(final WithSubstatements<A, D, E> original) {
                super(original);
                this.substatements = original.substatements;
            }

            @Override
            public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final @NonNull ImmutableMap<QName, SchemaTreeEffectiveStatement<?>> schemaTree;
        private final @NonNull ImmutableMap<QName, DataTreeEffectiveStatement<?>> dataTree;
        private final @NonNull D declared;

        protected DefaultWithDataTree(final D declared,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            this.declared = requireNonNull(declared);
            final Map<QName, SchemaTreeEffectiveStatement<?>> schema = createSchemaTreeNamespace(substatements);
            this.schemaTree = ImmutableMap.copyOf(schema);
            this.dataTree = createDataTreeNamespace(schema.values(), schemaTree);
        }

        protected DefaultWithDataTree(final DefaultWithDataTree<A, D, E> original) {
            this.declared = original.declared;
            this.schemaTree = original.schemaTree;
            this.dataTree = original.dataTree;
        }

        @Override
        public final D getDeclared() {
            return declared;
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

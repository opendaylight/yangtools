/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Common interface grouping all {@link EffectiveStatement}s which are accessible via
 * {@link SchemaTreeEffectiveStatement.IndexedIn#schemaTreeNodes()}. This such statement corresponds to a
 * {@code schema node}.
 *
 * <p>This interface could be named {@code SchemaNodeEffectiveStatement}, but that could induce a notion that it has
 * something to do with {@link SchemaNode} -- which it has not. SchemaNode semantics are wrong in may aspects and while
 * implementations of this interface may also implement SchemaNode, the semantics of this interface should always be
 * preferred and SchemaNode is to be treated as deprecated whenever possible.
 *
 * @param <D> Declared statement type
 */
// FIXME: rename to EffectiveSchemaTreeStatement to prevent confusion with a hypothetical 'schema-tree' extension
public sealed interface SchemaTreeEffectiveStatement<D extends DeclaredStatement<QName>>
    extends EffectiveStatement<QName, D>
    permits EffectiveOperationStatement, CaseEffectiveStatement, ChoiceEffectiveStatement, DataTreeEffectiveStatement,
            NotificationEffectiveStatement {

    /**
     * Am {@link EffectiveStatement} that is a parent of some {@link SchemaTreeEffectiveStatement}s, forming a single
     * level in {@code schema tree}. This tree can be walked using {@link SchemaNodeIdentifier}, looking up each
     * component of {@link SchemaNodeIdentifier#getNodeIdentifiers()} using {@link #findSchemaTreeNode(QName)}.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     */
    interface IndexedIn<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        /**
         * Enumerate all {@code schema node}s defined within this node.
         *
         * @return All substatements participating on the {@code schema tree}
         */
        @NonNull Collection<SchemaTreeEffectiveStatement<?>> schemaTreeNodes();

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param qname Child identifier
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code qname} is null
         */
        @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(@NonNull QName qname);

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param <E> Effective substatement type
         * @param type Effective substatement class
         * @param qname Child identifier
         * @return Schema tree child, or empty
         * @throws NullPointerException if any argument is null
         */
        default <E> @NonNull Optional<E> findSchemaTreeNode(final @NonNull Class<E> type, final @NonNull QName qname) {
            return DefaultMethodHelpers.filterOptional(findSchemaTreeNode(qname), type);
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param qnames Child identifiers
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code qnames} is null or contains a null element
         * @throws NoSuchElementException if {@code qnames} is empty
         */
        default @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final @NonNull QName... qnames) {
            return findSchemaTreeNode(Arrays.asList(qnames));
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param <E> Effective substatement type
         * @param type Effective substatement class
         * @param qnames Child identifiers
         * @return Schema tree child, or empty
         * @throws NullPointerException if any argument is null or if {@code qnames} contains a null element
         * @throws NoSuchElementException if {@code qnames} is empty
         */
        default <E> @NonNull Optional<E> findSchemaTreeNode(final @NonNull Class<E> type,
                final @NonNull QName... qnames) {
            return DefaultMethodHelpers.filterOptional(findSchemaTreeNode(Arrays.asList(qnames)), type);
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param qnames Child identifiers
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code qnames} is null or contains a null element
         * @throws NoSuchElementException if {@code qnames} is empty
         */
        default @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(
                final @NonNull List<QName> qnames) {
            final var it = qnames.iterator();
            SchemaTreeEffectiveStatement.IndexedIn<?, ?> parent = this;
            while (true) {
                final var found = parent.findSchemaTreeNode(it.next());
                if (!it.hasNext() || found.isEmpty()) {
                    return found;
                }
                final var node = found.orElseThrow();
                if (!(node instanceof SchemaTreeEffectiveStatement.IndexedIn<?, ?> aware)) {
                    return Optional.empty();
                }
                parent = aware;
            }
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param <E> Effective substatement type
         * @param type Effective substatement class
         * @param qnames Child identifiers
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code qnames} is null or contains a null element
         * @throws NoSuchElementException if {@code qnames} is empty
         */
        default <E> @NonNull Optional<E> findSchemaTreeNode(final @NonNull Class<E> type,
                final @NonNull List<QName> qnames) {
            return DefaultMethodHelpers.filterOptional(findSchemaTreeNode(qnames), type);
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its
         * {@link Descendant descendant schema node identifier}.
         *
         * @implSpec
         *     Default implementation defers to {@link #findSchemaTreeNode(List)}.
         *
         * @param descendant Descendant schema node identifier
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code descendant} is null
         */
        default @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(
                final @NonNull Descendant descendant) {
            return findSchemaTreeNode(descendant.getNodeIdentifiers());
        }

        /**
         * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its
         * {@link Descendant descendant schema node identifier}.
         *
         * @implSpec
         *     Default implementation defers to {@link #findSchemaTreeNode(Class, List)}.
         *
         * @param <E> Effective substatement type
         * @param type Effective substatement class
         * @param descendant Descendant schema node identifier
         * @return Schema tree child, or empty
         * @throws NullPointerException if {@code descendant} is null
         */
        default <E> @NonNull Optional<E> findSchemaTreeNode(final @NonNull Class<E> type,
                final @NonNull Descendant descendant) {
            return findSchemaTreeNode(type, descendant.getNodeIdentifiers());
        }
    }
}

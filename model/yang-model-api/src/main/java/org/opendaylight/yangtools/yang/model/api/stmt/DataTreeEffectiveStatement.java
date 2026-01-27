/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common interface grouping all {@link EffectiveStatement}s which are accessible via
 * {@link DataTreeEffectiveStatement.IndexedIn#dataTreeNodes()}. This such statement corresponds to a {@code data node}.
 *
 * <p>This interface could be named {@code SchemaNodeEffectiveStatement}, but that could induce a notion that it has
 * something to do with {@link DataSchemaNode} -- which it has not. DataSchemaNode semantics are wrong in may aspects
 * and while implementations of this interface may also implement DataSchemaNode, the semantics of this interface should
 * always be preferred and DataSchemaNode is to be treated as deprecated whenever possible.
 *
 * @param <D> Declared statement type
 */
// FIXME: rename to EffectiveSchemaTreeStatement to prevent confusion with a hypothetical 'data-tree' extension
public sealed interface DataTreeEffectiveStatement<D extends DeclaredStatement<QName>>
    extends SchemaTreeEffectiveStatement<D>
    permits EffectiveOperationBodyStatement, AnydataEffectiveStatement, AnyxmlEffectiveStatement,
            ContainerEffectiveStatement, LeafEffectiveStatement, LeafListEffectiveStatement, ListEffectiveStatement {
    /**
     * Interface implemented by all {@link SchemaTreeEffectiveStatement.IndexedIn}s which can contain
     * a {@code data tree} child.
     *
     * @param <A> Argument type
     * @param <D> Class representing declared version of this statement.
     */
    interface IndexedIn<A, D extends DeclaredStatement<A>> extends SchemaTreeEffectiveStatement.IndexedIn<A, D> {
        /**
         * Return the mapping of {@code data tree} children of this statement. This is a subtree of
         * {@link SchemaTreeEffectiveStatement.IndexedIn#schemaTreeNodes()} in that all data nodes are also schema
         * nodes. The structure of the tree is different, though, as {@code choice} and {@code case} statements are
         * glossed over and they do not contribute to the tree hierarchy -- only their children do.
         *
         * <p>Note that returned statements are not necessarily direct substatements of this statement.
         *
         * @return All substatements participating on the {@code data tree}
         */
        @NonNull Collection<DataTreeEffectiveStatement<?>> dataTreeNodes();

        /**
         * Find a {@code data tree} child {@link DataTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param qname Child identifier
         * @return Data tree child, or empty
         * @throws NullPointerException if {@code qname} is {@code null}
         */
        @NonNull Optional<DataTreeEffectiveStatement<?>> findDataTreeNode(@NonNull QName qname);

        /**
         * Find a {@code data tree} child {@link DataTreeEffectiveStatement}, as identified by its QName argument.
         *
         * @param <E> Effective substatement type
         * @param type Effective substatement class
         * @param qname Child identifier
         * @return Data tree child, or empty
         * @throws NullPointerException if any argument is {@code null}
         */
        default <E> @NonNull Optional<E> findDataTreeNode(final Class<E> type, final @NonNull QName qname) {
            return DefaultMethodHelpers.filterOptional(findDataTreeNode(qname), type);
        }
    }
}

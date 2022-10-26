/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.model.api.stmt.DefaultMethodHelpers.filterOptional;

import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Interface implemented by all {@link SchemaTreeAwareEffectiveStatement}s which can contain a {@code data tree} child.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 */
public interface DataTreeAwareEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends SchemaTreeAwareEffectiveStatement<A, D> {
    /**
     * Return the mapping of {@code data tree} children of this statement. This is a subtree of
     * {@link SchemaTreeAwareEffectiveStatement#schemaTreeNamespace()} in that all data nodes are also schema nodes. The
     * structure of the tree is different, though, as {@code choice} and {@code case} statements are glossed over and
     * they do not contribute to the tree hierarchy -- only their children do.
     */
    @NonNull Map<QName, DataTreeEffectiveStatement<?>> dataTreeNamespace();

    /**
     * Find a {@code data tree} child {@link DataTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param qname Child identifier
     * @return Data tree child, or empty
     * @throws NullPointerException if {@code qname} is {@code null}
     */
    default @NonNull Optional<DataTreeEffectiveStatement<?>> findDataTreeNode(final @NonNull QName qname) {
        return Optional.ofNullable(dataTreeNamespace().get(requireNonNull(qname)));
    }

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
        return filterOptional(type, findDataTreeNode(qname));
    }
}

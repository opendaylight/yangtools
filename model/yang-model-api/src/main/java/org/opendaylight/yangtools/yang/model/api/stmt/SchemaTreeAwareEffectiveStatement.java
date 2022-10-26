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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Interface implemented by all {@link EffectiveStatement}s which can contain a {@code schema tree} child. This tree
 * can be walked using {@link SchemaNodeIdentifier}, looking up each component of
 * {@link SchemaNodeIdentifier#getNodeIdentifiers()} using {@link #findSchemaTreeNode(QName)}.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 */
public interface SchemaTreeAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Mapping of {@code schema node}s defined within this node.
     */
    @NonNull Map<QName, SchemaTreeEffectiveStatement<?>> schemaTreeNamespace();

    /**
     * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param qname Child identifier
     * @return Schema tree child, or empty
     * @throws NullPointerException if {@code qname} is null
     */
    default @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final @NonNull QName qname) {
        return Optional.ofNullable(schemaTreeNamespace().get(requireNonNull(qname)));
    }

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
        return filterOptional(type, findSchemaTreeNode(qname));
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
    default <E> @NonNull Optional<E> findSchemaTreeNode(final @NonNull Class<E> type, final @NonNull QName... qnames) {
        return filterOptional(type, findSchemaTreeNode(Arrays.asList(qnames)));
    }

    /**
     * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param qnames Child identifiers
     * @return Schema tree child, or empty
     * @throws NullPointerException if {@code qnames} is null or contains a null element
     * @throws NoSuchElementException if {@code qnames} is empty
     */
    default @NonNull Optional<SchemaTreeEffectiveStatement<?>> findSchemaTreeNode(final @NonNull List<QName> qnames) {
        final Iterator<QName> it = qnames.iterator();
        SchemaTreeAwareEffectiveStatement<?, ?> parent = this;
        while (true) {
            final var found = parent.findSchemaTreeNode(it.next());
            if (!it.hasNext() || found.isEmpty()) {
                return found;
            }
            final SchemaTreeEffectiveStatement<?> node = found.orElseThrow();
            if (node instanceof SchemaTreeAwareEffectiveStatement<?, ?> aware) {
                parent = aware;
            } else {
                return Optional.empty();
            }
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
        return filterOptional(type, findSchemaTreeNode(qnames));
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

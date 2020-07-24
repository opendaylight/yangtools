/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Interface implemented by all {@link EffectiveStatement}s which can contain a {@code schema tree} child. This tree
 * can be walked using {@link SchemaNodeIdentifier}, looking up each component of
 * {@link SchemaNodeIdentifier#getNodeIdentifiers()} using {@link #findSchemaTreeNode(QName)}.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 * @author Robert Varga
 */
@Beta
public interface SchemaTreeAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Namespace of {@code schema node}s defined within this node.
     *
     * @param <T> Child statement type
     * @author Robert Varga
     */
    @NonNullByDefault
    abstract class Namespace<T extends SchemaTreeEffectiveStatement<?>> extends EffectiveStatementNamespace<T> {
        private Namespace() {
            // Should never be instantiated
        }
    }

    /**
     * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param <E> Effective substatement type
     * @param qname Child identifier
     * @return Schema tree child, or empty
     * @throws NullPointerException if {@code qname} is null
     */
    default <E extends SchemaTreeEffectiveStatement<?>> @NonNull Optional<E> findSchemaTreeNode(
            final @NonNull QName qname) {
        return get(Namespace.class, requireNonNull(qname));
    }
}

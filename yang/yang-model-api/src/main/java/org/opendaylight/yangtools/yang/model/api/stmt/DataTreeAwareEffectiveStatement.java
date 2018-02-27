/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Interface implemented by all {@link EffectiveStatement}s which can contain a {@code data tree} child.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 * @author Robert Varga
 */
public interface DataTreeAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Find a {@code data tree} child {@link DataTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param qname Child identifier
     * @return Data tree child, or empty
     * @throws NullPointerException if {@code qname} is null
     */
    // FIXME: make sure this namespace is populated
    default <E extends DataTreeEffectiveStatement<?>> @NonNull Optional<E> findDataTreeNode(
            final @NonNull QName qname) {
        return Optional.ofNullable(get(DataTreeEffectiveStatementNamespace.class, requireNonNull(qname)));
    }
}

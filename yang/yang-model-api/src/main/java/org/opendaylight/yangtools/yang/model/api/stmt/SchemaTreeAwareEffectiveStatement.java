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
 * Interface implemented by all {@link EffectiveStatement}s which can contain a {@code schema tree} child.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement.
 * @author Robert Varga
 */
public interface SchemaTreeAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Find a {@code schema tree} child {@link SchemaTreeEffectiveStatement}, as identified by its QName argument.
     *
     * @param qname Child identifier
     * @return Schema tree child, or empty
     * @throws NullPointerException if {@code qname} is null
     */
    default <E extends SchemaTreeEffectiveStatement<?>> @NonNull Optional<E> findSchemaTreeChild(
            final @NonNull QName qname) {
        return Optional.ofNullable(get(SchemaTreeEffectiveStatementNamespace.class, requireNonNull(qname)));
    }
}

/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The run-time signature of a YANG statement.
 *
 * @param <A> argument representation
 * @param <D> declared statement representation
 * @param <E> effective statement representation
 * @since 15.0.0
 */
public sealed interface StatementMapping<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        permits DefaultStatementMapping {
    /**
     * {@return the argument representation class}
     */
    @NonNull Class<A> argumentRepresentation();

    /**
     * {@return the declared statement representation class}
     */
    @NonNull Class<D> declaredRepresentation();

    /**
     * {@return the effective statement representation class}
     */
    @NonNull Class<E> effectiveRepresentation();

    /**
     * {@return the equivalent of {@code argumentRepresentation().getName()}}
     */
    default @NonNull String argumentName() {
        return argumentRepresentation().getName();
    }

    /**
     * {@return the equivalent of {@code declaredRepresentation().getName()}}
     */
    default @NonNull String declaredName() {
        return declaredRepresentation().getName();
    }

    /**
     * {@return the equivalent of {@code effectiveRepresentation().getName()}}
     */
    default @NonNull String effectiveName() {
        return effectiveRepresentation().getName();
    }
}

/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Abstract base class for {@link DeclaredStatement} implementations.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 */
public abstract non-sealed class AbstractDeclaredStatement<A> extends AbstractModelStatement<A>
        implements DeclaredStatement<A> {
    /**
     * Utility method for recovering singleton lists squashed by
     * {@link AbstractDeclaredStatement#maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if masked is null
     * @throws ClassCastException if masked object does not match DeclaredStatement
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static final @NonNull ImmutableList<? extends DeclaredStatement<?>> unmaskList(
            final @NonNull Object masked) {
        return (ImmutableList) unmaskList(masked, DeclaredStatement.class);
    }
}

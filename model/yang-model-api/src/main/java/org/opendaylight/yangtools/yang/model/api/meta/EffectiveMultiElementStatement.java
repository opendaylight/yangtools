/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;

/**
 * Common interface for multi-element {@link EffectiveStatement}s, such as {@code list} and {@code leaf-list}. This
 * interface appropriate for when the equivalents of {@code min-elements}, {@code max-elements} and {@code ordered-by}
 * are defined for a particular statement.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public interface EffectiveMultiElementStatement<A, D extends DeclaredMultiElementStatement<A>>
        extends EffectiveStatement<A, D> {
    /**
     * {@return the element {@link Ordering}}
     */
    default Ordering elementOrdering() {
        return findFirstEffectiveSubstatementArgument(OrderedByEffectiveStatement.class).orElse(Ordering.SYSTEM);
    }

    /**
     * {@return the {@code ElementCountMatcher}, or {@code null} if the number of elements is not restricted}
     */
    @Nullable ElementCountMatcher elementCountMatcher();
}

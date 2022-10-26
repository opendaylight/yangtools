/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An {@link EffectiveStatement} which can contain an {@link OrderedByEffectiveStatement}, controlling ordering of
 * data elements. Absence of an {@code ordered-by} statement implies {@link Ordering#SYSTEM}. YANG statements using
 * this interface are {@link LeafListEffectiveStatement} and {@link ListEffectiveStatement}.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public interface OrderedByAwareEffectiveStatement<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
    /**
     * Return the effective {@link Ordering} of this statement.
     *
     * @return Effective ordering
     */
    default @NonNull Ordering ordering() {
        return findFirstEffectiveSubstatementArgument(OrderedByEffectiveStatement.class).orElse(Ordering.SYSTEM);
    }
}

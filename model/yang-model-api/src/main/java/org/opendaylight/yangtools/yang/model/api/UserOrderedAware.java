/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByAwareEffectiveStatement;

/**
 * Common interface for {@link DataSchemaNode}s which can have an {@code ordered-by} substatement.
 *
 * @param <E> Effective representation of the underlying YANG statement
 */
@Beta
public interface UserOrderedAware<E extends OrderedByAwareEffectiveStatement<?, ?>>
        extends EffectiveStatementEquivalent {
    @Override
    E asEffectiveStatement();

    /**
     * YANG {@code ordered-by} statement. It defines whether the order of entries within this node are determined by the
     * user or the system. If not present, default is false.
     *
     * @return true if ordered-by argument is {@code user}, false otherwise
     */
    default boolean isUserOrdered() {
        return asEffectiveStatement().ordering() == Ordering.USER;
    }
}

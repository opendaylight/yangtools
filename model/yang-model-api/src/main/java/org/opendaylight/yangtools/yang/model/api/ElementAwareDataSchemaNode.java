/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.ElementAwareEffectiveStatement;

/**
 * Common interface for list-like nodes, which can optionally have constraints on the number of direct children. This
 * is either a {@link LeafListSchemaNode} or a  {@link ListSchemaNode}
 */
public interface ElementAwareDataSchemaNode<E extends ElementAwareEffectiveStatement<?>>
        extends DataSchemaNode, EffectiveStatementEquivalent<E>, MustConstraintAware {
    /**
     * {@return the {@code ElementCountMatcher}, or {@code null}}
     */
    default @Nullable ElementCountMatcher elementCountMatcher() {
        return asEffectiveStatement().elementCountMatcher();
    }

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

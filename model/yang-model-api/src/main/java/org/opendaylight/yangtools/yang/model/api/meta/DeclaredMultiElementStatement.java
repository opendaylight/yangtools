/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;

/**
 * Common interface for multi-element {@link DeclaredStatement}s, such as {@code list} and {@code leaf-list}. This
 * interface appropriate for when the equivalents of {@code min-elements}, {@code max-elements} and {@code ordered-by}
 * are defined for a particular statement.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 */
public interface DeclaredMultiElementStatement<A> extends DeclaredStatement<A> {

    default @Nullable MinElementsStatement minElements() {
        return findFirstDeclaredSubstatement(MinElementsStatement.class).orElse(null);
    }

    default @Nullable MaxElementsStatement maxElements() {
        return findFirstDeclaredSubstatement(MaxElementsStatement.class).orElse(null);
    }

    default @Nullable OrderedByStatement orderedBy() {
        return findFirstDeclaredSubstatement(OrderedByStatement.class).orElse(null);
    }
}

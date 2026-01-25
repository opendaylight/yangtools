/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * A {@link DeclaredStatement} that is a parent of multiple {@link GroupingStatement}s.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 */
@Beta
public interface GroupingStatementMultipleIn<A> extends DeclaredStatement<A> {
    /**
     * {@return all {@code GroupingStatement} substatements}
     */
    default @NonNull Collection<? extends @NonNull GroupingStatement> groupingStatements() {
        return declaredSubstatements(GroupingStatement.class);
    }
}

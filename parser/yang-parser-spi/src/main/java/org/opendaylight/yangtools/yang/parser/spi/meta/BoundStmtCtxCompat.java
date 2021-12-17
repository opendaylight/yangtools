/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Intermediate compatibility interface between {@link StmtContext} and {@link EffectiveStmtCtx.Current}.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement
 */
@Beta
public interface BoundStmtCtxCompat<A, D extends DeclaredStatement> extends BoundStmtCtx<A>, StmtContextCompat {
    /**
     * Returns the {@link DeclaredStatement} view of this statement.
     */
    @NonNull D declared();
}

/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A marker interface for a {@link StmtContext}. Useful for operations which make assumption about the context's
 * hierarchy.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public interface RootStmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StmtContext<A, D, E> {

    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext.Mutable<A, D, E>, RootStmtContext<A, D, E> {

        @Override
        RootStmtContext.Mutable<?, ?, ?> getRoot();
    }
}

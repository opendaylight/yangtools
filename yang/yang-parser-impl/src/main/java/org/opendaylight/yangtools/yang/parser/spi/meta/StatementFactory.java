/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public interface StatementFactory<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {
    /**
     * Create a {@link DeclaredStatement} for specified context.
     *
     * @param ctx Statement context
     * @return A declared statement instance.
     */
    @Nonnull D createDeclared(@Nonnull StmtContext<A, D, ?> ctx);

    /**
     * Create a {@link EffectiveStatement} for specified context.
     *
     * @param ctx Statement context
     * @return An effective statement instance.
     */
    @Nonnull E createEffective(@Nonnull StmtContext<A, D, E> ctx);
}

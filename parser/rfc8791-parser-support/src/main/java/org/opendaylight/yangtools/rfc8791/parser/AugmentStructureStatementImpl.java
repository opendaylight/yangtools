/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureArgument;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;

final class AugmentStructureStatementImpl extends WithSubstatements<AugmentStructureArgument>
        implements AugmentStructureStatement {
    AugmentStructureStatementImpl(final BoundStmtCtx<AugmentStructureArgument> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }
}

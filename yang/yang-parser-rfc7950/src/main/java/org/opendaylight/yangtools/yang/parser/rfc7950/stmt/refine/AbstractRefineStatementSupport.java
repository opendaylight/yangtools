/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractRefineStatementSupport
        extends AbstractStatementSupport<Descendant, RefineStatement, RefineEffectiveStatement> {

    AbstractRefineStatementSupport() {
        super(YangStmtMapping.REFINE);
    }

    @Override
    public final Descendant parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ArgumentUtils.parseDescendantSchemaNodeIdentifier(ctx, value);
    }

    @Override
    public final RefineStatement createDeclared(final StmtContext<Descendant, RefineStatement, ?> ctx) {
        return new RefineStatementImpl(ctx);
    }

    @Override
    public final RefineEffectiveStatement createEffective(
            final StmtContext<Descendant, RefineStatement, RefineEffectiveStatement> ctx) {
        return new RefineEffectiveStatementImpl(ctx);
    }
}
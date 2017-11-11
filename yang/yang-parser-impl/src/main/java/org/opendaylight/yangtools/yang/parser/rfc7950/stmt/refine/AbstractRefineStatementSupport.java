/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.PathUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractRefineStatementSupport extends AbstractStatementSupport<SchemaNodeIdentifier, RefineStatement,
        EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> {

    AbstractRefineStatementSupport() {
        super(YangStmtMapping.REFINE);
    }

    @Override
    public final SchemaNodeIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return PathUtils.nodeIdentifierFromPath(ctx, value);
    }

    @Override
    public final RefineStatement createDeclared(final StmtContext<SchemaNodeIdentifier, RefineStatement, ?> ctx) {
        return new RefineStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<SchemaNodeIdentifier, RefineStatement> createEffective(
            final StmtContext<SchemaNodeIdentifier, RefineStatement,
            EffectiveStatement<SchemaNodeIdentifier, RefineStatement>> ctx) {
        return new RefineEffectiveStatementImpl(ctx);
    }
}
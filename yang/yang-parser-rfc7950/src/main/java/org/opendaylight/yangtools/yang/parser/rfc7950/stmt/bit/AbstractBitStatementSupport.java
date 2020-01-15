/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractBitStatementSupport
        extends AbstractStatementSupport<String, BitStatement, BitEffectiveStatement> {
    AbstractBitStatementSupport() {
        super(YangStmtMapping.BIT);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // Performs de-duplication and interning in one go
        return StmtContextUtils.parseIdentifier(ctx, value).getLocalName();
    }

    @Override
    public final BitStatement createDeclared(final StmtContext<String, BitStatement, ?> ctx) {
        return new BitStatementImpl(ctx);
    }

    @Override
    public final BitEffectiveStatement createEffective(
            final StmtContext<String, BitStatement, BitEffectiveStatement> ctx) {
        return new BitEffectiveStatementImpl(ctx);
    }
}
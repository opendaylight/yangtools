/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractBitStatementSupport extends
        AbstractQNameStatementSupport<BitStatement, EffectiveStatement<QName, BitStatement>> {
    AbstractBitStatementSupport() {
        super(YangStmtMapping.BIT);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public final BitStatement createDeclared(final StmtContext<QName, BitStatement, ?> ctx) {
        return new BitStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<QName, BitStatement> createEffective(
            final StmtContext<QName, BitStatement, EffectiveStatement<QName, BitStatement>> ctx) {
        return new BitEffectiveStatementImpl(ctx);
    }
}
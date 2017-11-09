/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractEnumStatementSupport extends
        AbstractStatementSupport<String, EnumStatement, EffectiveStatement<String, EnumStatement>> {

    AbstractEnumStatementSupport() {
        super(YangStmtMapping.ENUM);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // FIXME: Checks for real value
        return value;
    }

    @Override
    public final EnumStatement createDeclared(final StmtContext<String, EnumStatement, ?> ctx) {
        return new EnumStatementImpl(ctx);
    }

    @Override
    public final EffectiveStatement<String, EnumStatement> createEffective(
            final StmtContext<String, EnumStatement, EffectiveStatement<String, EnumStatement>> ctx) {
        return new EnumEffectiveStatementImpl(ctx);
    }
}
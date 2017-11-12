/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.argument;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ArgumentStatementSupport
        extends AbstractQNameStatementSupport<ArgumentStatement, EffectiveStatement<QName, ArgumentStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .ARGUMENT)
        .addOptional(YangStmtMapping.YIN_ELEMENT)
        .build();

    public ArgumentStatementSupport() {
        super(YangStmtMapping.ARGUMENT);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.qnameFromArgument(ctx, value);
    }

    @Override
    public ArgumentStatement createDeclared(
            final StmtContext<QName, ArgumentStatement, ?> ctx) {
        return new ArgumentStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, ArgumentStatement> createEffective(
            final StmtContext<QName, ArgumentStatement, EffectiveStatement<QName, ArgumentStatement>> ctx) {
        return new ArgumentEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
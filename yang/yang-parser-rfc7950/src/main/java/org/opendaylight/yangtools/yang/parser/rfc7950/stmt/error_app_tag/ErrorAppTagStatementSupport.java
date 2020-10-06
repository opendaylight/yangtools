/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ErrorAppTagStatementSupport
        extends BaseStringStatementSupport<ErrorAppTagStatement, ErrorAppTagEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ERROR_APP_TAG).build();
    private static final ErrorAppTagStatementSupport INSTANCE = new ErrorAppTagStatementSupport();

    private ErrorAppTagStatementSupport() {
        super(YangStmtMapping.ERROR_APP_TAG);
    }

    public static ErrorAppTagStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ErrorAppTagStatement createDeclared(final StmtContext<String, ErrorAppTagStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularErrorAppTagStatement(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected ErrorAppTagStatement createEmptyDeclared(final StmtContext<String, ErrorAppTagStatement, ?> ctx) {
        return new EmptyErrorAppTagStatement(ctx.coerceRawStatementArgument());
    }

    @Override
    protected ErrorAppTagEffectiveStatement createEffective(
            final StmtContext<String, ErrorAppTagStatement, ErrorAppTagEffectiveStatement> ctx,
            final ErrorAppTagStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularErrorAppTagEffectiveStatement(declared, substatements);
    }

    @Override
    protected ErrorAppTagEffectiveStatement createEmptyEffective(
            final StmtContext<String, ErrorAppTagStatement, ErrorAppTagEffectiveStatement> ctx,
            final ErrorAppTagStatement declared) {
        return new EmptyErrorAppTagEffectiveStatement(declared);
    }
}
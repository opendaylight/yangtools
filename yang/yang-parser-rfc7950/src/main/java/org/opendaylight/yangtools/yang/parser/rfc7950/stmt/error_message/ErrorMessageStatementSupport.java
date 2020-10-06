/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_message;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ErrorMessageStatementSupport
        extends BaseStringStatementSupport<ErrorMessageStatement, ErrorMessageEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.ERROR_MESSAGE).build();
    private static final ErrorMessageStatementSupport INSTANCE = new ErrorMessageStatementSupport();

    private ErrorMessageStatementSupport() {
        super(YangStmtMapping.ERROR_MESSAGE);
    }

    public static ErrorMessageStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ErrorMessageStatement createDeclared(final StmtContext<String, ErrorMessageStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularErrorMessageStatement(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected ErrorMessageStatement createEmptyDeclared(final StmtContext<String, ErrorMessageStatement, ?> ctx) {
        return new EmptyErrorMessageStatement(ctx.coerceRawStatementArgument());
    }

    @Override
    protected ErrorMessageEffectiveStatement createEffective(
            final StmtContext<String, ErrorMessageStatement, ErrorMessageEffectiveStatement> ctx,
            final ErrorMessageStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularErrorMessageEffectiveStatement(declared, substatements);
    }

    @Override
    protected ErrorMessageEffectiveStatement createEmptyEffective(
            final StmtContext<String, ErrorMessageStatement, ErrorMessageEffectiveStatement> ctx,
            final ErrorMessageStatement declared) {
        return new EmptyErrorMessageEffectiveStatement(declared);
    }
}

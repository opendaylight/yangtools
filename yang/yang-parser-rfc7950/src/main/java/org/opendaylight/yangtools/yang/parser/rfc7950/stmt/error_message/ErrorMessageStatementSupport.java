/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_message;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ErrorMessageStatementSupport extends
        AbstractStatementSupport<String, ErrorMessageStatement, EffectiveStatement<String, ErrorMessageStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .ERROR_MESSAGE)
        .build();
    private static final ErrorMessageStatementSupport INSTANCE = new ErrorMessageStatementSupport();

    private ErrorMessageStatementSupport() {
        super(YangStmtMapping.ERROR_MESSAGE);
    }

    public static ErrorMessageStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ErrorMessageStatement createDeclared(final StmtContext<String, ErrorMessageStatement, ?> ctx) {
        return new ErrorMessageStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ErrorMessageStatement> createEffective(
            final StmtContext<String, ErrorMessageStatement,
            EffectiveStatement<String, ErrorMessageStatement>> ctx) {
        return new ErrorMessageEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
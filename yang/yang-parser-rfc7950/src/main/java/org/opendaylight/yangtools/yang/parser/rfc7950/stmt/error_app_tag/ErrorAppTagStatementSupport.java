/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.error_app_tag;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public class ErrorAppTagStatementSupport extends
        AbstractStatementSupport<String, ErrorAppTagStatement, EffectiveStatement<String, ErrorAppTagStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ERROR_APP_TAG).build();

    public ErrorAppTagStatementSupport() {
        super(YangStmtMapping.ERROR_APP_TAG);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public ErrorAppTagStatement createDeclared(
            final StmtContext<String, ErrorAppTagStatement, ?> ctx) {
        return new ErrorAppTagStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, ErrorAppTagStatement> createEffective(
            final StmtContext<String, ErrorAppTagStatement, EffectiveStatement<String, ErrorAppTagStatement>> ctx) {
        return new ErrorAppTagEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
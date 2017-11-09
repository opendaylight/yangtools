/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.stmt.dflt;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultStatementSupport
        extends AbstractStatementSupport<String, DefaultStatement, EffectiveStatement<String, DefaultStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DEFAULT).build();

    public DefaultStatementSupport() {
        super(YangStmtMapping.DEFAULT);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public DefaultStatement createDeclared(final StmtContext<String, DefaultStatement, ?> ctx) {
        return new DefaultStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, DefaultStatement> createEffective(
            final StmtContext<String, DefaultStatement, EffectiveStatement<String, DefaultStatement>> ctx) {
        return new DefaultEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
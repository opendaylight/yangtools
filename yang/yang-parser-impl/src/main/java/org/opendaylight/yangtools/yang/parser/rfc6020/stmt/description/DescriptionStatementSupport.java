/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.stmt.description;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DescriptionStatementSupport extends AbstractStatementSupport<String, DescriptionStatement,
        EffectiveStatement<String, DescriptionStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.DESCRIPTION).build();

    public DescriptionStatementSupport() {
        super(YangStmtMapping.DESCRIPTION);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public DescriptionStatement createDeclared(final StmtContext<String, DescriptionStatement, ?> ctx) {
        return new DescriptionStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<String, DescriptionStatement> createEffective(
            final StmtContext<String, DescriptionStatement, EffectiveStatement<String, DescriptionStatement>> ctx) {
        return new DescriptionEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
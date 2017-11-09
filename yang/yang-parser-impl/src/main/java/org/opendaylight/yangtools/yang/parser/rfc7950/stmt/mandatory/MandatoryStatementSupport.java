/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class MandatoryStatementSupport extends
        AbstractStatementSupport<Boolean, MandatoryStatement, EffectiveStatement<Boolean, MandatoryStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.MANDATORY).build();

    public MandatoryStatementSupport() {
        super(YangStmtMapping.MANDATORY);
    }

    @Override
    public Boolean parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return Utils.parseBoolean(ctx, value);
    }

    @Override
    public MandatoryStatement createDeclared(final StmtContext<Boolean, MandatoryStatement, ?> ctx) {
        final MandatoryStatement ret = new MandatoryStatementImpl(ctx);
        if (EmptyMandatoryStatement.FALSE.equals(ret)) {
            return EmptyMandatoryStatement.FALSE;
        } else if (EmptyMandatoryStatement.TRUE.equals(ret)) {
            return EmptyMandatoryStatement.TRUE;
        } else {
            return ret;
        }
    }

    @Override
    public EffectiveStatement<Boolean, MandatoryStatement> createEffective(
            final StmtContext<Boolean, MandatoryStatement, EffectiveStatement<Boolean, MandatoryStatement>> ctx) {
        final EffectiveStatement<Boolean, MandatoryStatement> ret = new MandatoryEffectiveStatementImpl(ctx);
        final MandatoryStatement declared = ret.getDeclared();
        if (declared instanceof EmptyMandatoryStatement && ret.effectiveSubstatements().isEmpty()) {
            return ((EmptyMandatoryStatement)declared).toEffective();
        }
        return ret;
    }

    @Override
    public String internArgument(final String rawArgument) {
        return Utils.internBoolean(rawArgument);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
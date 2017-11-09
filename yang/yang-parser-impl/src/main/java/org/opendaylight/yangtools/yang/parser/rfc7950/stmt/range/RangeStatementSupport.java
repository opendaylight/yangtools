/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public final class RangeStatementSupport extends AbstractStatementSupport<List<ValueRange>, RangeStatement,
        EffectiveStatement<List<ValueRange>, RangeStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .RANGE)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.ERROR_APP_TAG)
        .addOptional(YangStmtMapping.ERROR_MESSAGE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    public RangeStatementSupport() {
        super(YangStmtMapping.RANGE);
    }

    @Override
    public List<ValueRange> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return TypeUtils.parseRangeListFromString(ctx, value);
    }

    @Override
    public RangeStatement createDeclared(final StmtContext<List<ValueRange>, RangeStatement, ?> ctx) {
        return new RangeStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<List<ValueRange>, RangeStatement> createEffective(
            final StmtContext<List<ValueRange>, RangeStatement, EffectiveStatement<List<ValueRange>,
                    RangeStatement>> ctx) {
        return new RangeEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
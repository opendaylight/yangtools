/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.fraction_digits;

import com.google.common.collect.Range;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class FractionDigitsStatementSupport extends AbstractStatementSupport<Integer, FractionDigitsStatement,
        EffectiveStatement<Integer, FractionDigitsStatement>> {
    private static final Range<Integer> FRAC_DIGITS_ALLOWED = Range.closed(1, 18);
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.FRACTION_DIGITS)
        .build();
    private static final FractionDigitsStatementSupport INSTANCE = new FractionDigitsStatementSupport();

    private FractionDigitsStatementSupport() {
        super(YangStmtMapping.FRACTION_DIGITS);
    }

    public static FractionDigitsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final Integer fractionDigits;
        try {
            fractionDigits = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e,
                "%s is not valid fraction-digits integer argument", value);
        }

        SourceException.throwIf(!FRAC_DIGITS_ALLOWED.contains(fractionDigits), ctx.getStatementSourceReference(),
            "fraction-digits argument should be integer within %s", FRAC_DIGITS_ALLOWED);
        return fractionDigits;
    }

    @Override
    public FractionDigitsStatement createDeclared(final StmtContext<Integer, FractionDigitsStatement, ?> ctx) {
        return new FractionDigitsStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Integer, FractionDigitsStatement> createEffective(
            final StmtContext<Integer, FractionDigitsStatement,
            EffectiveStatement<Integer, FractionDigitsStatement>> ctx) {
        return new FractionDigitsEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
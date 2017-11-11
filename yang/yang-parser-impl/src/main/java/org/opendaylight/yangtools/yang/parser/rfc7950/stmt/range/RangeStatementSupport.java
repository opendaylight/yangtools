/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.range;

import com.google.common.collect.Iterables;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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
    public List<ValueRange> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String rangeArgument) {
        final List<ValueRange> ranges = new ArrayList<>();

        for (final String singleRange : TypeUtils.PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TypeUtils.TWO_DOTS_SPLITTER.split(singleRange).iterator();
            final Number min = parseDecimalConstraintValue(ctx, boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(ctx, boundaries.next());

                // if min larger than max then error
                SourceException.throwIf(TypeUtils.compareNumbers(min, max) == 1, ctx.getStatementSourceReference(),
                        "Range constraint %s has descending order of boundaries; should be ascending", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx.getStatementSourceReference(),
                    "Wrong number of boundaries in range constraint %s", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            InferenceException.throwIf(ranges.size() > 1
                && TypeUtils.compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                ctx.getStatementSourceReference(),  "Some of the value ranges in %s are not disjoint",
                rangeArgument);
            ranges.add(ValueRange.of(min, max));
        }

        return ranges;
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

    private static Number parseDecimalConstraintValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }

        try {
            return value.indexOf('.') != -1 ? new BigDecimal(value) : new BigInteger(value);
        } catch (final NumberFormatException e) {
            throw new SourceException(String.format("Value %s is not a valid decimal number", value),
                    ctx.getStatementSourceReference(), e);
        }
    }
}
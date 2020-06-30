/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.length;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class LengthStatementSupport
        extends BaseStatementSupport<List<ValueRange>, LengthStatement, LengthEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .LENGTH)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.ERROR_APP_TAG)
        .addOptional(YangStmtMapping.ERROR_MESSAGE)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();
    private static final LengthStatementSupport INSTANCE = new LengthStatementSupport();

    private LengthStatementSupport() {
        super(YangStmtMapping.LENGTH, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static LengthStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public ImmutableList<ValueRange> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final List<ValueRange> ranges = new ArrayList<>();

        for (final String singleRange : ArgumentUtils.PIPE_SPLITTER.split(value)) {
            final Iterator<String> boundaries = ArgumentUtils.TWO_DOTS_SPLITTER.split(singleRange).iterator();
            final Number min = parseIntegerConstraintValue(ctx, boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseIntegerConstraintValue(ctx, boundaries.next());

                // if min larger than max then error
                SourceException.throwIf(ArgumentUtils.compareNumbers(min, max) == 1, ctx.getStatementSourceReference(),
                        "Length constraint %s has descending order of boundaries; should be ascending.", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx.getStatementSourceReference(),
                        "Wrong number of boundaries in length constraint %s.", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            InferenceException.throwIf(ranges.size() > 1
                && ArgumentUtils.compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                        ctx.getStatementSourceReference(),  "Some of the length ranges in %s are not disjoint", value);
            ranges.add(ValueRange.of(min, max));
        }

        return ImmutableList.copyOf(ranges);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected LengthStatement createDeclared(final StmtContext<List<ValueRange>, LengthStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularLengthStatement(ctx, substatements);
    }

    @Override
    protected LengthStatement createEmptyDeclared(final StmtContext<List<ValueRange>, LengthStatement, ?> ctx) {
        return new EmptyLengthStatement(ctx);
    }

    @Override
    protected LengthEffectiveStatement createEffective(
            final StmtContext<List<ValueRange>, LengthStatement, LengthEffectiveStatement> ctx,
            final LengthStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularLengthEffectiveStatement(declared, substatements);
    }

    @Override
    protected LengthEffectiveStatement createEmptyEffective(
            final StmtContext<List<ValueRange>, LengthStatement, LengthEffectiveStatement> ctx,
            final LengthStatement declared) {
        return new EmptyLengthEffectiveStatement(declared);
    }

    private static Number parseIntegerConstraintValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }

        try {
            return new BigInteger(value);
        } catch (final NumberFormatException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Value %s is not a valid integer", value);
        }
    }
}
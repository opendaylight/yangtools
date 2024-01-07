/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class RangeStatementSupport
        extends AbstractStatementSupport<List<ValueRange>, RangeStatement, RangeEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.RANGE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    public RangeStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.RANGE, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public ImmutableList<ValueRange> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String rangeArgument) {
        final List<ValueRange> ranges = new ArrayList<>();

        for (final String singleRange : ArgumentUtils.PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = ArgumentUtils.TWO_DOTS_SPLITTER.split(singleRange).iterator();
            final Number min = parseDecimalConstraintValue(ctx, boundaries.next());

            final Number max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(ctx, boundaries.next());

                // if min larger than max then error
                SourceException.throwIf(ArgumentUtils.compareNumbers(min, max) == 1, ctx,
                    "Range constraint %s has descending order of boundaries; should be ascending", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx,
                    "Wrong number of boundaries in range constraint %s", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            ctx.inferFalse(
                ranges.size() > 1 && ArgumentUtils.compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                "Some of the value ranges in %s are not disjoint", rangeArgument);
            ranges.add(ValueRange.of(min, max));
        }

        return ImmutableList.copyOf(ranges);
    }

    @Override
    protected RangeStatement createDeclared(final BoundStmtCtx<List<ValueRange>> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createRange(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected RangeStatement attachDeclarationReference(final RangeStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateRange(stmt, reference);
    }

    @Override
    protected RangeEffectiveStatement createEffective(final Current<List<ValueRange>, RangeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createRange(stmt.declared(), substatements);
    }

    private static @NonNull Number parseDecimalConstraintValue(final @NonNull StmtContext<?, ?, ?> ctx,
            final @NonNull String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }
        // Deal with decimal64, i.e. 'decimal-value' production of the RFC6020 ABNF
        if (value.indexOf('.') != -1) {
            try {
                return Decimal64.valueOf(value);
            } catch (NumberFormatException e) {
                throw new SourceException(ctx, e, "Value %s is not a valid decimal number", value);
            }
        }

        // This has to be an 'integer-value' production of the RFC6020 ABNF. We also clamp allowed range to Long/Uint,
        // as that is the effectively-valid range of allowed values. For Uint64 we also try to intern the value.
        try {
            return value.startsWith("-") ? Long.valueOf(value) : Uint64.valueOf(value).intern();
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Value %s is not a valid integral range number", value);
        }
    }
}

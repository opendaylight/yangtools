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
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
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

public final class LengthStatementSupport
        extends AbstractStatementSupport<List<ValueRange>, LengthStatement, LengthEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.LENGTH)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.ERROR_APP_TAG)
            .addOptional(YangStmtMapping.ERROR_MESSAGE)
            .addOptional(YangStmtMapping.REFERENCE)
            .build();

    public LengthStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.LENGTH, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
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
                SourceException.throwIf(ArgumentUtils.compareNumbers(min, max) == 1, ctx,
                    "Length constraint %s has descending order of boundaries; should be ascending.", singleRange);
                SourceException.throwIf(boundaries.hasNext(), ctx,
                    "Wrong number of boundaries in length constraint %s.", singleRange);
            } else {
                max = min;
            }

            // some of intervals overlapping
            ctx.inferFalse(
                ranges.size() > 1 && ArgumentUtils.compareNumbers(min, Iterables.getLast(ranges).upperBound()) != 1,
                "Some of the length ranges in %s are not disjoint", value);
            ranges.add(ValueRange.of(min, max));
        }

        return ImmutableList.copyOf(ranges);
    }

    @Override
    protected LengthStatement createDeclared(final BoundStmtCtx<List<ValueRange>> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createLength(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected LengthStatement attachDeclarationReference(final LengthStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateLength(stmt, reference);
    }

    @Override
    protected LengthEffectiveStatement createEffective(final Current<List<ValueRange>, LengthStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createLength(stmt.declared(), substatements);
    }

    private static @NonNull Number parseIntegerConstraintValue(final StmtContext<?, ?, ?> ctx, final String value) {
        if ("max".equals(value)) {
            return UnresolvedNumber.max();
        }
        if ("min".equals(value)) {
            return UnresolvedNumber.min();
        }

        // As per RFC6020/RFC7950 section 9.4.4:
        //
        //   An implementation is not required to support a length value larger than 18446744073709551615.
        //
        // We could support bigger precision at the cost of additional memory and/or potential ValueRange upper/lower
        // bound inconsistency. We also take advantage of Uint64's interning facilities.
        try {
            return Uint64.valueOf(value).intern();
        } catch (NumberFormatException e) {
            throw new SourceException(ctx, e, "Value %s is not a valid unsigned integer", value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Value %s exceeds maximum supported value %s", value, Uint64.MAX_VALUE);
        }
    }
}

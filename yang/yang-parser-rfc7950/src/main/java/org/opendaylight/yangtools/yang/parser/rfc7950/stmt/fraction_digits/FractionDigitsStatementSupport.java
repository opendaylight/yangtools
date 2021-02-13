/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.fraction_digits;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class FractionDigitsStatementSupport
        extends AbstractStatementSupport<Integer, FractionDigitsStatement, FractionDigitsEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.FRACTION_DIGITS).build();
    private static final FractionDigitsStatementSupport INSTANCE = new FractionDigitsStatementSupport();

    // FIXME: move this to yang-model-spi
    private static final ImmutableMap<FractionDigitsStatement, FractionDigitsEffectiveStatement> EMPTY_EFF;

    static {
        final Builder<FractionDigitsStatement, FractionDigitsEffectiveStatement> effBuilder = ImmutableMap.builder();
        for (int i = 1; i <= 18; ++i) {
            final FractionDigitsStatement decl = DeclaredStatements.createFractionDigits(i);
            effBuilder.put(decl, EffectiveStatements.createFractionDigits(decl));
        }
        EMPTY_EFF = effBuilder.build();
    }

    private FractionDigitsStatementSupport() {
        super(YangStmtMapping.FRACTION_DIGITS, StatementPolicy.contextIndependent());
    }

    public static FractionDigitsStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final int fractionDigits;
        try {
            fractionDigits = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SourceException(ctx, e, "%s is not valid fraction-digits integer argument", value);
        }
        if (fractionDigits < 1 || fractionDigits > 18) {
            throw new SourceException("fraction-digits argument should be integer within [1..18]", ctx);
        }
        return fractionDigits;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected FractionDigitsStatement createDeclared(final StmtContext<Integer, FractionDigitsStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createFractionDigits(ctx.getArgument(), substatements);
    }

    @Override
    protected FractionDigitsStatement createEmptyDeclared(final StmtContext<Integer, FractionDigitsStatement, ?> ctx) {
        return DeclaredStatements.createFractionDigits(ctx.getArgument());
    }

    @Override
    protected FractionDigitsEffectiveStatement createEffective(final Current<Integer, FractionDigitsStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createEmptyEffective(stmt.declared())
            : EffectiveStatements.createFractionDigits(stmt.declared(), substatements);
    }

    private static @NonNull FractionDigitsEffectiveStatement createEmptyEffective(
            final FractionDigitsStatement declared) {
        final FractionDigitsEffectiveStatement shared = EMPTY_EFF.get(declared);
        return shared != null ? shared : EffectiveStatements.createFractionDigits(declared);
    }
}

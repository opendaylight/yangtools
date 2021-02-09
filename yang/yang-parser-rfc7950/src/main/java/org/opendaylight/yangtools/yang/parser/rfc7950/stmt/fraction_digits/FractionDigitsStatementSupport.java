/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.fraction_digits;

import static com.google.common.base.Verify.verifyNotNull;

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

    private static final ImmutableMap<Integer, FractionDigitsStatement> EMPTY_DECLS;
    private static final ImmutableMap<FractionDigitsStatement, EmptyFractionDigitsEffectiveStatement> EMPTY_EFF;

    static {
        final Builder<Integer, FractionDigitsStatement> declBuilder = ImmutableMap.builder();
        final Builder<FractionDigitsStatement, EmptyFractionDigitsEffectiveStatement> effBuilder =
                ImmutableMap.builder();

        for (int i = 1; i <= 18; ++i) {
            final Integer argument = i;
            final FractionDigitsStatement decl = DeclaredStatements.createFractionDigits(argument);
            declBuilder.put(argument, decl);
            effBuilder.put(decl, new EmptyFractionDigitsEffectiveStatement(decl));
        }

        EMPTY_DECLS = declBuilder.build();
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
        final Integer argument = ctx.getArgument();
        return verifyNotNull(EMPTY_DECLS.get(argument), "No declared instance for %s", argument);
    }

    @Override
    protected FractionDigitsEffectiveStatement createEffective(final Current<Integer, FractionDigitsStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createEmptyEffective(stmt.declared())
            : new RegularFractionDigitsEffectiveStatement(stmt.declared(), substatements);
    }

    private static @NonNull FractionDigitsEffectiveStatement createEmptyEffective(
            final FractionDigitsStatement declared) {
        final EmptyFractionDigitsEffectiveStatement shared = EMPTY_EFF.get(declared);
        return shared != null ? shared :  new EmptyFractionDigitsEffectiveStatement(declared);
    }
}

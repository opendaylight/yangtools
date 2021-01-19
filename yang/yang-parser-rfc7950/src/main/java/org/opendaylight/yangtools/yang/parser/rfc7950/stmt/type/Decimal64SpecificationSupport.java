/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.DecimalTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class Decimal64SpecificationSupport extends AbstractStatementSupport<String, Decimal64Specification,
        EffectiveStatement<String, Decimal64Specification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addMandatory(YangStmtMapping.FRACTION_DIGITS)
        .addOptional(YangStmtMapping.RANGE)
        .build();

    Decimal64SpecificationSupport() {
        super(YangStmtMapping.TYPE, CopyPolicy.DECLARED_COPY);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected Decimal64Specification createDeclared(final StmtContext<String, Decimal64Specification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Decimal64SpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected Decimal64Specification createEmptyDeclared(final StmtContext<String, Decimal64Specification, ?> ctx) {
        throw noFracDigits(ctx);
    }

    @Override
    protected EffectiveStatement<String, Decimal64Specification> createEffective(
            final Current<String, Decimal64Specification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noFracDigits(stmt);
        }

        final DecimalTypeBuilder builder = BaseTypes.decimalTypeBuilder(stmt.wrapSchemaPath());
        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof FractionDigitsEffectiveStatement) {
                builder.setFractionDigits(((FractionDigitsEffectiveStatement) subStmt).argument());
            }
            if (subStmt instanceof RangeEffectiveStatement) {
                final RangeEffectiveStatement range = (RangeEffectiveStatement) subStmt;
                builder.setRangeConstraint(range, range.argument());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noFracDigits(final CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.3.4
         *
         *     The "fraction-digits" statement, which is a substatement to the
         *     "type" statement, MUST be present if the type is "decimal64".
         */
        return new SourceException("At least one fraction-digits statement has to be present", stmt);
    }
}
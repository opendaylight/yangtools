/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.DecimalTypeBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class Decimal64SpecificationSupport extends AbstractTypeSupport<Decimal64Specification> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(TypeStatement.DEFINITION)
            .addMandatory(FractionDigitsStatement.DEFINITION)
            .addOptional(RangeStatement.DEFINITION)
            .build();

    Decimal64SpecificationSupport(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected Decimal64Specification createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            throw noFracDigits(ctx);
        }
        return new Decimal64SpecificationImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected Decimal64Specification attachDeclarationReference(final Decimal64Specification stmt,
            final DeclarationReference reference) {
        return new RefDecimal64Specification(stmt, reference);
    }

    @Override
    protected EffectiveStatement<QName, Decimal64Specification> createEffective(
            final Current<QName, Decimal64Specification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noFracDigits(stmt);
        }

        final DecimalTypeBuilder builder = BaseTypes.decimalTypeBuilder(stmt.argumentAsTypeQName());
        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof FractionDigitsEffectiveStatement fracDigits) {
                builder.setFractionDigits(fracDigits.argument());
            }
            if (subStmt instanceof RangeEffectiveStatement range) {
                builder.setRangeConstraint(range, range.argument());
            }
        }

        try {
            return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
        } catch (ArithmeticException e) {
            throw new SourceException("Range constraint does not match fraction-digits: " + e.getMessage(), stmt, e);
        }
    }

    private static SourceException noFracDigits(final CommonStmtCtx stmt) {
        /*
         *  https://www.rfc-editor.org/rfc/rfc7950#section-9.3.4
         *
         *     The "fraction-digits" statement, which is a substatement to the
         *     "type" statement, MUST be present if the type is "decimal64".
         */
        return new SourceException("At least one fraction-digits statement has to be present", stmt);
    }
}

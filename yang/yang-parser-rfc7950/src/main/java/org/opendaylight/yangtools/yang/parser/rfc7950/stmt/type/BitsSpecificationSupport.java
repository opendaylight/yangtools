/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class BitsSpecificationSupport
        extends AbstractStatementSupport<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addMultiple(YangStmtMapping.BIT)
        .build();

    BitsSpecificationSupport() {
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
    protected BitsSpecification createDeclared(final StmtContext<String, BitsSpecification, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new BitsSpecificationImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected BitsSpecification createEmptyDeclared(final StmtContext<String, BitsSpecification, ?> ctx) {
        throw noBits(ctx);
    }

    @Override
    protected EffectiveStatement<String, BitsSpecification> createEffective(
            final Current<String, BitsSpecification> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBits(stmt);
        }

        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(stmt.wrapSchemaPath());
        Uint32 highestPosition = null;
        for (final EffectiveStatement<?, ?> subStmt : substatements) {
            if (subStmt instanceof BitEffectiveStatement) {
                final BitEffectiveStatement bitSubStmt = (BitEffectiveStatement) subStmt;

                final Optional<Uint32> declaredPosition = bitSubStmt.getDeclaredPosition();
                final Uint32 effectivePos;
                if (declaredPosition.isEmpty()) {
                    if (highestPosition != null) {
                        SourceException.throwIf(Uint32.MAX_VALUE.equals(highestPosition), stmt,
                            "Bit %s must have a position statement", bitSubStmt);
                        effectivePos = Uint32.fromIntBits(highestPosition.intValue() + 1);
                    } else {
                        effectivePos = Uint32.ZERO;
                    }
                } else {
                    effectivePos = declaredPosition.get();
                }

                final Bit bit = EffectiveTypeUtil.buildBit(bitSubStmt, effectivePos);
                if (highestPosition == null || highestPosition.compareTo(bit.getPosition()) < 0) {
                    highestPosition = bit.getPosition();
                }

                builder.addBit(bit);
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noBits(final CommonStmtCtx stmt) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.7.4:
         *
         *     The "bit" statement, which is a substatement to the "type" statement,
         *     MUST be present if the type is "bits".
         */
        return new SourceException("At least one bit statement has to be present", stmt);
    }
}

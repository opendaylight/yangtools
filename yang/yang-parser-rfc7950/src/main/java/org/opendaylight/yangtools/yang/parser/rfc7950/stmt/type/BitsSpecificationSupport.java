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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class BitsSpecificationSupport
        extends BaseStatementSupport<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPE)
        .addMultiple(YangStmtMapping.BIT)
        .build();

    BitsSpecificationSupport() {
        super(YangStmtMapping.TYPE);
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
        return new BitsSpecificationImpl(ctx, substatements);
    }

    @Override
    protected BitsSpecification createEmptyDeclared(final StmtContext<String, BitsSpecification, ?> ctx) {
        throw noBits(ctx);
    }

    @Override
    protected EffectiveStatement<String, BitsSpecification> createEffective(
            final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx,
            final BitsSpecification declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(ctx.getSchemaPath().get());
        Uint32 highestPosition = null;
        for (final EffectiveStatement<?, ?> stmt : substatements) {
            if (stmt instanceof BitEffectiveStatement) {
                final BitEffectiveStatement bitSubStmt = (BitEffectiveStatement) stmt;

                final Optional<Uint32> declaredPosition = bitSubStmt.getDeclaredPosition();
                final Uint32 effectivePos;
                if (declaredPosition.isEmpty()) {
                    if (highestPosition != null) {
                        SourceException.throwIf(Uint32.MAX_VALUE.equals(highestPosition),
                            ctx.getStatementSourceReference(), "Bit %s must have a position statement", bitSubStmt);
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

        return new TypeEffectiveStatementImpl<>(declared, substatements, builder);
    }

    @Override
    protected EffectiveStatement<String, BitsSpecification> createEmptyEffective(
            final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx,
            final BitsSpecification declared) {
        throw noBits(ctx);
    }

    private static SourceException noBits(final StmtContext<?, ?, ?> ctx) {
        /*
         *  https://tools.ietf.org/html/rfc7950#section-9.7.4:
         *
         *     The "bit" statement, which is a substatement to the "type" statement,
         *     MUST be present if the type is "bits".
         */
        return new SourceException("At least one bit statement has to be present", ctx.getStatementSourceReference());
    }
}

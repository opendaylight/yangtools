/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractBitStatementSupport
        extends AbstractStatementSupport<String, BitStatement, BitEffectiveStatement> {
    AbstractBitStatementSupport() {
        super(YangStmtMapping.BIT);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // Performs de-duplication and interning in one go
        return StmtContextUtils.parseIdentifier(ctx, value).getLocalName();
    }

    @Override
    public final BitStatement createDeclared(final StmtContext<String, BitStatement, ?> ctx) {
        return new BitStatementImpl(ctx);
    }

    @Override
    public final BitEffectiveStatement createEffective(
            final StmtContext<String, BitStatement, BitEffectiveStatement> ctx) {

        final StmtContext<String, BitStatement, BitEffectiveStatement> effective;
        if (!ctx.allSubstatementsStream().anyMatch(stmt -> stmt.getPublicDefinition() == YangStmtMapping.POSITION)) {
            // We do not have a position specified, we need to infer it from parent

            final StmtContext<?, ?, ?> parent = ctx.coerceParentContext();

            Long highestPosition = null;
            for (StmtContext<?, ?, ?> stmt : parent.allSubstatements()) {

            }


//            final Optional<Long> declaredPosition = bitStmt.getDeclaredPosition();
//            final long effectivePos;
//            if (declaredPosition.isEmpty()) {
//                if (highestPosition != null) {
//                    SourceException.throwIf(highestPosition == 4294967295L, ctx.getStatementSourceReference(),
//                            "Bit %s must have a position statement", bitStmt);
//                    effectivePos = highestPosition + 1;
//                } else {
//                    effectivePos = 0L;
//                }
//            } else {
//                effectivePos = declaredPosition.get();
//            }



//            // FIXME: this looks like a duplicate of BitsSpecificationEffectiveStatement
//            final Optional<Long> declared = bitSubStmt.getDeclaredPosition();
//            final long effectivePos;
//            if (declared.isEmpty()) {
//                effectivePos = getBaseTypeBitPosition(bitSubStmt.argument(), baseType, ctx);
//            } else {
//                effectivePos = declared.get();
//            }


//            private static long getBaseTypeBitPosition(final String bitName, final BitsTypeDefinition baseType,
//                    final StmtContext<?, ?, ?> ctx) {
//                for (Bit baseTypeBit : baseType.getBits()) {
//                    if (bitName.equals(baseTypeBit.getName())) {
//                        return baseTypeBit.getPosition();
//                    }
//                }
    //
//                throw new SourceException(ctx.getStatementSourceReference(),
//                        "Bit '%s' is not a subset of its base bits type %s.", bitName, baseType.getQName());
//            }

            effective = null;
        } else {
            effective = ctx;
        }

        return new BitEffectiveStatementImpl(effective);
    }

    private static StmtContext<String, BitStatement, BitEffectiveStatement> ensurePosition(
            final StmtContext<String, BitStatement, BitEffectiveStatement> ctx) {
        for (String stmt : ctx) {
            if (StmtContextUtils.producesDeclared(baseParentCtx, IdentityStatement.class)) {


   }
        }

}
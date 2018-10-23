/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@VisibleForTesting
// FIXME: hide this class
public final class BitsSpecificationEffectiveStatement extends
        DeclaredEffectiveStatementBase<String, BitsSpecification> implements TypeEffectiveStatement<BitsSpecification> {

    private final @NonNull BitsTypeDefinition typeDefinition;

    BitsSpecificationEffectiveStatement(
            final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx) {
        super(ctx);

        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(ctx.getSchemaPath().get());
        Long highestPosition = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof BitEffectiveStatementImpl) {
                final BitEffectiveStatementImpl bitSubStmt = (BitEffectiveStatementImpl) stmt;

                final long effectivePos;
                if (bitSubStmt.getDeclaredPosition() == null) {
                    if (highestPosition != null) {
                        SourceException.throwIf(highestPosition == 4294967295L, ctx.getStatementSourceReference(),
                                "Bit %s must have a position statement", bitSubStmt);
                        effectivePos = highestPosition + 1;
                    } else {
                        effectivePos = 0L;
                    }
                } else {
                    effectivePos = bitSubStmt.getDeclaredPosition();
                }

                final Bit bit = EffectiveTypeUtil.buildBit(bitSubStmt, effectivePos);
                SourceException.throwIf(bit.getPosition() < 0L && bit.getPosition() > 4294967295L,
                        ctx.getStatementSourceReference(), "Bit %s has illegal position", bit);

                if (highestPosition == null || highestPosition < bit.getPosition()) {
                    highestPosition = bit.getPosition();
                }

                builder.addBit(bit);
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode) stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public BitsTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}

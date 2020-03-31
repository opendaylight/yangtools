/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class BitsSpecificationEffectiveStatement extends DeclaredEffectiveStatementBase<String, BitsSpecification>
        implements TypeEffectiveStatement<BitsSpecification> {

    private final @NonNull BitsTypeDefinition typeDefinition;

    BitsSpecificationEffectiveStatement(
            final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx) {
        super(ctx);

        final BitsTypeBuilder builder = BaseTypes.bitsTypeBuilder(ctx.getSchemaPath().get());
        Uint32 highestPosition = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
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

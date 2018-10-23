/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class BitsTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements TypeEffectiveStatement<TypeStatement> {

    private final @NonNull BitsTypeDefinition typeDefinition;

    BitsTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final BitsTypeDefinition baseType) {
        super(ctx);

        final BitsTypeBuilder builder = RestrictedTypes.newBitsBuilder(baseType, ctx.getSchemaPath().get());

        final YangVersion yangVersion = ctx.getRootVersion();
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof BitEffectiveStatementImpl) {
                SourceException.throwIf(yangVersion != YangVersion.VERSION_1_1, ctx.getStatementSourceReference(),
                        "Restricted bits type is allowed only in YANG 1.1 version.");
                final BitEffectiveStatementImpl bitSubStmt = (BitEffectiveStatementImpl) stmt;

                final long effectivePos;
                if (bitSubStmt.getDeclaredPosition() == null) {
                    effectivePos = getBaseTypeBitPosition(bitSubStmt.getName(), baseType, ctx);
                } else {
                    effectivePos = bitSubStmt.getDeclaredPosition();
                }

                builder.addBit(EffectiveTypeUtil.buildBit(bitSubStmt, effectivePos));
            } else if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode) stmt);
            }
        }

        typeDefinition = builder.build();
    }

    private static long getBaseTypeBitPosition(final String bitName, final BitsTypeDefinition baseType,
            final StmtContext<?, ?, ?> ctx) {
        for (Bit baseTypeBit : baseType.getBits()) {
            if (bitName.equals(baseTypeBit.getName())) {
                return baseTypeBit.getPosition();
            }
        }

        throw new SourceException(ctx.getStatementSourceReference(),
                "Bit '%s' is not a subset of its base bits type %s.", bitName, baseType.getQName());
    }

    @Override
    public BitsTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}

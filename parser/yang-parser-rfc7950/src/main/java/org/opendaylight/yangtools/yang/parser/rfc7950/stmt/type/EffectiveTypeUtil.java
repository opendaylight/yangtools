/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.ri.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.EnumPairBuilder;

final class EffectiveTypeUtil {
    private EffectiveTypeUtil() {
        // Hidden on purpose
    }

    static @NonNull Bit buildBit(final @NonNull BitEffectiveStatement stmt, final Uint32 effectivePos) {
        final var bit = verifyWithStatus(stmt);

        // TODO: code duplication with EnumPairBuilder is indicating we could use a common Builder<?> interface
        final var builder = BitBuilder.create(stmt.argument(), effectivePos).setStatus(bit.getStatus());
        bit.getDescription().ifPresent(builder::setDescription);
        bit.getReference().ifPresent(builder::setReference);

        return builder.build();
    }

    static @NonNull EnumPair buildEnumPair(final @NonNull EnumEffectiveStatement stmt, final int effectiveValue) {
        final var node = verifyWithStatus(stmt);

        final var builder = EnumPairBuilder.create(stmt.requireDeclared().rawArgument(), effectiveValue)
                .setStatus(node.getStatus()).setUnknownSchemaNodes(node.getUnknownSchemaNodes());
        node.getDescription().ifPresent(builder::setDescription);
        node.getReference().ifPresent(builder::setReference);

        return builder.build();
    }

    private static @NonNull WithStatus verifyWithStatus(final EffectiveStatement<?, ?> stmt) {
        if (stmt instanceof WithStatus withStatus) {
            return withStatus;
        }
        throw new VerifyException("Unexpected statement " + stmt);
    }
}

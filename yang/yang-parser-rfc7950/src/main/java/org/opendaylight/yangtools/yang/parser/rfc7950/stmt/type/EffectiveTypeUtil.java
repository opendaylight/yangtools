/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;

@Beta
final class EffectiveTypeUtil {
    private EffectiveTypeUtil() {
        // Hidden on purpose
    }

    static @NonNull Bit buildBit(final @NonNull BitEffectiveStatement stmt, final Uint32 effectivePos) {
        verify(stmt instanceof WithStatus);
        final WithStatus bit = (WithStatus) stmt;

        // TODO: code duplication with EnumPairBuilder is indicating we could use a common Builder<?> interface
        final BitBuilder builder = BitBuilder.create(stmt.argument(), effectivePos).setStatus(bit.getStatus());
        bit.getDescription().ifPresent(builder::setDescription);
        bit.getReference().ifPresent(builder::setReference);

        return builder.build();
    }

    static @NonNull EnumPair buildEnumPair(final @NonNull EnumEffectiveStatement stmt, final int effectiveValue) {
        verify(stmt instanceof WithStatus);
        final WithStatus node = (WithStatus) stmt;

        final EnumPairBuilder builder = EnumPairBuilder.create(stmt.getDeclared().rawArgument(), effectiveValue)
                .setStatus(node.getStatus()).setUnknownSchemaNodes(node.getUnknownSchemaNodes());
        node.getDescription().ifPresent(builder::setDescription);
        node.getReference().ifPresent(builder::setReference);

        return builder.build();
    }
}

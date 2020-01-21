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
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumEffectiveStatementImpl;

@Beta
final class EffectiveTypeUtil {
    private EffectiveTypeUtil() {
        // Hidden on purpose
    }

    static @NonNull Bit buildBit(final @NonNull BitEffectiveStatement stmt, final long effectivePos) {
        verify(stmt instanceof DocumentedNode.WithStatus);
        final DocumentedNode.WithStatus bit = (WithStatus) stmt;

        // TODO: code duplication with EnumPairBuilder is indicating we could use a common Builder<?> interface
        final BitBuilder builder = BitBuilder.create(stmt.argument(), effectivePos).setStatus(bit.getStatus());
        bit.getDescription().ifPresent(builder::setDescription);
        bit.getReference().ifPresent(builder::setReference);

        return builder.build();
    }

    static @NonNull EnumPair buildEnumPair(final @NonNull EnumEffectiveStatementImpl stmt, final int effectiveValue) {
        final EnumPairBuilder builder = EnumPairBuilder.create(stmt.getName(), effectiveValue)
                .setStatus(stmt.getStatus()).setUnknownSchemaNodes(stmt.getUnknownSchemaNodes());
        stmt.getDescription().ifPresent(builder::setDescription);
        stmt.getReference().ifPresent(builder::setReference);

        return builder.build();
    }
}

/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit.BitEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_.EnumEffectiveStatementImpl;

@Beta
final class EffectiveTypeUtil {
    private EffectiveTypeUtil() {
        throw new UnsupportedOperationException();
    }

    static @NonNull Bit buildBit(final @NonNull BitEffectiveStatementImpl stmt, final long effectivePos) {
        // TODO: code duplication with EnumPairBuilder is indicating we could use a common Builder<?> interface
        final BitBuilder builder = BitBuilder.create(stmt.getPath(), effectivePos).setStatus(stmt.getStatus());
        stmt.getDescription().ifPresent(builder::setDescription);
        stmt.getReference().ifPresent(builder::setReference);

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

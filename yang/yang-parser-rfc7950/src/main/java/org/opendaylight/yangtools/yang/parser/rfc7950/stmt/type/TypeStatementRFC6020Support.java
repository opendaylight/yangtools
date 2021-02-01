/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class TypeStatementRFC6020Support extends AbstractTypeStatementSupport {
    private static final TypeStatementRFC6020Support INSTANCE = new TypeStatementRFC6020Support();

    private TypeStatementRFC6020Support() {
        // Hidden
    }

    public static TypeStatementRFC6020Support getInstance() {
        return INSTANCE;
    }

    @Override
    Bit addRestrictedBit(final CommonStmtCtx stmt, final BitsTypeDefinition base, final BitEffectiveStatement bit) {
        throw new SourceException("Restricted bits type is not allowed in YANG version 1", stmt);
    }


    @Override
    EnumPair addRestrictedEnum(final CommonStmtCtx stmt, final EnumTypeDefinition base,
            final EnumEffectiveStatement enumStmt) {
        throw new SourceException("Restricted enumeration type is not allowed in YANG version 1", stmt);
    }
}
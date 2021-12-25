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
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class TypeStatementRFC6020Support extends AbstractTypeStatementSupport {
    public TypeStatementRFC6020Support(final YangParserConfiguration config) {
        super(config);
    }

    @Override
    Bit addRestrictedBit(final EffectiveStmtCtx stmt, final BitsTypeDefinition base, final BitEffectiveStatement bit) {
        throw new SourceException("Restricted bits type is not allowed in YANG version 1", stmt);
    }

    @Override
    EnumPair addRestrictedEnum(final EffectiveStmtCtx stmt, final EnumTypeDefinition base,
            final EnumEffectiveStatement enumStmt) {
        throw new SourceException("Restricted enumeration type is not allowed in YANG version 1", stmt);
    }
}
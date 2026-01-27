/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.ArgumentSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Support for {@link ModifierStatement}'s argument.
 */
@NonNullByDefault
final class ModifierArgumentSupport extends ArgumentSupport.Explicit<ModifierKind> {
    ModifierArgumentSupport(final ArgumentDefinition<ModifierKind> definition) {
        super(definition);
    }

    @Override
    public String internRawArgument(final String rawArgument) {
        return "invert-match".equals(rawArgument) ? "invert-match" : rawArgument;
    }

    @Override
    public ModifierKind parseArgument(final CommonStmtCtx stmt, final String rawArgument) {
        return SourceException.unwrap(ModifierKind.parse(rawArgument), stmt,
            "'%s' is not valid argument of modifier statement", rawArgument);
    }
}

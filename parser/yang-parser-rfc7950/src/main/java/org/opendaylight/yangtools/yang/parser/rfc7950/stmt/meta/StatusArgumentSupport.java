/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ArgumentSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Support for {@link StatusStatement}'s argument.
 */
@NonNullByDefault
final class StatusArgumentSupport extends ArgumentSupport.Explicit<Status> {
    StatusArgumentSupport(final ArgumentDefinition<Status> definition) {
        super(definition);
    }

    @Override
    public String internRawArgument(final String rawArgument) {
        return switch (rawArgument) {
            case "current" -> "current";
            case "deprecated" -> "deprecated";
            case "obsolete" -> "obsolete";
            default -> rawArgument;
        };
    }

    @Override
    public Status parseArgument(final CommonStmtCtx stmt, final String rawArgument) {
        try {
            return Status.ofArgument(rawArgument);
        } catch (IllegalArgumentException e) {
            throw new SourceException(stmt, e,
                "Invalid status '%s', must be one of 'current', 'deprecated' or 'obsolete'", rawArgument);
        }
    }
}

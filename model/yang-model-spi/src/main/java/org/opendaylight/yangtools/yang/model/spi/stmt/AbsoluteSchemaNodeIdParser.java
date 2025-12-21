/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@NonNullByDefault
final class AbsoluteSchemaNodeIdParser extends SchemaNodeIdParser<SchemaNodeIdentifier.Absolute> {
    /**
     * Construct a new instance backed by specified {@link NodeIdentifierParser}.
     *
     * @param resolver the {@link NodeIdentifierParser}
     */
    AbsoluteSchemaNodeIdParser(final NodeIdentifierParser nodeIdentifierParser) {
        super(nodeIdentifierParser);
    }

    @Override
    public Absolute parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        if (rawArgument.isEmpty()) {
            throw new ArgumentSyntaxException("empty string", 0);
        }
        final var firstChar = rawArgument.charAt(0);
        if (firstChar != '/') {
            throw new ArgumentSyntaxException("'" + firstChar + "' is not '/'", 1);
        }
        return Absolute.of(parseNodeIdentifiers(rawArgument, 1));
    }
}

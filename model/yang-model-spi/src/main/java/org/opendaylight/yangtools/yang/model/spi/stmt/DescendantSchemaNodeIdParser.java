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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@NonNullByDefault
final class DescendantSchemaNodeIdParser extends SchemaNodeIdParser<SchemaNodeIdentifier.Descendant> {
    DescendantSchemaNodeIdParser(final NodeIdentifierParser nodeIdentifierParser) {
        super(nodeIdentifierParser);
    }

    @Override
    public Descendant parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        return Descendant.of(parseNodeIdentifiers(rawArgument, 0));
    }
}

/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

/**
 * Abstract base class for the two specializations encompassed by {@code schema-nodeid} ABNF production.
 */
@NonNullByDefault
abstract sealed class SchemaNodeIdParser<T extends SchemaNodeIdentifier> implements ArgumentParser<T>
        permits AbsoluteSchemaNodeIdParser, DescendantSchemaNodeIdParser {
    final NodeIdentifierParser nodeIdentifierParser;

    /**
     * Construct a new instance backed by specified {@link NodeIdentifierParser}.
     *
     * @param resolver the {@link NodeIdentifierParser}
     */
    SchemaNodeIdParser(final NodeIdentifierParser nodeIdentifierParser) {
        this.nodeIdentifierParser = requireNonNull(nodeIdentifierParser);
    }

    final ImmutableList<QName> parseNodeIdentifiers(final String str, final int beginIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        final var builder = ImmutableList.<QName>builder();

        int nextIndex = beginIndex;
        do {
            final int nextSlash = str.indexOf('/', nextIndex);
            builder.add(nodeIdentifierParser.parseNodeIdentifier(str, nextIndex,
                nextSlash == -1 ? str.length() : nextSlash));
            nextIndex = nextSlash + 1;
        } while (nextIndex != 0);

        return builder.build();
    }
}

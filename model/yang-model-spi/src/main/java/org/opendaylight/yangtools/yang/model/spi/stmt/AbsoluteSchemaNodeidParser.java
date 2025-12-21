/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

/**
 * An {@link ArgumentParser} creating {@link Absolute} representation of an {@code absolute-schema-nodeid} ABNF
 * production. Returned objects are quaranteed to have been {@link QName#intern() interned}.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public final class AbsoluteSchemaNodeidParser extends AbstractArgumentParser<Absolute> {
    private final DescendantSchemaNodeidParser descendantSchemaNodeidParser;

    /**
     * Construct a new instance backed by specified {@link DescendantSchemaNodeidParser}.
     *
     * @param descendantSchemaNodeidParser the {@link DescendantSchemaNodeidParser}
     */
    public AbsoluteSchemaNodeidParser(final DescendantSchemaNodeidParser descendantSchemaNodeidParser) {
        this.descendantSchemaNodeidParser = requireNonNull(descendantSchemaNodeidParser);
    }

    @Override
    public Absolute parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        return parseAbsoluteSchemaNodeidAs("absolute-schema-nodeid", rawArgument);
    }

    public Absolute parseAbsoluteSchemaNodeidAs(final String production, final String rawArgument)
            throws ArgumentSyntaxException, ArgumentBindingException {
        if (rawArgument.isEmpty()) {
            throw new ArgumentSyntaxException(production + " cannot be empty", 0);
        }
        final var firstChar = rawArgument.charAt(0);
        if (firstChar != '/') {
            throw new ArgumentSyntaxException("'" + firstChar + "' is not '/' as required by " + production, 1);
        }
        return Absolute.of(descendantSchemaNodeidParser.parseNodeIdentifiers(rawArgument, 1));
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return descendantSchemaNodeidParser.addToStringAttributes(helper);
    }
}

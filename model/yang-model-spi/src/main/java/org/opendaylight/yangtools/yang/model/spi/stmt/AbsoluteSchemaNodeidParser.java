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
        return parseAbsoluteSchemaNodeid(rawArgument, 0, rawArgument.length());
    }

    public Absolute parseAbsoluteSchemaNodeid(final String str, final int beginIndex, final int endIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        return parseAbsoluteSchemaNodeidAs("absolute-schema-nodeid", str, beginIndex, endIndex);
    }

    public Absolute parseAbsoluteSchemaNodeidAs(final String production, final String str, final int beginIndex,
            final int endIndex) throws ArgumentSyntaxException, ArgumentBindingException {
        if (beginIndex == endIndex) {
            throw new ArgumentSyntaxException(production + " cannot be empty", beginIndex);
        }

        final var nextBegin = beginIndex + 1;
        final var firstChar = str.charAt(beginIndex);
        if (firstChar != '/') {
            throw new ArgumentSyntaxException("'" + firstChar + "' is not '/' as required by " + production, nextBegin);
        }
        return Absolute.of(descendantSchemaNodeidParser.parseNodeIdentifiers(str, nextBegin, endIndex));
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return descendantSchemaNodeidParser.addToStringAttributes(helper);
    }
}

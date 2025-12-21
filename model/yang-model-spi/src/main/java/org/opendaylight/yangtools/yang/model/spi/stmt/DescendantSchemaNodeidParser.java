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
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

/**
 * An {@link ArgumentParser} creating {@link Descendant} representation of an {@code descendant-schema-nodeid} ABNF
 * production. Returned objects are quaranteed to have been {@link QName#intern() interned}.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public final class DescendantSchemaNodeidParser extends AbstractArgumentParser<Descendant> {
    private final NodeIdentifierParser nodeIdentifierParser;

    /**
     * Construct an instance using specified {@link NodeIdentifierParser}.
     *
     * @param nodeIdentifierParser the {@link NodeIdentifierParser}
     */
    public DescendantSchemaNodeidParser(final NodeIdentifierParser nodeIdentifierParser) {
        this.nodeIdentifierParser = requireNonNull(nodeIdentifierParser);
    }

    @Override
    public Descendant parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        return parseDescendantSchemaNodeid(rawArgument, 0, rawArgument.length());
    }

    /**
     * Parse a {@code descendant-schema-nodeid} ABNF production string.
     *
     * @param str the string to parse
     * @param beginIndex first character, included
     * @param endIndex last character, excluded
     * @return a {@link Descendant} presenting the {@code descendant-schema-nodeid} bound to the current module
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code descendant-schema-nodeid}
     */
    public Descendant parseDescendantSchemaNodeid(final String str, final int beginIndex, final int endIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        return parseDescendantSchemaNodeidAs("descendant-schema-nodeid", str, beginIndex, endIndex);
    }

    /**
     * Parse a {@code descendant-schema-nodeid}-equivalent ABNF production string.
     *
     * @param production the production being parsed
     * @param str the string to parse
     * @param beginIndex first character, included
     * @param endIndex last character, excluded
     * @return a {@link Descendant} presenting the {@code descendant-schema-nodeid} bound to the current module
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code descendant-schema-nodeid}
     */
    public Descendant parseDescendantSchemaNodeidAs(final String production, final String str, final int beginIndex,
            final int endIndex) throws ArgumentSyntaxException, ArgumentBindingException {
        if (beginIndex == endIndex) {
            throw new ArgumentSyntaxException(production + " cannot be empty", beginIndex);
        }
        return Descendant.of(parseNodeIdentifiers(str, beginIndex, endIndex));
    }

    // Note: assumes str is non-empty, str is being cons
    ImmutableList<QName> parseNodeIdentifiers(final String str, final int beginIndex, final int endIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        final var builder = ImmutableList.<QName>builder();
        var nextIndex = beginIndex;

        // process all slash-delimited node-identifiers first
        while (true) {
            final int slash = str.indexOf('/', nextIndex, endIndex);
            if (slash == -1) {
                break;
            }

            // let's see if the slash is the first character: if so it is masquerading as at first node-identifier
            // character and we catch that here
            if (slash == nextIndex) {
                throw new ArgumentSyntaxException("'/' is not a valid prefix nor identifier", slash + 1);
            }

            builder.add(nodeIdentifierParser.parseNodeIdentifier(str, nextIndex, slash));
            nextIndex = slash + 1;
        }

        // final node-identifier
        builder.add(nodeIdentifierParser.parseNodeIdentifier(str, nextIndex, endIndex));
        return builder.build();
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return nodeIdentifierParser.addToStringAttributes(helper);
    }
}

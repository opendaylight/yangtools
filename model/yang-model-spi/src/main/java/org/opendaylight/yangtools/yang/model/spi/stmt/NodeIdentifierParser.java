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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

/**
 * An {@link ArgumentParser} creating {@link QName} representation of an {@code node-identifier} ABNF production.
 * Returned objects are quaranteed to have been {@link QName#intern() interned}.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public final class NodeIdentifierParser extends AbstractArgumentParser<QName> {
    private final IdentifierParser identifierParser;

    /**
     * Construct an instance using specified {@link IdentifierParser}.
     *
     * @param identifierParser the {@link IdentifierParser}
     */
    public NodeIdentifierParser(final IdentifierParser identifierParser) {
        this.identifierParser = requireNonNull(identifierParser);
    }

    @Override
    public QName parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        return parseNodeIdentifier(rawArgument, 0, rawArgument.length());
    }

    /**
     * Parse a {@code node-identifier} ABNF production string.
     *
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return a {@link QName} presenting the {@code node-identifier} bound to the current module
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code node-identifier}
     */
    public QName parseNodeIdentifier(final String str, final int beginIndex, final int endIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        return parseNodeIdentifierAs("node-identifier", str, beginIndex, endIndex);
    }

    /**
     * Parse a {@code node-identifier}-equivalent ABNF production string.
     *
     * @param production the production being parsed
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return a {@link QName} presenting the {@code node-identifier} bound to the current module
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code node-identifier}
     */
    public QName parseNodeIdentifierAs(final String production, final String str, final int beginIndex,
            final int endIndex) throws ArgumentSyntaxException, ArgumentBindingException {
        if (beginIndex == endIndex) {
            throw new ArgumentSyntaxException(production + " cannot be empty", beginIndex);
        }

        final int colonIndex = str.indexOf(':', beginIndex, endIndex);
        if (colonIndex == beginIndex) {
            // We have ":" or ":aa" or similar: pretend we are a character-at-a-time parser
            throw new ArgumentSyntaxException("':' is not a valid prefix nor identifier", colonIndex + 1);
        }
        if (colonIndex == -1) {
            return identifierParser.parseIdentifier(str.substring(beginIndex, endIndex), beginIndex);
        }

        final var module = parsePrefix(str.substring(beginIndex, colonIndex), beginIndex);
        final int identifierIndex = colonIndex + 1;
        return IdentifierParser.lexIdentifier(str.substring(identifierIndex, endIndex), identifierIndex)
            .bindTo(module)
            .intern();
    }

    private QNameModule parsePrefix(final String str, final int beginIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        final var prefix = IdentifierParser.lexIdentifierAs("prefix", str, beginIndex);
        final var module = identifierParser.namespaceBinding.lookupModule(prefix);
        if (module == null) {
            throw new ArgumentBindingException("Prefix '" + str + "' cannot be resolved", beginIndex + 1);
        }
        return module;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("namespaceBinding", identifierParser.namespaceBinding);
    }
}

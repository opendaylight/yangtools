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
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentBindingException;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

@NonNullByDefault
final class NodeIdentifierParser extends AbstractArgumentParser<QName> {
    private final IdentifierParser identifierParser;

    /**
     * Construct an instance using specified {@link NamespaceBinding}.
     *
     * @param namespaceBinding the {@link NamespaceBinding}
     */
    NodeIdentifierParser(final IdentifierParser identifierBinding) {
        identifierParser = requireNonNull(identifierBinding);
    }

    @Override
    public QName parseArgument(final String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException {
        return parseNodeIdentifier(rawArgument, 0, rawArgument.length());
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("namespaceBinding", identifierParser.namespaceBinding);
    }

    QName parseNodeIdentifier(final String str, final int beginIndex, final int endIndex)
            throws ArgumentSyntaxException, ArgumentBindingException {
        if (beginIndex == endIndex) {
            throw new ArgumentSyntaxException("node-identifier cannot be empty", beginIndex);
        }

        final int colonIndex = str.indexOf(':', beginIndex, endIndex);
        if (colonIndex == beginIndex) {
            // We have ":" or ":aa" or similar: pretend we are a character-at-a-time parser
            throw new ArgumentSyntaxException("':' is not a valid prefix nor identifier", colonIndex + 1);
        }
        if (colonIndex == -1) {
            return identifierParser.parseIdentifier(str.substring(beginIndex, endIndex), beginIndex);
        }

        final var prefixStr = str.substring(beginIndex, colonIndex);
        final var prefix = IdentifierParser.lexIdentifierAs("prefix", prefixStr, beginIndex);

        final int identifierIndex = colonIndex + 1;
        final var identifier = IdentifierParser.lexIdentifier(str.substring(identifierIndex, endIndex),
            identifierIndex);

        final var module = identifierParser.namespaceBinding.lookupModule(prefix);
        if (module == null) {
            throw new ArgumentBindingException("Prefix '" + prefixStr + "' cannot be resolved", beginIndex + 1);
        }

        return identifier.bindTo(module).intern();
    }
}

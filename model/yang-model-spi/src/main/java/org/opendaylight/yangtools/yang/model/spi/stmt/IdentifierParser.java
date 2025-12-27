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
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.YangNames;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;

/**
 * An {@link ArgumentParser} creating {@link QName} representation of an {@code identifier} as bound by
 * {@link NamespaceBinding}. Returned objects are quaranteed to have been {@link QName#intern() interned}.
 */
@NonNullByDefault
public final class IdentifierParser extends AbstractArgumentParser<QName> implements ArgumentParser.SyntaxOnly<QName> {
    private final NamespaceBinding namespaceBinding;

    /**
     * Construct an instance using specified {@link NamespaceBinding}.
     *
     * @param namespaceBinding the {@link NamespaceBinding}
     */
    public IdentifierParser(final NamespaceBinding namespaceBinding) {
        this.namespaceBinding = requireNonNull(namespaceBinding);
    }

    @Override
    public QName parseArgument(final String rawArgument) throws ArgumentSyntaxException {
        final var str = requireNonNull(rawArgument);
        final var identifier = UnresolvedQName.tryLocalName(str);
        if (identifier == null) {
            throw describeViolation(str);
        }
        return identifier.bindTo(namespaceBinding.currentModule()).intern();
    }

    // run manual validation so we can report the exact cause why the string was rejected
    private static ArgumentSyntaxException describeViolation(final String str) {
        if (str.isEmpty()) {
            return new ArgumentSyntaxException("identifier cannot be empty", 0);
        }
        // TODO: try to handle complete codepoints, but that redefines the meaning of ArgumentException.getPosition()
        final var ch = str.charAt(0);
        if (!YangNames.IDENTIFIER_START.matches(ch)) {
            return new ArgumentSyntaxException("'" + ch + "' is not valid as a first character in an identifier", 1);
        }
        final var index = YangNames.NOT_IDENTIFIER_PART.indexIn(str, 1);
        return new ArgumentSyntaxException("'" + str.charAt(index) + "' is not valid as a character in an identifier",
            index + 1);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("currentModule", namespaceBinding.currentModule());
    }
}

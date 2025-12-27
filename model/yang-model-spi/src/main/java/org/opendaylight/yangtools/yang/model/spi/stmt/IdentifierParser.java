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
 * An {@link ArgumentParser} creating {@link QName} representation of an {@code identifier-arg-str} ABNF production
 * as decoded into {@code identifier-arg} by YANG argument string handling procedures. Returned objects are quaranteed
 * to have been {@link QName#intern() interned}.
 *
 * {@apiNote This class is designed to support parsing of other ABNF productions which boil down to an
 *           {@code identifier}}
 */
@NonNullByDefault
public final class IdentifierParser extends AbstractArgumentParser<QName> implements ArgumentParser.SyntaxOnly<QName> {
    // package-visible for common use
    final NamespaceBinding namespaceBinding;

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
        // assumes identifier-arg-str part has been taken care of by caller
        return parseIdentifierArgStr(requireNonNull(rawArgument), 0);
    }

    // split out for implementation layout consistency
    private QName parseIdentifierArgStr(final String str, final int beginIndex) throws ArgumentSyntaxException {
        return parseIdentifierAs("identifier-arg", str, beginIndex);
    }

    /**
     * Parse an {@code identifier}-equivalent ABNF production string.
     *
     * @param production the production being parsed
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return a {@link QName} presenting the {@code identifier} bound to the current module
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code identifier}
     */
    public QName parseIdentifierAs(final String production, final String str, final int beginIndex)
            throws ArgumentSyntaxException {
        return lexIdentifierAs(production, str, beginIndex)
            .bindTo(namespaceBinding.currentModule())
            .intern();
    }

    /**
     * Parse {@code identifier} ABNF production.
     *
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return an {@link QName}
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code identifier}
     */
    QName parseIdentifier(final String str, final int beginIndex) throws ArgumentSyntaxException {
        return lexIdentifier(str, beginIndex).bindTo(namespaceBinding.currentModule()).intern();
    }

    /**
     * Lex {@code identifier} ABNF production.
     *
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return an {@link UnresolvedQName.Unqualified}
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code identifier}
     */
    static UnresolvedQName.Unqualified lexIdentifier(final String str, final int beginIndex)
            throws ArgumentSyntaxException {
        return lexIdentifierAs("identifier", str, beginIndex);
    }

    /**
     * Lex an ABNF production as an equivalent of the {@code identifier} ABNF production, such as
     * {@code identifier-arg}, {@code prefix} and similar. Not interned for wider reuse.
     *
     * @param production the production being parsed
     * @param str the string to parse
     * @param beginIndex logical start index for error reporting
     * @return an {@link UnresolvedQName.Unqualified}
     * @throws ArgumentSyntaxException if {@code str} does not conform to {@code identifier}
     */
    private static UnresolvedQName.Unqualified lexIdentifierAs(final String production, final String str,
            final int beginIndex) throws ArgumentSyntaxException {
        final var identifier = UnresolvedQName.tryLocalName(str);
        if (identifier == null) {
            throw syntaxExceptionOf(production, str, beginIndex);
        }
        return identifier;
    }

    // run manual validation so we can report the exact cause why the string was rejected
    private static ArgumentSyntaxException syntaxExceptionOf(final String production, final String str,
            final int beginIndex) {
        if (str.isEmpty()) {
            return new ArgumentSyntaxException(production + " cannot be empty", beginIndex);
        }
        // TODO: try to handle complete codepoints, but that redefines the meaning of ArgumentException.getPosition()
        final var ch = str.charAt(0);
        if (!YangNames.IDENTIFIER_START.matches(ch)) {
            return new ArgumentSyntaxException("'" + ch + "' is not valid as a first character in " + production,
                beginIndex + 1);
        }
        final var index = YangNames.NOT_IDENTIFIER_PART.indexIn(str, 1);
        return new ArgumentSyntaxException("'" + str.charAt(index) + "' is not valid as a character in " + production,
            beginIndex + index + 1);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("currentModule", namespaceBinding.currentModule());
    }
}

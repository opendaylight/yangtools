/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.text.ParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * A stateless utility for parsing YANG ABNF productions related to {@link SchemaNodeIdentifier}. {@link QName}s handed
 * out by this class are guaranteed to have been {@link QName#intern() interned}.
 *
 * @since 14.0.22
 */
@NonNullByDefault
public final class SchemaNodeIdentifierParser {
    /**
     * Interface for resolving the namespace part of YANG {@code identifier}.
     *
     * @since 14.0.22
     */
    public interface ModuleResolver {
        /**
         * {@return the current module, i.e. the {@link QNameModule} a plain {@code identifier}s are bound to}
         */
        QNameModule currentModule();

        /**
         * {@return the module for specified prefix, or {@code null} if no such module exists}
         * @param prefix the prefix
         */
        @Nullable QNameModule lookupModule(UnresolvedQName.Unqualified prefix);
    }

    private final ModuleResolver resolver;

    /**
     * Construct a new instance backed by specified {@link ModuleResolver}.
     *
     * @param resolver the {@link ModuleResolver}
     */
    public SchemaNodeIdentifierParser(final ModuleResolver resolver) {
        this.resolver = requireNonNull(resolver);
    }

    /**
     * Parse a string conforming to the YANG {@code identifier} production.
     *
     * @param str the string
     * @return a {@link QName}
     * @throws ParseException if the string cannot be parsed
     */
    public QName parseIdentifier(final String str) throws ParseException {
        return parseIdentifier(resolver.currentModule(), str, 0);
    }

    private static QName parseIdentifier(final QNameModule module, final String str, final int beginIndex)
            throws ParseException {
        final var identifier = UnresolvedQName.tryLocalName(str);
        if (identifier == null) {
            throw new ParseException("'" + str + "' is not a valid identifier", beginIndex);
        }
        return identifier.bindTo(module).intern();
    }

    /**
     * Parse a string conforming to the YANG {@code node-identifier} production.
     *
     * @param str the string
     * @return a {@link QName}
     * @throws ParseException if the string cannot be parsed
     * @throws UnknownPrefixException if the prefix cannot be resolved
     */
    public QName parseNodeIdentifier(final String str) throws ParseException, UnknownPrefixException {
        return parseNodeIdentifier(str, 0);
    }

    private QName parseNodeIdentifier(final String str, final int beginIndex)
            throws ParseException, UnknownPrefixException {
        final int colon = str.indexOf(':');
        if (colon == -1) {
            return parseIdentifier(resolver.currentModule(), str, beginIndex);
        }

        final var prefixStr = str.substring(0, colon);
        final var prefix = UnresolvedQName.tryLocalName(prefixStr);
        if (prefix == null) {
            throw new ParseException("'" + prefixStr + "' is not a valid prefix", beginIndex);
        }

        final var module = resolver.lookupModule(prefix);
        if (module == null) {
            throw new UnknownPrefixException("Prefix '" + prefixStr + "' cannot be resolved", beginIndex);
        }

        final var colonPlusOne = colon + 1;
        return parseIdentifier(module, str.substring(colonPlusOne), beginIndex + colonPlusOne);
    }

    /**
     * Parse a string conforming to the YANG {@code absolute-schema-nodeid} production.
     *
     * @param str the string
     * @return an {@link Absolute}
     * @throws ParseException if the string cannot be parsed
     * @throws UnknownPrefixException if any a prefix cannot be resolved
     */
    public Absolute parseAbsoluteSchemaNodeid(final String str) throws ParseException, UnknownPrefixException {
        if (str.isEmpty() || str.charAt(0) != '/') {
            throw new ParseException("'" + str + "' does not start with '/'", 0);
        }
        return Absolute.of(parseSchemaNodeId(str, 1));
    }

    /**
     * Parse a string conforming to the YANG {@code descendant-schema-nodeid} production.
     *
     * @param str the string
     * @return a {@link Descendant}
     * @throws ParseException if the string cannot be parsed
     * @throws UnknownPrefixException if any a prefix cannot be resolved
     */
    public Descendant parseDescendantSchemaNodeid(final String str) throws ParseException, UnknownPrefixException {
        return Descendant.of(parseSchemaNodeId(str, 0));
    }

    private ImmutableList<QName> parseSchemaNodeId(final String str, final int beginIndex)
            throws ParseException, UnknownPrefixException {
        final var builder = ImmutableList.<QName>builder();

        int nextIndex = beginIndex;
        do {
            final int nextSlash = str.indexOf('/', nextIndex);
            // TODO: do not use substring() here but rather pass a 'toIndex' parameter to parseNodeIdentifier()
            final var nextStr = nextSlash == -1 ? str.substring(nextIndex) : str.substring(nextIndex, nextSlash);
            builder.add(parseNodeIdentifier(nextStr, nextIndex));
            nextIndex = nextSlash + 1;
        } while (nextIndex != 0);

        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("resolver", resolver).toString();
    }
}

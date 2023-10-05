/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import java.util.Set;
import javax.xml.XMLConstants;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.PathMixin;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

/**
 * Abstract utility class for representations which encode {@link YangInstanceIdentifier} as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
public abstract class AbstractStringInstanceIdentifierCodec extends AbstractNamespaceCodec<YangInstanceIdentifier>
        implements InstanceIdentifierCodec<String> {
    // Escaper as per https://www.rfc-editor.org/rfc/rfc7950#section-6.1.3
    private static final Escaper DQUOT_ESCAPER = Escapers.builder()
        .addEscape('\n', "\\n")
        .addEscape('\t', "\\t")
        .addEscape('"', "\\\"")
        .addEscape('\\', "\\\\")
        .build();

    @Override
    protected final String serializeImpl(final YangInstanceIdentifier data) {
        final StringBuilder sb = new StringBuilder();
        DataSchemaContext current = getDataContextTree().getRoot();
        QNameModule lastModule = null;
        for (var arg : data.getPathArguments()) {
            current = current instanceof Composite composite ? composite.childByArg(arg) : null;
            if (current == null) {
                throw new IllegalArgumentException(
                    "Invalid input %s: schema for argument %s (after \"%s\") not found".formatted(data, arg, sb));
            }

            if (current instanceof PathMixin) {
                /*
                 * XML/YANG instance identifier does not have concept of augmentation identifier, or list as whole which
                 * identifies a mixin (same as the parent element), so we can safely ignore it if it is part of path
                 * (since child node) is identified in same fashion.
                 */
                continue;
            }

            final var qname = arg.getNodeType();
            sb.append('/');
            appendQName(sb, qname, lastModule);
            lastModule = qname.getModule();

            if (arg instanceof NodeIdentifierWithPredicates nip) {
                for (var entry : nip.entrySet()) {
                    final var keyName = entry.getKey();
                    appendQName(sb.append('['), keyName, lastModule).append('=');
                    appendValue(sb, keyName.getModule(), entry.getValue()).append(']');
                }
            } else if (arg instanceof NodeWithValue<?> val) {
                appendValue(sb.append("[.="), lastModule, val.getValue()).append(']');
            }
        }
        return sb.toString();
    }

    private StringBuilder appendValue(final StringBuilder sb, final QNameModule currentModule,
            final Object value) {
        if (value instanceof QName qname) {
            // QName implies identity-ref, which can never be escaped
            return appendQName(sb.append('\''), qname, currentModule).append('\'');
        }
        // FIXME: YANGTOOLS-1426: update once we have a dedicated type
        if (value instanceof Set<?> bits) {
            // Set implies bits, which can never be escaped and need to be serialized as space-separated items
            sb.append('\'');

            final var it = bits.iterator();
            if (it.hasNext()) {
                sb.append(checkBitsItem(it.next()));
                while (it.hasNext()) {
                    sb.append(' ').append(checkBitsItem(it.next()));
                }
            }

            return sb.append('\'');
        }

        final var str = value instanceof YangInstanceIdentifier id ? serialize(id) : String.valueOf(value);

        // We have two specifications here: Section 6.1.3 of both RFC6020 and RFC7950:
        //
        // RFC6020 Section 6.1.3:
        //        If a string contains any space or tab characters, a semicolon (";"),
        //        braces ("{" or "}"), or comment sequences ("//", "/*", or "*/"), then
        //        it MUST be enclosed within double or single quotes.
        //
        // RFC7950 Section 6.1.3:
        //        An unquoted string is any sequence of characters that does not
        //        contain any space, tab, carriage return, or line feed characters, a
        //        single or double quote character, a semicolon (";"), braces ("{" or
        //        "}"), or comment sequences ("//", "/*", or "*/").
        //
        // Plus the common part:
        //        A single-quoted string (enclosed within ' ') preserves each character
        //        within the quotes.  A single quote character cannot occur in a
        //        single-quoted string, even when preceded by a backslash.
        //
        // Unquoted strings are not interesting, as we are embedding the value in a string, not a YANG document, hence
        // we have to use quotes. Single-quoted case is simpler, as it does not involve any escaping. The only case
        // where we cannot use it is when the value itself has a single-quote in itself -- then we call back to
        // double-quoting.

        return str.indexOf('\'') == -1
            // No escaping needed, use single quotes
            ? sb.append('\'').append(str).append('\'')
            // Escaping needed: use double quotes
            : sb.append('"').append(DQUOT_ESCAPER.escape(str)).append('"');
    }

    /**
     * Returns DataSchemaContextTree associated with SchemaContext for which
     * serialization / deserialization occurs.
     *
     * <p>
     * Implementations MUST provide non-null Data Tree context, in order
     * for correct serialization / deserialization of PathArguments,
     * since XML representation does not have Augmentation arguments
     * and does not provide path arguments for cases.
     *
     * <p>
     * This effectively means same input XPath representation of Path Argument
     * may result in different YangInstanceIdentifiers if models are different
     * in uses of choices and cases.
     *
     * @return DataSchemaContextTree associated with SchemaContext for which
     *         serialization / deserialization occurs.
     */
    protected abstract @NonNull DataSchemaContextTree getDataContextTree();

    protected abstract @NonNull Object deserializeKeyValue(@NonNull DataSchemaNode schemaNode,
        @NonNull LeafrefResolver resolver, String value);

    @Override
    protected final YangInstanceIdentifier deserializeImpl(final String data) {
        return YangInstanceIdentifier.of(
            new XpathStringParsingPathArgumentBuilder(this, requireNonNull(data)).build());
    }

    /**
     * Create QName from unprefixed name, potentially taking last QNameModule encountered into account.
     *
     * @param lastModule last QNameModule encountered, potentially null
     * @param localName Local name string
     * @return A newly-created QName
     */
    protected @NonNull QName createQName(final @Nullable QNameModule lastModule, final String localName) {
        // This implementation handles both XML encoding, where we follow XML namespace rules and old JSON encoding,
        // which is the same thing: always encode prefixes
        return createQName(XMLConstants.DEFAULT_NS_PREFIX, localName);
    }

    @Override
    protected final QName createQName(final String prefix, final String localName) {
        final var module = moduleForPrefix(prefix);
        if (module != null) {
            return QName.create(module, localName);
        }
        throw new IllegalArgumentException("Failed to lookup prefix " + prefix);
    }

    /**
     * Resolve a string prefix into the corresponding module.
     *
     * @param prefix Prefix
     * @return module mapped to prefix, or null if the module cannot be resolved
     */
    protected abstract @Nullable QNameModule moduleForPrefix(@NonNull String prefix);

    // FIXME: YANGTOOLS-1426: this will not be necessary when we have dedicated bits type
    private static @NonNull String checkBitsItem(final Object obj) {
        if (obj instanceof String str) {
            return str;
        }
        throw new IllegalArgumentException("Unexpected bits component " + obj);
    }
}

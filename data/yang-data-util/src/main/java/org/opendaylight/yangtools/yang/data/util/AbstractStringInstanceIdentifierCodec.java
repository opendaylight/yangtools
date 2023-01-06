/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.xml.XMLConstants;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

/**
 * Abstract utility class for representations which encode {@link YangInstanceIdentifier} as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
@Beta
public abstract class AbstractStringInstanceIdentifierCodec extends AbstractNamespaceCodec<YangInstanceIdentifier>
        implements InstanceIdentifierCodec<String> {
    @Override
    protected final String serializeImpl(final YangInstanceIdentifier data) {
        final StringBuilder sb = new StringBuilder();
        DataSchemaContextNode<?> current = getDataContextTree().getRoot();
        QNameModule lastModule = null;
        for (var arg : data.getPathArguments()) {
            current = current.getChild(arg);
            checkArgument(current != null, "Invalid input %s: schema for argument %s (after %s) not found", data, arg,
                    sb);

            if (current.isMixin()) {
                /*
                 * XML/YANG instance identifier does not have concept
                 * of augmentation identifier, or list as whole which
                 * identifies a mixin (same as the parent element),
                 * so we can safely ignore it if it is part of path
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
                    appendQName(sb.append('['), entry.getKey(), lastModule);
                    sb.append("='").append(String.valueOf(entry.getValue())).append("']");
                }
            } else if (arg instanceof NodeWithValue<?> val) {
                sb.append("[.='").append(val.getValue()).append("']");
            }
        }
        return sb.toString();
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

    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final LeafrefResolver resolver,
            final String value) {
        return value;
    }

    @Override
    protected final YangInstanceIdentifier deserializeImpl(final String data) {
        return YangInstanceIdentifier.create(
            new XpathStringParsingPathArgumentBuilder(this, requireNonNull(data)).build());
    }

    /**
     * Create QName from unprefixed name, potentially taking last QNameModule encountered into account.
     *
     * @param lastModule last QNameModule encountered, potentially null
     * @param localName Local name string
     * @return A newly-created QName
     */
    protected QName createQName(final @Nullable QNameModule lastModule, final String localName) {
        // This implementation handles both XML encoding, where we follow XML namespace rules and old JSON encoding,
        // which is the same thing: always encode prefixes
        return createQName(XMLConstants.DEFAULT_NS_PREFIX, localName);
    }
}

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract utility class for representations which encode {@link YangInstanceIdentifier} as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
@Beta
public abstract class AbstractStringInstanceIdentifierCodec extends AbstractNamespaceCodec
        implements InstanceIdentifierCodec<String> {

    @Override
    public final String serialize(final YangInstanceIdentifier data) {
        final StringBuilder sb = new StringBuilder();
        DataSchemaContextNode<?> current = getDataContextTree().getRoot();
        QNameModule lastModule = null;
        for (PathArgument arg : data.getPathArguments()) {
            current = current.getChild(arg);
            Preconditions.checkArgument(current != null,
                    "Invalid input %s: schema for argument %s (after %s) not found", data, arg, sb);

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

            final QName qname = arg.getNodeType();
            sb.append('/');
            appendQName(sb, qname, lastModule);
            lastModule = qname.getModule();

            if (arg instanceof NodeIdentifierWithPredicates) {
                for (Entry<QName, Object> entry : ((NodeIdentifierWithPredicates) arg).getKeyValues().entrySet()) {
                    sb.append('[');
                    appendQName(sb, entry.getKey(), lastModule);
                    sb.append("='");
                    sb.append(String.valueOf(entry.getValue()));
                    sb.append("']");
                }
            } else if (arg instanceof NodeWithValue) {
                sb.append("[.='");
                sb.append(((NodeWithValue<?>) arg).getValue());
                sb.append("']");
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
    protected abstract @Nonnull DataSchemaContextTree getDataContextTree();

    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        return value;
    }

    @Override
    public final YangInstanceIdentifier deserialize(final String data) {
        Preconditions.checkNotNull(data, "Data may not be null");
        XpathStringParsingPathArgumentBuilder builder = new XpathStringParsingPathArgumentBuilder(this, data);
        return YangInstanceIdentifier.create(builder.build());
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

/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.net.URI;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

abstract class AbstractNamespaceCodec {
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    /**
     * Return string prefix for a particular namespace, allocating a new one if necessary.
     *
     * @param namespace Namespace to map
     * @return Allocated unique prefix, or null if the prefix cannot be mapped.
     */
    @Nullable protected abstract String prefixForNamespace(@Nonnull URI namespace);

    /**
     * Create a QName for a prefix and local name.
     *
     * @param prefix Prefix for namespace
     * @param localName local name
     * @return QName
     * @throws IllegalArgumentException if the prefix cannot be resolved
     */
    @Nullable protected abstract QName createQName(@Nonnull String prefix, @Nonnull String localName);

    private static String getIdAndPrefixAsStr(final String pathPart) {
        int predicateStartIndex = pathPart.indexOf('[');
        return predicateStartIndex == -1 ? pathPart : pathPart.substring(0, predicateStartIndex);
    }

    protected final StringBuilder appendQName(final StringBuilder sb, final QName qname) {
        final String prefix = prefixForNamespace(qname.getNamespace());
        Preconditions.checkArgument(prefix != null, "Failed to map QName {}", qname);
        sb.append(prefix);
        sb.append(':');
        sb.append(qname.getLocalName());
        return sb;
    }

    protected final QName parseQName(final String str) {
        final String xPathPartTrimmed = getIdAndPrefixAsStr(str).trim();
        final Iterator<String> it = COLON_SPLITTER.split(xPathPartTrimmed).iterator();

        // Empty string
        if (!it.hasNext()) {
            return null;
        }


        final String first = it.next().trim();
        if (first.isEmpty()) {
            return null;
        }

        final String identifier;
        final String prefix;
        if (it.hasNext()) {
            // It is "prefix:value"
            prefix = first;
            identifier = it.next().trim();
        } else {
            prefix = "";
            identifier = first;
        }
        if (identifier.isEmpty()) {
            return null;
        }

        return createQName(prefix, identifier);
    }
}

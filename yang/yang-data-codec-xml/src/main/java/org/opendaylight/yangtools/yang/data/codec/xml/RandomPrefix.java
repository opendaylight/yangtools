/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.net.URI;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

class RandomPrefix {
    // 32 characters, carefully chosen
    private static final String LOOKUP = "abcdefghiknoprstABCDEFGHIKNOPRST";
    private static final int MASK = 0x1f;
    private static final int SHIFT = 5;

    private int counter = 0;

    // BiMap to make values lookup faster
    private final BiMap<URI, String> prefixes = HashBiMap.create();
    private final NamespaceContext context;

    RandomPrefix(final NamespaceContext context) {
        this.context = context;
    }

    Iterable<Entry<URI, String>> getPrefixes() {
        return prefixes.entrySet();
    }

    String encodePrefix(final URI namespace) {
        String prefix = prefixes.get(namespace);
        if (prefix != null) {
            return prefix;
        }

        if (context != null) {
            prefix = context.getPrefix(namespace.toString());
            if (prefix != null) {
                return prefix;
            }
        }

        do {
            prefix = encode(counter);
            counter++;
        } while (alreadyUsedPrefix(prefix));

        prefixes.put(namespace, prefix);
        return prefix;
    }

    private boolean alreadyUsedPrefix(final String prefix) {
        if (context == null) {
            return false;
        }

        // It seems JDK8 is violating the API contract of NamespaceContext by returning null for unbound prefixes,
        // rather than specified NULL_NS_URI. Work this around by checking explicitly for null.
        final String str = context.getNamespaceURI(prefix);
        return str != null && !XMLConstants.NULL_NS_URI.equals(str);
    }

    @VisibleForTesting
    static int decode(final String str) {
        int ret = 0;
        for (char c : str.toCharArray()) {
            int idx = LOOKUP.indexOf(c);
            Preconditions.checkArgument(idx != -1, "Invalid string %s", str);
            ret = (ret << SHIFT) + idx;
        }

        return ret;
    }

    @VisibleForTesting
    static String encode(int num) {
        final StringBuilder sb = new StringBuilder();

        do {
            sb.append(LOOKUP.charAt(num & MASK));
            num >>>= SHIFT;
        } while (num != 0);

        return sb.reverse().toString();
    }
}

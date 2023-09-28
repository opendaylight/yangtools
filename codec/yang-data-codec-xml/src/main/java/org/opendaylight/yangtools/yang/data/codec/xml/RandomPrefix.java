/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

final class RandomPrefix {
    // 32 characters, carefully chosen
    private static final String LOOKUP = "abcdefghiknoprstABCDEFGHIKNOPRST";
    private static final int MASK = 0x1f;
    private static final int SHIFT = 5;

    private int counter = 0;

    // BiMap to make values lookup faster
    private final BiMap<XMLNamespace, String> emittedPrefixes = HashBiMap.create();
    private final @NonNull ModelContextPrefixes prefixes;
    private final NamespaceContext context;

    RandomPrefix(final ModelContextPrefixes prefixes, final NamespaceContext context) {
        this.prefixes = requireNonNull(prefixes);
        this.context = context;
    }

    NamespaceContext context() {
        return context;
    }

    Iterable<Entry<XMLNamespace, String>> emittedPrefixes() {
        return emittedPrefixes.entrySet();
    }

    String encodePrefix(final XMLNamespace namespace) {
        var prefix = emittedPrefixes.get(namespace);
        if (prefix != null) {
            return prefix;
        }

        if (context != null) {
            prefix = context.getPrefix(namespace.toString());
            if (prefix != null) {
                return prefix;
            }
        }

        prefix = prefixes.prefixForNamespace(namespace);
        if (prefix == null) {
            prefix = encode(counter++);
        }

        while (alreadyUsedPrefix(prefix)) {
            prefix = encode(counter++);
            counter++;
        }

        emittedPrefixes.put(namespace, prefix);
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
            checkArgument(idx != -1, "Invalid string %s", str);
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

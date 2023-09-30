/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Interface to prefix assignment based on a {@link NamespaceContext} and advice from {@link PreferredPrefixes}.
 */
final class NamespacePrefixes implements Mutable {
    // 32 characters, carefully chosen
    @VisibleForTesting
    static final String LOOKUP = "abcdefghiknoprstABCDEFGHIKNOPRST";
    @VisibleForTesting
    static final int SHIFT = 5;
    private static final int MASK = 0x1f;

    private int counter = 0;

    // BiMap to make values lookup faster
    private final BiMap<XMLNamespace, String> emittedPrefixes = HashBiMap.create();
    private final PreferredPrefixes pref;
    private final NamespaceContext context;

    NamespacePrefixes(final NamespaceContext context, final @Nullable PreferredPrefixes pref) {
        this.context = context;
        this.pref = pref;
    }

    List<Entry<XMLNamespace, String>> emittedPrefixes() {
        return emittedPrefixes.entrySet().stream()
            // Order by prefix
            .sorted(Comparator.comparing(Entry::getValue))
            .collect(Collectors.toList());
    }

    @NonNull String encodePrefix(final XMLNamespace namespace) {
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

        return createPrefix(namespace);

    }

    private @NonNull String createPrefix(final @NonNull XMLNamespace namespace) {
        if (pref != null) {
            final var prefix = pref.prefixForNamespace(namespace);
            if (prefix != null) {
                emittedPrefixes.put(namespace, prefix);
                return prefix;
            }
        }

        String prefix;
        do {
            prefix = encode(counter++);
        } while (alreadyUsedPrefix(prefix));

        emittedPrefixes.put(namespace, prefix);
        return prefix;
    }

    private boolean alreadyUsedPrefix(final String prefix) {
        if (pref != null && pref.isUsed(prefix)) {
            return true;
        }
        if (context == null) {
            return false;
        }

        // It seems JDK8 is violating the API contract of NamespaceContext by returning null for unbound prefixes,
        // rather than specified NULL_NS_URI. Work this around by checking explicitly for null.
        final var str = context.getNamespaceURI(prefix);
        return str != null && !XMLConstants.NULL_NS_URI.equals(str);
    }

    @VisibleForTesting
    static @NonNull String encode(int num) {
        final var sb = new StringBuilder();

        do {
            sb.append(LOOKUP.charAt(num & MASK));
            num >>>= SHIFT;
        } while (num != 0);

        return sb.reverse().toString();
    }
}

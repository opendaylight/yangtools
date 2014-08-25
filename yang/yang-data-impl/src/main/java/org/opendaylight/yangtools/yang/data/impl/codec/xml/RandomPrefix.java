/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.net.URI;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;

final class RandomPrefix {

    public static final char STARTING_CHAR = 'a';
    public static final int CHARACTER_RANGE = 26;
    public static final int PREFIX_MAX_LENGTH = 4;

    public static final int MAX_COUNTER_VALUE = (int) Math.pow(CHARACTER_RANGE, PREFIX_MAX_LENGTH);
    private static final int STARTING_WITH_XML = decode("xml");

    private int counter = 0;

    // BiMap to make values lookup faster
    private final BiMap<URI, String> prefixes = HashBiMap.create();

    Iterable<Map.Entry<URI, String>> getPrefixes() {
        return prefixes.entrySet();
    }

    String encodeQName(final QName qname) {
        return encodePrefix(qname) + ':' + qname.getLocalName();
    }

    String encodePrefix(final QName qname) {
        String prefix = prefixes.get(qname.getNamespace());
        if (prefix != null) {
            return prefix;
        }

        // Reuse prefix from QName if possible
        final String qNamePrefix = qname.getPrefix();

        if (!Strings.isNullOrEmpty(qNamePrefix) && !qNamePrefix.startsWith("xml") && !alreadyUsedPrefix(qNamePrefix)) {
            prefix = qNamePrefix;
        } else {

            do {
                // Skip values starting with xml (Expecting only 4 chars max since division is calculated only once)
                while (counter == STARTING_WITH_XML
                        || counter / CHARACTER_RANGE == STARTING_WITH_XML) {
                    counter++;
                }

                // Reset in case of max prefix generated
                if (counter >= MAX_COUNTER_VALUE) {
                    counter = 0;
                    prefixes.clear();
                }

                prefix = encode(counter);
                counter++;
            } while (alreadyUsedPrefix(prefix));
        }

        prefixes.put(qname.getNamespace(), prefix);
        return prefix;
    }

    private boolean alreadyUsedPrefix(final String prefix) {
        return prefixes.values().contains(prefix);
    }

    @VisibleForTesting
    static int decode(final String s) {
        int num = 0;
        for (final char ch : s.toCharArray()) {
            num *= CHARACTER_RANGE;
            num += (ch - STARTING_CHAR);
        }
        return num;
    }

    @VisibleForTesting
    static String encode(int num) {
        if (num == 0) {
            return "a";
        }

        final StringBuilder sb = new StringBuilder();
        while (num != 0) {
            sb.append(((char) (num % CHARACTER_RANGE + STARTING_CHAR)));
            num /= CHARACTER_RANGE;
        }

        return sb.reverse().toString();
    }
}

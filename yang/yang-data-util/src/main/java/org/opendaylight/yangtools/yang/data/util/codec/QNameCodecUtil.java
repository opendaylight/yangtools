/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import java.util.Iterator;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Utility methods for parsing and writing QNames.
 *
 * @author Robert Varga
 */
@Beta
public final class QNameCodecUtil {
    private static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults();

    public static QName decodeQName(final String str, final Function<String, QNameModule> prefixToModule) {
        final Iterator<String> it = COLON_SPLITTER.split(str).iterator();

        // Empty string
        final String identifier;
        final String prefix;
        if (it.hasNext()) {
            final String first = it.next();
            if (it.hasNext()) {
                // It is "prefix:value"
                prefix = first;
                identifier = it.next();
                checkArgument(!it.hasNext(), "Malformed QName '%s'", str);
            } else {
                prefix = "";
                identifier = first;
            }
        } else {
            prefix = "";
            identifier = "";
        }

        final QNameModule module = prefixToModule.apply(prefix);
        checkArgument(module != null, "Cannot resolve prefix '%s' from %s", prefix, str);
        return QName.create(module, identifier);
    }

    public static String encodeQName(final QName qname, final Function<QNameModule, String> moduleToPrefix) {
        final String prefix = moduleToPrefix.apply(qname.getModule());
        checkArgument(prefix != null, "Cannot allocated prefix for %s", qname);
        return prefix.isEmpty() ? qname.getLocalName() : prefix + ":" + qname.getLocalName();
    }
}

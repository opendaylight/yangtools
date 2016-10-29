/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

/**
 * Utility class for handling various naming conventions mentioned in YANG and related specifications.
 *
 * @author Robert Varga
 */
@Beta
public final class YangNames {
    private YangNames() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parse a file name according to rules outlined in https://tools.ietf.org/html/rfc6020#section-5.2. Input string
     * should be the base path with file extension stripped.
     *
     * @param baseName file base name
     * @return A tuple containing the module name and parsed revision, if present.
     */
    public static Entry<String, String> parseFilename(final String baseName) {
        final int zavinac = baseName.lastIndexOf('@');
        if (zavinac < 0) {
            return new SimpleEntry<>(baseName, null);
        }

        return new SimpleEntry<>(baseName.substring(0, zavinac), baseName.substring(zavinac + 1));
    }
}

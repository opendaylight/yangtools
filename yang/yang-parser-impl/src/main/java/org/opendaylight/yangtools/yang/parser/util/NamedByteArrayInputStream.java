/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NamedByteArrayInputStream extends ByteArrayInputStream implements NamedInputStream {
    private final String toString;
    public NamedByteArrayInputStream(byte[] buf, String toString) {
        super(buf);
        this.toString = toString;
    }

    public static ByteArrayInputStream create(InputStream originalIS) throws IOException {
        final byte[] data = ByteStreams.toByteArray(originalIS);

        if (originalIS instanceof NamedInputStream) {
            return new NamedByteArrayInputStream(data, originalIS.toString());
        } else {
            return new ByteArrayInputStream(data);
        }
    }

    @Override
    public String toString() {
        return toString;
    }
}

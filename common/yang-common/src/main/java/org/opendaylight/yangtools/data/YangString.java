/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A YANG {@code string} value.
 */
@NonNullByDefault
public abstract non-sealed class YangString implements Comparable<YangString>, ScalarValue {

    public byte[] toUtf8() {
        return utf8Bytes().clone();
    }

    @Override
    public String toString() {
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8Bytes())).toString();
    }

    protected abstract byte[] utf8Bytes();
}

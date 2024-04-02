/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1542Test {
    @Test
    void writeInstanceIdentifierReportsIOException() {
        final var codec = JSONCodecFactorySupplier.RFC7951.createSimple(YangParserTestUtils.parseYang())
            .instanceIdentifierCodec();
        final var ex = assertThrows(IOException.class, () -> codec.writeValue((JSONValueWriter) null,
            YangInstanceIdentifier.of(QName.create("foo", "bar"))));
        assertEquals("Failed to encode instance-identifier", ex.getMessage());
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }
}

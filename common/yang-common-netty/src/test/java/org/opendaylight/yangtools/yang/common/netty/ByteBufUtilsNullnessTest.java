/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.netty;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

class ByteBufUtilsNullnessTest {
    private final ByteBuf buf = Unpooled.buffer();

    @Test
    void testReadUint8() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.readUint8(null));
    }

    @Test
    void testReadUint16() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.readUint16(null));
    }

    @Test
    void testReadUint32() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.readUint8(null));
    }

    @Test
    void testReadUint64() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.readUint64(null));
    }

    @Test
    void testWriteNullBuf8() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(null, Uint8.ONE));
    }

    @Test
    void testWriteNullBuf16() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(null, Uint16.ONE));
    }

    @Test
    void testWriteNullBuf32() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(null, Uint32.ONE));
    }

    @Test
    void testWriteNullBuf64() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(null, Uint64.ONE));
    }

    @Test
    void testWriteNull8() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(buf, (Uint8) null));
    }

    @Test
    void testWriteNull16() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(buf, (Uint16) null));
    }

    @Test
    void testWriteNull32() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(buf, (Uint32) null));
    }

    @Test
    void testWriteNull64() {
        assertThrows(NullPointerException.class, () -> ByteBufUtils.write(null, (Uint64) null));
    }

    @Test
    void testWriteMandatoryByte() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Byte) null, "name"));
    }

    @Test
    void testWriteMandatoryShort() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Short) null, "name"));
    }

    @Test
    void testWriteMandatoryInt() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Integer) null, "name"));
    }

    @Test
    void testWriteMandatoryLong() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Long) null, "name"));
    }

    @Test
    void testWriteMandatoryUint8() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Uint8) null, "name"));
    }

    @Test
    void testWriteMandatoryUint16() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Uint16) null, "name"));
    }

    @Test
    void testWriteMandatoryUint32() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Uint32) null, "name"));
    }

    @Test
    void testWriteMandatoryUint64() {
        assertThrows(IllegalArgumentException.class, () -> ByteBufUtils.writeMandatory(buf, (Uint64) null, "name"));
    }
}

/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class ByteBufUtilsNullnessTest {
    private ByteBuf buf;

    @Before
    public void before() {
        buf = Unpooled.buffer();
    }

    @Test(expected = NullPointerException.class)
    public void testReadUint8() {
        ByteBufUtils.readUint8(null);
    }

    @Test(expected = NullPointerException.class)
    public void testReadUint16() {
        ByteBufUtils.readUint16(null);
    }

    @Test(expected = NullPointerException.class)
    public void testReadUint32() {
        ByteBufUtils.readUint8(null);
    }

    @Test(expected = NullPointerException.class)
    public void testReadUint64() {
        ByteBufUtils.readUint64(null);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNullBuf8() {
        ByteBufUtils.write(null, Uint8.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNullBuf16() {
        ByteBufUtils.write(null, Uint16.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNullBuf32() {
        ByteBufUtils.write(null, Uint32.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNullBuf64() {
        ByteBufUtils.write(null, Uint64.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull8() {
        ByteBufUtils.write(buf, (Uint8) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull16() {
        ByteBufUtils.write(buf, (Uint16) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull32() {
        ByteBufUtils.write(buf, (Uint32) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull64() {
        ByteBufUtils.write(null, (Uint64) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryByte() {
        ByteBufUtils.writeMandatory(buf, (Byte) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryShort() {
        ByteBufUtils.writeMandatory(buf, (Short) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryInt() {
        ByteBufUtils.writeMandatory(buf, (Integer) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryLong() {
        ByteBufUtils.writeMandatory(buf, (Long) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryUint8() {
        ByteBufUtils.writeMandatory(buf, (Uint8) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryUint16() {
        ByteBufUtils.writeMandatory(buf, (Uint16) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryUint32() {
        ByteBufUtils.writeMandatory(buf, (Uint32) null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteMandatoryUint64() {
        ByteBufUtils.writeMandatory(buf, (Uint64) null, "name");
    }


}

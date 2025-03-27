/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

class ByteBufUtilsTest {
    @Test
    void testWriteByte() {
        test(Byte.MAX_VALUE);
        test(Byte.MIN_VALUE);
    }

    @Test
    void testWriteShort() {
        test(Short.MAX_VALUE);
        test(Short.MIN_VALUE);
    }

    @Test
    void testWriteInt() {
        test(Integer.MAX_VALUE);
        test(Integer.MIN_VALUE);
    }

    @Test
    void testWriteLong() {
        test(Long.MAX_VALUE);
        test(Long.MIN_VALUE);
    }

    @Test
    void testWrite8() {
        testUint(Uint8.ONE);
        testUint(Uint8.TWO);
        testUint(Uint8.TEN);
        testUint(Uint8.MAX_VALUE);
    }

    @Test
    void testWrite16() {
        testUint(Uint16.ONE);
        testUint(Uint16.TWO);
        testUint(Uint16.TEN);
        testUint(Uint16.MAX_VALUE);
    }

    @Test
    void testWrite32() {
        testUint(Uint32.ONE);
        testUint(Uint32.TWO);
        testUint(Uint32.TEN);
        testUint(Uint32.MAX_VALUE);
    }

    @Test
    void testWrite64() {
        testUint(Uint64.ONE);
        testUint(Uint64.TWO);
        testUint(Uint64.TEN);
        testUint(Uint64.MAX_VALUE);
    }

    @Test
    void testWriteOptionalByte() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Byte) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Byte.MAX_VALUE);
        assertEquals(1, buf.readableBytes());
    }

    @Test
    void testWriteOptionalShort() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Short) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Short.MAX_VALUE);
        assertEquals(2, buf.readableBytes());
    }

    @Test
    void testWriteOptionalInt() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Integer) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Integer.MAX_VALUE);
        assertEquals(4, buf.readableBytes());
    }

    @Test
    void testWriteOptionalLong() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Long) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Long.MAX_VALUE);
        assertEquals(8, buf.readableBytes());
    }

    @Test
    void testWriteOptional8() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Uint8) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Uint8.MAX_VALUE);
        assertUint(buf, Uint8.MAX_VALUE);
    }

    @Test
    void testWriteOptional16() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Uint16) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Uint16.MAX_VALUE);
        assertUint(buf, Uint16.MAX_VALUE);
    }

    @Test
    void testWriteOptional32() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Uint32) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Uint32.MAX_VALUE);
        assertUint(buf, Uint32.MAX_VALUE);
    }

    @Test
    void testWriteOptional64() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOptional(buf, (Uint64) null);
        assertEquals(0, buf.readableBytes());

        ByteBufUtils.writeOptional(buf, Uint64.MAX_VALUE);
        assertUint(buf, Uint64.MAX_VALUE);
    }

    @Test
    void testWriteZeroByte() {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Byte) null);
        assertByte(buf, 0);

        ByteBufUtils.writeOrZero(buf, Byte.MAX_VALUE);
        assertByte(buf, Byte.MAX_VALUE);
    }

    @Test
    void testWriteZeroShort() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Short) null);
        assertShort(buf, 0);

        ByteBufUtils.writeOrZero(buf, Short.MAX_VALUE);
        assertShort(buf, Short.MAX_VALUE);
    }

    @Test
    void testWriteZeroInt() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Integer) null);
        assertInt(buf, 0);

        ByteBufUtils.writeOrZero(buf, Integer.MAX_VALUE);
        assertInt(buf, Integer.MAX_VALUE);
    }

    @Test
    void testWriteZeroLong() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Long) null);
        assertLong(buf, 0);

        ByteBufUtils.writeOrZero(buf, Long.MAX_VALUE);
        assertLong(buf, Long.MAX_VALUE);
    }

    @Test
    void testWriteZero8() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Uint8) null);
        assertUint(buf, Uint8.ZERO);

        ByteBufUtils.writeOrZero(buf, Uint8.MAX_VALUE);
        assertUint(buf, Uint8.MAX_VALUE);
    }

    @Test
    void testWriteZero16() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Uint16) null);
        assertUint(buf, Uint16.ZERO);

        ByteBufUtils.writeOrZero(buf, Uint16.MAX_VALUE);
        assertUint(buf, Uint16.MAX_VALUE);
    }

    @Test
    void testWriteZero32() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Uint32) null);
        assertUint(buf, Uint32.ZERO);

        ByteBufUtils.writeOrZero(buf, Uint32.MAX_VALUE);
        assertUint(buf, Uint32.MAX_VALUE);
    }

    @Test
    void testWriteZero64() {
        final var buf = Unpooled.buffer();
        ByteBufUtils.writeOrZero(buf, (Uint64) null);
        assertUint(buf, Uint64.ZERO);

        ByteBufUtils.writeOrZero(buf, Uint64.MAX_VALUE);
        assertUint(buf, Uint64.MAX_VALUE);
    }

    private static void test(final Byte value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertByte(buf, value);
    }

    private static void test(final Short value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertShort(buf, value);
    }

    private static void test(final Integer value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertInt(buf, value);
    }

    private static void test(final Long value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertLong(buf, value);
    }

    private static void testUint(final Uint8 value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertUint(buf, value);
    }

    private static void testUint(final Uint16 value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertUint(buf, value);
    }

    private static void testUint(final Uint32 value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertUint(buf, value);
    }

    private static void testUint(final Uint64 value) {
        final ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeMandatory(buf, value, "foo");
        assertUint(buf, value);
    }

    private static void assertByte(final ByteBuf buf, final int value) {
        assertEquals(1, buf.readableBytes());
        assertEquals(value, buf.readByte());
    }

    private static void assertShort(final ByteBuf buf, final int value) {
        assertEquals(2, buf.readableBytes());
        assertEquals(value, buf.readShort());
    }

    private static void assertInt(final ByteBuf buf, final int value) {
        assertEquals(4, buf.readableBytes());
        assertEquals(value, buf.readInt());
    }

    private static void assertLong(final ByteBuf buf, final long value) {
        assertEquals(8, buf.readableBytes());
        assertEquals(value, buf.readLong());
    }

    private static void assertUint(final ByteBuf buf, final Uint8 value) {
        assertEquals(1, buf.readableBytes());
        assertEquals(value, ByteBufUtils.readUint8(buf));
    }

    private static void assertUint(final ByteBuf buf, final Uint16 value) {
        assertEquals(2, buf.readableBytes());
        assertEquals(value, ByteBufUtils.readUint16(buf));
    }

    private static void assertUint(final ByteBuf buf, final Uint32 value) {
        assertEquals(4, buf.readableBytes());
        assertEquals(value, ByteBufUtils.readUint32(buf));
    }

    private static void assertUint(final ByteBuf buf, final Uint64 value) {
        assertEquals(8, buf.readableBytes());
        assertEquals(value, ByteBufUtils.readUint64(buf));
    }
}

/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;

import org.junit.Before;
import org.junit.Test;

public class ByteArrayTest {

	byte[] before = new byte[] { 15, 28, 4, 6, 9, 10 };

	@Test
	public void testBytesToFloat() {
		final float expected = 8581;
		final byte[] b = ByteArray.floatToBytes(expected);
		assertEquals(expected, ByteArray.bytesToFloat(b), 50);
	}

	@Test
	public void testSubByte() {
		byte[] after = ByteArray.subByte(this.before, 0, 3);
		byte[] expected = new byte[] { 15, 28, 4 };
		assertArrayEquals(expected, after);
		after = ByteArray.subByte(this.before, 5, 1);
		expected = new byte[] { 10 };
		assertArrayEquals(expected, after);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte2() {
		ByteArray.subByte(new byte[0], 2, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte3() {
		ByteArray.subByte(this.before, 2, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte4() {
		ByteArray.subByte(this.before, -1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte5() {
		ByteArray.subByte(this.before, 9, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte6() {
		ByteArray.subByte(this.before, 2, 19);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubByte7() {
		ByteArray.subByte(this.before, 2, 7);
	}

	@Test
	public void testCutBytes() {
		byte[] after = ByteArray.cutBytes(this.before, 2);
		byte[] expected = new byte[] { 4, 6, 9, 10 };
		assertArrayEquals(expected, after);
		after = ByteArray.cutBytes(this.before, 6);
		expected = new byte[] {};
		assertArrayEquals(expected, after);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCutBytes2() {
		ByteArray.cutBytes(new byte[0], 5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCutBytes3() {
		ByteArray.cutBytes(this.before, 9);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCutBytes4() {
		ByteArray.cutBytes(this.before, 0);
	}

	@Test
	public void testParseBits() {
		final byte b = (byte) -76; // 1011 0100
		final boolean[] after = ByteArray.parseBits(b);
		assertTrue(after[0]);
		assertFalse(after[1]);
		assertTrue(after[2]);
		assertTrue(after[3]);
		assertFalse(after[4]);
		assertTrue(after[5]);
		assertFalse(after[6]);
		assertFalse(after[7]);
	}

	private final byte[] inBytes = { (byte) 0x03, (byte) 0xFF, (byte) 0x01, (byte) 0x80 };
	BitSet inBitSet = new BitSet();

	@Before
	public void generateBitSet() {
		// 0x03
		this.inBitSet.set(6, 8);

		// 0xFF
		this.inBitSet.set(8, 16);

		// 0x01
		this.inBitSet.set(23);

		// 0x80
		this.inBitSet.set(24);
	}

	@Test
	public void testBytesToBitSetFunction() {
		final BitSet iputBitSet = ByteArray.bytesToBitSet(this.inBytes);

		assertEquals(this.inBitSet, iputBitSet);
	}

	@Test
	public void testBitSetToBytesFunction() {
		byte[] resultBytes = ByteArray.bitSetToBytes(this.inBitSet, this.inBytes.length);
		assertArrayEquals(this.inBytes, resultBytes);

		resultBytes = ByteArray.bitSetToBytes(this.inBitSet, this.inBytes.length - 1);
		assertArrayEquals(Arrays.copyOf(this.inBytes, this.inBytes.length - 1), resultBytes);

		resultBytes = ByteArray.bitSetToBytes(this.inBitSet, this.inBytes.length + 1);
		assertArrayEquals(Arrays.copyOf(this.inBytes, this.inBytes.length + 1), resultBytes);
	}

	@Test
	public void testFileToBytes() throws IOException {
		final String FILE_TO_TEST = "src/test/resources/PCEStatefulCapabilityTlv1.bin";

		final File fileToCompareWith = new File(FILE_TO_TEST);
		final InputStream bytesIStream = new FileInputStream(fileToCompareWith);

		try {
			final byte[] actualBytes = ByteArray.fileToBytes(FILE_TO_TEST);

			if (fileToCompareWith.length() > Integer.MAX_VALUE) {
				throw new IOException("Too large file to load in byte array.");
			}

			final byte[] expectedBytes = new byte[(int) fileToCompareWith.length()];

			int offset = 0;
			int numRead = 0;
			while (offset < expectedBytes.length && (numRead = bytesIStream.read(expectedBytes, offset, actualBytes.length - offset)) >= 0) {
				offset += numRead;
			}

			assertArrayEquals(expectedBytes, actualBytes);
		} finally {
			bytesIStream.close();
		}
	}

	@Test
	public void testIntToBytes() {
		assertEquals(Integer.MAX_VALUE, ByteArray.bytesToInt(ByteArray.intToBytes(Integer.MAX_VALUE, Integer.SIZE / Byte.SIZE)));
		assertEquals(Integer.MIN_VALUE, ByteArray.bytesToInt(ByteArray.intToBytes(Integer.MIN_VALUE, Integer.SIZE / Byte.SIZE)));
		assertEquals(2, ByteArray.intToBytes(12, 2).length);
		assertArrayEquals(new byte[] { 0, 12 }, ByteArray.intToBytes(12, 2));
		assertEquals(5, ByteArray.bytesToInt(ByteArray.intToBytes(5, 2)));
	}

	@Test
	public void testLongToBytes_bytesToLong() {
		assertEquals(Long.MAX_VALUE, ByteArray.bytesToLong(ByteArray.longToBytes(Long.MAX_VALUE, Long.SIZE / Byte.SIZE)));
		assertEquals(Long.MIN_VALUE, ByteArray.bytesToLong(ByteArray.longToBytes(Long.MIN_VALUE, Long.SIZE / Byte.SIZE)));
		assertArrayEquals(new byte[] { 0, 0, 5 }, ByteArray.longToBytes(5L, 3));
		assertEquals(5, ByteArray.bytesToLong(ByteArray.longToBytes(5, 2)));
	}

	/**
	 * if less than 4 bytes are converted, zero bytes should be appendet at the buffer's start
	 */
	@Test
	public void testBytesToLong_prependingZeros() {
		assertEquals(1, ByteArray.bytesToLong(new byte[] { 0, 0, 1 }));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBytesToInt() {
		final byte[] b = new byte[Integer.SIZE + 1];
		ByteArray.bytesToInt(b);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBytesToShort2() {
		final byte[] b = new byte[Short.SIZE + 1];
		ByteArray.bytesToInt(b);
	}

	@Test
	public void testBytes() {
		assertTrue(ByteArray.bytesToInt(new byte[] { 0, 0, 0, 15 }) == 15);
		assertEquals(Float.valueOf((float) 1.4E-45), Float.valueOf(ByteArray.bytesToFloat(new byte[] { 0, 0, 0, 1 })));
		assertEquals(Long.valueOf(16613001005322L), Long.valueOf(ByteArray.bytesToLong(this.before)));
		assertEquals(Short.valueOf((short) 1), Short.valueOf(ByteArray.bytesToShort(new byte[] { 0, 1 })));
	}

	@Test
	public void testCopyBitRange() {
		assertEquals((byte) 10, ByteArray.copyBitsRange((byte) 0x28, 2, 4));
		assertEquals((byte) 3, ByteArray.copyBitsRange((byte) 0xFF, 2, 2));
		assertEquals((byte) 7, ByteArray.copyBitsRange((byte) 0xFF, 5, 3));
		assertEquals((byte) 15, ByteArray.copyBitsRange((byte) 0xFF, 0, 4));
		assertEquals((byte) 31, ByteArray.copyBitsRange((byte) 0xF9, 0, 5));
		assertEquals((byte) 0xA2, ByteArray.copyBitsRange((byte) 0xA2, 0, 8));
		assertEquals((byte) 1, ByteArray.copyBitsRange((byte) 0xFF, 5, 1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyBitsRange2() {
		ByteArray.copyBitsRange((byte) 0x28, -1, 4);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyBitsRange3() {
		ByteArray.copyBitsRange((byte) 0x28, 1, 187);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyBitsRange4() {
		ByteArray.copyBitsRange((byte) 0x28, 1, 40);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyBitsRange5() {
		ByteArray.copyBitsRange((byte) 0x28, 28, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCopyBitsRange6() {
		ByteArray.copyBitsRange((byte) 0x28, 2, -2);
	}

	@Test
	public void testCopyWhole() {
		final byte[] expecteds = { (byte) 0x04, (byte) 0x02, (byte) 0xD4, (byte) 0xf5, (byte) 0x32 };

		final byte[] actuals = new byte[5];
		actuals[0] = (byte) 0x04;
		actuals[1] = (byte) 0x02;
		actuals[2] = (byte) 0xD4;

		final byte[] src = { (byte) 0xf5, (byte) 0x32 };

		ByteArray.copyWhole(src, actuals, 3);

		assertArrayEquals(expecteds, actuals);
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testCopyWhole2() {
		ByteArray.copyWhole(new byte[0], new byte[1], 2);
	}

	@Test
	public void testBytesToShort() {
		final byte[] bytes1 = { (byte) 0x00, (byte) 0x01 };
		final short expectedShort1 = 1;
		assertEquals(expectedShort1, ByteArray.bytesToShort(bytes1));

		final byte[] bytes2 = { (byte) 0xFF, (byte) 0xFF };
		final short expectedShort2 = (short) 0xFFFF;
		assertEquals(expectedShort2, ByteArray.bytesToShort(bytes2));

		final byte[] bytes3 = { (byte) 0x25, (byte) 0x34 };
		final short expectedShort3 = (short) 0x2534;
		assertEquals(expectedShort3, ByteArray.bytesToShort(bytes3));
	}

	@Test
	public void testShortToBytes() {
		final byte[] expectedBytes1 = { (byte) 0x00, (byte) 0x01 };
		assertArrayEquals(expectedBytes1, ByteArray.shortToBytes((short) 1));

		final byte[] expectedBytes2 = { (byte) 0xFF, (byte) 0xFF };
		assertArrayEquals(expectedBytes2, ByteArray.shortToBytes((short) 0xFFFF));

		final byte[] expectedBytes3 = { (byte) 0x25, (byte) 0x34 };
		assertArrayEquals(expectedBytes3, ByteArray.shortToBytes((short) 0x2534));
	}

	@Test
	public void testFloatToBytes() {
		final byte[] expectedBytes1 = { (byte) 0x35, (byte) 0x86, (byte) 0x37, (byte) 0xbd };
		assertArrayEquals(expectedBytes1, ByteArray.floatToBytes((float) 0.000001));

		final byte[] expectedBytes2 = { (byte) 0xEF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
		assertArrayEquals(expectedBytes2, ByteArray.floatToBytes((float) -158456315583795709447797473280.0));

		final byte[] expectedBytes3 = { (byte) 0x49, (byte) 0xbf, (byte) 0x1c, (byte) 0x92 };
		assertArrayEquals(expectedBytes3, ByteArray.floatToBytes((float) 1565586.253637));
	}

	@Test
	public void testBytesToHexString() {
		final byte[] b = new byte[] { 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01, 0x16, 0x01,
				0x16, };
		final String expected = "01 16 01 16 01 16 01 16  01 16 01 16 01 16 01 16\n01 16 ";
		assertEquals(expected, ByteArray.bytesToHexString(b));
	}

	@Test
	public void testBytesToHRString() {
		byte[] b;

		// test valid US-ASCII string
		b = new byte[] { (byte) 79, (byte) 102, (byte) 45, (byte) 57, (byte) 107, (byte) 45, (byte) 48, (byte) 50 };
		final String expected = "Of-9k-02";
		assertEquals(expected, ByteArray.bytesToHRString(b));

		// test Utf-8 restricted bytes
		b = new byte[] { (byte) 246, (byte) 248, (byte) 254 };
		assertEquals(Arrays.toString(b), ByteArray.bytesToHRString(b));

		// test unexpected continuation bytes
		b = new byte[] { (byte) 128, (byte) 113, (byte) 98 };
		assertEquals(Arrays.toString(b), ByteArray.bytesToHRString(b));
	}

	@Test
	public void testFindByteSequence() {
		final byte[] bytes = new byte[] { (byte) 36, (byte) 41, (byte) 55, (byte) 101, (byte) 38 };
		final byte[] sequence1 = new byte[] { (byte) 36, (byte) 41 };

		assertEquals(0, ByteArray.findByteSequence(bytes, sequence1));

		final byte[] sequence2 = new byte[] { (byte) 55, (byte) 38 };

		assertEquals(-1, ByteArray.findByteSequence(bytes, sequence2));

		final byte[] sequence3 = new byte[] { (byte) 101, (byte) 38 };

		assertEquals(3, ByteArray.findByteSequence(bytes, sequence3));

		try {
			ByteArray.findByteSequence(bytes, new byte[] { (byte) 36, (byte) 41, (byte) 55, (byte) 101, (byte) 38, (byte) 66 });
		} catch (final IllegalArgumentException e) {
			assertEquals("Sequence to be found is longer than the given byte array.", e.getMessage());
		}
	}

	@Test
	public void testMaskBytes() {
		final byte[] bytes = new byte[] { (byte) 0xAC, (byte) 0xA8, (byte) 0x1F, (byte) 0x08 };
		try {
			ByteArray.maskBytes(bytes, 48);
		} catch (final IllegalArgumentException e) {
			assertEquals("Attempted to apply invalid mask (too long)", e.getMessage());
		}

		assertArrayEquals(bytes, ByteArray.maskBytes(bytes, 32));

		assertArrayEquals(new byte[] { (byte) 0xAC, (byte) 0x80, 0, 0 }, ByteArray.maskBytes(bytes, 10));
	}

}

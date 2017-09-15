/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility methods for working with {@link WritableObject}s.
 *
 * @author Robert Varga
 */
@Beta
public final class WritableObjects {
    private WritableObjects() {
        throw new UnsupportedOperationException();
    }

    /**
     * Shorthand for {@link #writeLong(DataOutput, long, int)} with zero flags.
     *
     * @param out Data output
     * @param value long value to write
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if output is null
     */
    public static void writeLong(final DataOutput out, final long value) throws IOException {
        writeLong(out, value, 0);
    }

    /**
     * Write a long value into a {@link DataOutput}, compressing potential zero bytes. This method is useful for
     * serializing counters and similar, which have a wide range, but typically do not use it. The value provided is
     * treated as unsigned.
     *
     * <p>This methods writes the number of trailing non-zero in the value. It then writes the minimum required bytes
     * to reconstruct the value by left-padding zeroes. Inverse operation is performed by {@link #readLong(DataInput)}
     * or a combination of {@link #readLongHeader(DataInput)} and {@link #readLongBody(DataInput, byte)}.
     *
     * <p>Additionally the caller can use the top four bits (i.e. 0xF0) for caller-specific flags. These will be
     * ignored by {@link #readLong(DataInput)}, but can be extracted via {@link #readLongHeader(DataInput)}.
     *
     * @param out Data output
     * @param value long value to write
     * @param flags flags to store
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if output is null
     */
    public static void writeLong(final DataOutput out, final long value, final int flags) throws IOException {
        Preconditions.checkArgument((flags & 0xFFFFFF0F) == 0, "Invalid flags %s", flags);
        final int bytes = valueBytes(value);
        out.writeByte(bytes | flags);
        writeValue(out, value, bytes);
    }

    /**
     * Read a long value from a {@link DataInput} which was previously written via {@link #writeLong(DataOutput, long)}.
     *
     * @param in Data input
     * @return long value extracted from the data input
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if input is null
     */
    public static long readLong(final @NonNull DataInput in) throws IOException {
        return readLongBody(in, readLongHeader(in));
    }

    /**
     * Read the header of a compressed long value. The header may contain user-defined flags, which can be extracted
     * via {@link #longHeaderFlags(byte)}.
     *
     * @param in Data input
     * @return Header of next value
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if input is null
     */
    public static byte readLongHeader(final @NonNull DataInput in) throws IOException {
        return in.readByte();
    }

    /**
     * Extract user-defined flags from a compressed long header. This will return 0 if the long value originates from
     * {@link #writeLong(DataOutput, long)}.
     *
     * @param header Value header, as returned by {@link #readLongHeader(DataInput)}
     * @return User-defined flags
     */
    public static int longHeaderFlags(final byte header) {
        return header & 0xF0;
    }

    /**
     * Read a long value from a {@link DataInput} as hinted by the result of {@link #readLongHeader(DataInput)}.
     *
     * @param in Data input
     * @param header Value header, as returned by {@link #readLongHeader(DataInput)}
     * @return long value
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if input is null
     */
    public static long readLongBody(final @NonNull DataInput in, final byte header) throws IOException {
        int bytes = header & 0xF;
        if (bytes >= 8) {
            return in.readLong();
        }

        if (bytes <= 0) {
            return 0;
        }

        long value = 0;
        if (bytes >= 4) {
            bytes -= 4;
            value = (in.readInt() & 0xFFFFFFFFL) << bytes * Byte.SIZE;
        }
        if (bytes >= 2) {
            bytes -= 2;
            value |= in.readUnsignedShort() << bytes * Byte.SIZE;
        }
        if (bytes > 0) {
            value |= in.readUnsignedByte();
        }
        return value;
    }

    /**
     * Write two consecutive long values. These values can be read back using {@link #readLongHeader(DataInput)},
     * {@link #readFirstLong(DataInput, byte)} and {@link #readSecondLong(DataInput, byte)}.
     *
     * <p>This is a more efficient way of serializing two longs than {@link #writeLong(DataOutput, long)}. This is
     * achieved by using the flags field to hold the length of the second long -- hence saving one byte.
     *
     * @param out Data output
     * @param value0 first long value to write
     * @param value1 second long value to write
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if output is null
     */
    public static void writeLongs(final @NonNull DataOutput out, final long value0, final long value1)
            throws IOException {
        final int clen = WritableObjects.valueBytes(value1);
        writeLong(out, value0, clen << 4);
        WritableObjects.writeValue(out, value1, clen);
    }

    /**
     * Read first long value from an input.
     *
     * @param in Data input
     * @param header Value header, as returned by {@link #readLongHeader(DataInput)}
     * @return First long specified in {@link #writeLongs(DataOutput, long, long)}
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if input is null
     */
    public static long readFirstLong(final @NonNull DataInput in, final byte header) throws IOException {
        return WritableObjects.readLongBody(in, header);
    }

    /**
     * Read second long value from an input.
     *
     * @param in Data input
     * @param header Value header, as returned by {@link #readLongHeader(DataInput)}
     * @return Second long specified in {@link #writeLongs(DataOutput, long, long)}
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if input is null
     */
    public static long readSecondLong(final @NonNull DataInput in, final byte header) throws IOException {
        return WritableObjects.readLongBody(in, (byte)(header >>> 4));
    }

    private static void writeValue(final DataOutput out, final long value, final int bytes) throws IOException {
        if (bytes < 8) {
            int left = bytes;
            if (left >= 4) {
                left -= 4;
                out.writeInt((int)(value >>> left * Byte.SIZE));
            }
            if (left >= 2) {
                left -= 2;
                out.writeShort((int)(value >>> left * Byte.SIZE));
            }
            if (left > 0) {
                out.writeByte((int)(value & 0xFF));
            }
        } else {
            out.writeLong(value);
        }
    }

    private static int valueBytes(final long value) {
        // This is a binary search for the first match. Note that we need to mask bits from the most significant one.
        // It completes completes in three to four mask-and-compare operations.
        if ((value & 0xFFFFFFFF00000000L) != 0) {
            if ((value & 0xFFFF000000000000L) != 0) {
                return (value & 0xFF00000000000000L) != 0 ? 8 : 7;
            }
            return (value & 0x0000FF0000000000L) != 0 ? 6 : 5;
        } else if ((value & 0x00000000FFFFFFFFL) != 0) {
            if ((value & 0x00000000FFFF0000L) != 0) {
                return (value & 0x00000000FF000000L) != 0 ? 4 : 3;
            }
            return (value & 0x000000000000FF00L) != 0 ? 2 : 1;
        } else {
            return 0;
        }
    }
}

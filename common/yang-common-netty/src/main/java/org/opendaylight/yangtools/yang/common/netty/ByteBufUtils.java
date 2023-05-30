/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.netty;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Utility methods for interacting with {@link ByteBuf}s. These add a number of methods for reading and writing various
 * data types from/to ByteBufs. Methods fall into these categories:
 * <ul>
 *   <li>{@code readUint*}, which extract the corresponding amount of data from a buffer and return an Uint type. These
 *       are more efficient than going the {@code Uint8.valueOf(buf.readUnsignedByte())} route.</li>
 *   <li>{@code writeUint*}, which write specified value into a buffer.</li>
 *   <li>{@code writeMandatory*}, which write a property not statically known to be non-null. These methods throw
 *       an {@link IllegalArgumentException} if the supplied value is null. Otherwise they will write it to provided
 *       buffer.</li>
 *   <li>{@code writeOptional*}, which write a value which can legally be null. In case the value is not null, it is
 *       written to the provided buffer. If the value null, the method does nothing.</li>
 *   <li>{@code writeOrZero*}, which write a value which can legally be null. In case the value is not null, it is
 *       written to the provided buffer. If the value is null, a {code zero} value of corresponding width is written
 *       instead.</li>
 * </ul>
 */
public final class ByteBufUtils {
    private ByteBufUtils() {
        // Hidden on purpose
    }

    /**
     * Read an {@link Uint8} from specified buffer.
     *
     * @param buf buffer
     * @return A {@link Uint8}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code buf} does not have enough data
     */
    public static @NonNull Uint8 readUint8(final ByteBuf buf) {
        return Uint8.fromByteBits(buf.readByte());
    }

    /**
     * Read a {@link Uint16} from specified buffer.
     *
     * @param buf buffer
     * @return A {@link Uint16}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code buf} does not have enough data
     */
    public static @NonNull Uint16 readUint16(final ByteBuf buf) {
        return Uint16.fromShortBits(buf.readShort());
    }

    /**
     * Read a {@link Uint32} from specified buffer.
     *
     * @param buf buffer
     * @return A {@link Uint32}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code buf} does not have enough data
     */
    public static @NonNull Uint32 readUint32(final ByteBuf buf) {
        return Uint32.fromIntBits(buf.readInt());
    }

    /**
     * Read a {@link Uint64} from specified buffer.
     *
     * @param buf buffer
     * @return A {@link Uint64}
     * @throws NullPointerException if {@code buf} is null
     * @throws IndexOutOfBoundsException if {@code buf} does not have enough data
     */
    public static @NonNull Uint64 readUint64(final ByteBuf buf) {
        return Uint64.fromLongBits(buf.readLong());
    }

    /**
     * Write a {@link Uint8} to specified buffer.
     *
     * @param buf buffer
     * @param value A {@link Uint8}
     * @throws NullPointerException if any argument is null
     */
    public static void writeUint8(final ByteBuf buf, final Uint8 value) {
        buf.writeByte(value.byteValue());
    }

    /**
     * Write a {@link Uint16} to specified buffer.
     *
     * @param buf buffer
     * @param value A {@link Uint16}
     * @throws NullPointerException if any argument is null
     */
    public static void writeUint16(final ByteBuf buf, final Uint16 value) {
        buf.writeShort(value.shortValue());
    }

    /**
     * Write a {@link Uint32} to specified buffer.
     *
     * @param buf buffer
     * @param value A {@link Uint32}
     * @throws NullPointerException if any argument is null
     */
    public static void writeUint32(final ByteBuf buf, final Uint32 value) {
        buf.writeInt(value.intValue());
    }

    /**
     * Write a {@link Uint64} to specified buffer.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @throws NullPointerException if any argument is null
     */
    public static void writeUint64(final ByteBuf buf, final Uint64 value) {
        buf.writeLong(value.longValue());
    }

    /**
     * Write a {@link Uint8} to specified buffer. This method is provided for convenience, you may want to use
     * {@link #writeUint8(ByteBuf, Uint8)} as it is more explicit.
     *
     * @param buf buffer
     * @param value A {@link Uint8}
     * @throws NullPointerException if any argument is null
     */
    public static void write(final ByteBuf buf, final Uint8 value) {
        writeUint8(buf, value);
    }

    /**
     * Write a {@link Uint16} to specified buffer. This method is provided for convenience, you may want to use
     * {@link #writeUint16(ByteBuf, Uint16)} as it is more explicit.
     *
     * @param buf buffer
     * @param value A {@link Uint16}
     * @throws NullPointerException if any argument is null
     */
    public static void write(final ByteBuf buf, final Uint16 value) {
        writeUint16(buf, value);
    }

    /**
     * Write a {@link Uint32} to specified buffer. This method is provided for convenience, you may want to use
     * {@link #writeUint32(ByteBuf, Uint32)} as it is more explicit.
     *
     * @param buf buffer
     * @param value A {@link Uint32}
     * @throws NullPointerException if any argument is null
     */
    public static void write(final ByteBuf buf, final Uint32 value) {
        writeUint32(buf, value);
    }

    /**
     * Write a {@link Uint64} to specified buffer. This method is provided for convenience, you may want to use
     * {@link #writeUint64(ByteBuf, Uint64)} as it is more explicit.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @throws NullPointerException if any argument is null
     */
    public static void write(final ByteBuf buf, final Uint64 value) {
        writeUint64(buf, value);
    }

    /**
     * Write a {@link Byte} property to specified buffer. If the {@code value} is known to be non-null, prefer
     * {@link ByteBuf#writeByte(int)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Byte}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Byte value, final String name) {
        buf.writeByte(nonNullArgument(value, name).byteValue());
    }

    /**
     * Write a {@link Short} property to specified buffer. If the {@code value} is known to be non-null, prefer
     * {@link ByteBuf#writeShort(int)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Short}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Short value, final String name) {
        buf.writeShort(nonNullArgument(value, name).shortValue());
    }

    /**
     * Write a {@link Integer} property to specified buffer. If the {@code value} is known to be non-null, prefer
     * {@link ByteBuf#writeInt(int)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Integer}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Integer value, final String name) {
        buf.writeInt(nonNullArgument(value, name));
    }

    /**
     * Write a {@link Long} property to specified buffer. If the {@code value} is known to be non-null, prefer
     * {@link ByteBuf#writeLong(long)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Long}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Long value, final String name) {
        buf.writeLong(nonNullArgument(value, name));
    }

    /**
     * Write a {@link Uint8} property to specified buffer. If the {@code value} is known to be non-null, prefer to use
     * {@link #write(ByteBuf, Uint8)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Uint8}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Uint8 value, final String name) {
        write(buf, nonNullArgument(value, name));
    }

    /**
     * Write a {@link Uint16} property to specified buffer. If the {@code value} is known to be non-null, prefer to use
     * {@link #write(ByteBuf, Uint16)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Uint16}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Uint16 value, final String name) {
        write(buf, nonNullArgument(value, name));
    }

    /**
     * Write a {@link Uint32} property to specified buffer. If the {@code value} is known to be non-null, prefer to use
     * {@link #write(ByteBuf, Uint32)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Uint32}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Uint32 value, final String name) {
        write(buf, nonNullArgument(value, name));
    }

    /**
     * Write a {@link Uint64} property to specified buffer. If the {@code value} is known to be non-null, prefer to use
     * {@link #write(ByteBuf, Uint64)} instead of this method.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @param name Property name for error reporting purposes
     * @throws NullPointerException if {@code buf} is null
     * @throws IllegalArgumentException if {@code value} is null
     */
    public static void writeMandatory(final ByteBuf buf, final Uint64 value, final String name) {
        write(buf, nonNullArgument(value, name));
    }

    /**
     * Write a {@link Byte} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Byte}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Byte value) {
        if (value != null) {
            buf.writeByte(value.byteValue());
        }
    }

    /**
     * Write a {@link Byte} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Short}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Short value) {
        if (value != null) {
            buf.writeShort(value.shortValue());
        }
    }

    /**
     * Write a {@link Integer} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Integer}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Integer value) {
        if (value != null) {
            buf.writeInt(value);
        }
    }

    /**
     * Write a {@link Long} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Long}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Long value) {
        if (value != null) {
            buf.writeLong(value);
        }
    }

    /**
     * Write a {@link Uint8} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Uint8}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Uint8 value) {
        if (value != null) {
            write(buf, value);
        }
    }

    /**
     * Write a {@link Uint16} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Uint16}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Uint16 value) {
        if (value != null) {
            write(buf, value);
        }
    }

    /**
     * Write a {@link Uint32} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Uint32}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Uint32 value) {
        if (value != null) {
            write(buf, value);
        }
    }

    /**
     * Write a {@link Uint64} value to specified buffer if it is not null.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOptional(final ByteBuf buf, final @Nullable Uint64 value) {
        if (value != null) {
            write(buf, value);
        }
    }

    /**
     * Write a {@link Byte} value to specified buffer if it is not null, otherwise write one zero byte.
     *
     * @param buf buffer
     * @param value A {@link Byte}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Byte value) {
        buf.writeByte(value != null ? value.byteValue() : 0);
    }

    /**
     * Write a {@link Short} value to specified buffer if it is not null, otherwise write two zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Short}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Short value) {
        buf.writeShort(value != null ? value.shortValue() : (short) 0);
    }

    /**
     * Write a {@link Integer} value to specified buffer if it is not null, otherwise write four zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Integer}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Integer value) {
        buf.writeInt(value != null ? value : 0);
    }

    /**
     * Write a {@link Byte} value to specified buffer if it is not null, otherwise write eight zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Byte}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Long value) {
        buf.writeLong(value != null ? value : 0L);
    }

    /**
     * Write a {@link Uint8} value to specified buffer if it is not null, otherwise write one zero byte.
     *
     * @param buf buffer
     * @param value A {@link Uint8}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Uint8 value) {
        buf.writeByte(value != null ? value.byteValue() : 0);
    }

    /**
     * Write a {@link Uint16} value to specified buffer if it is not null, otherwise write two zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Uint16}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Uint16 value) {
        buf.writeShort(value != null ? value.shortValue() : (short) 0);
    }

    /**
     * Write a {@link Uint32} value to specified buffer if it is not null, otherwise write four zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Uint32}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Uint32 value) {
        buf.writeInt(value != null ? value.intValue() : 0);
    }

    /**
     * Write a {@link Uint64} value to specified buffer if it is not null, otherwise write eight zero bytes.
     *
     * @param buf buffer
     * @param value A {@link Uint64}
     * @throws NullPointerException if {@code buf} is null
     */
    public static void writeOrZero(final ByteBuf buf, final @Nullable Uint64 value) {
        buf.writeLong(value != null ? value.longValue() : 0L);
    }

    private static <T> @NonNull T nonNullArgument(final @Nullable T obj, final String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " is mandatory");
        }
        return obj;
    }
}

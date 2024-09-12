/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValue;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

/**
 *
 */
@NonNullByDefault
public final class YangBoolean implements CanonicalValue<YangBoolean>, ScalarValue {
    public static final class Support extends AbstractCanonicalValueSupport<YangBoolean> {
        public Support() {
            super(YangBoolean.class);
        }

        @Override
        public Either<YangBoolean, CanonicalValueViolation> fromString(final String str) {
            try {
                return Either.ofFirst(YangBoolean.valueOf(str));
            } catch (IllegalArgumentException e) {
                return CanonicalValueViolation.variantOf(e);
            }
        }
    }

    public static final YangBoolean TRUE = new YangBoolean(true);
    public static final YangBoolean FALSE = new YangBoolean(false);

    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final CanonicalValueSupport<YangBoolean> SUPPORT = new Support();

    private final boolean value;

    private YangBoolean(final boolean value) {
        this.value = value;
    }

    /**
     * Returns an {@code YangBoolean} holding the value of the specified {@code String}.
     *
     * @param string String to parse
     * @return A YangBoolean instance
     * @throws NullPointerException if string is null
     * @throws IllegalArgumentException if string is neither {@code true} nor {@code false}
     */
    public static YangBoolean valueOf(final String string) {
        return switch (requireNonNull(string)) {
            case "false" -> FALSE;
            case "true" -> TRUE;
            default -> throw new IllegalArgumentException("");
        };
    }

    public static YangBoolean valueOf(final boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean value() {
        return value;
    }

    @Override
    public String toCanonicalString() {
        return value ? "true" : "false";
    }

    @Override
    public CanonicalValueSupport<YangBoolean> support() {
        return SUPPORT;
    }

    @Override
    public int compareTo(final YangBoolean other) {
        return Boolean.compare(value, other.value);
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof YangBoolean other && value == other.value;
    }

    @Override
    public String toString() {
        return toCanonicalString();
    }

    @java.io.Serial
    private Object writeReplace() {
        return value ? Bv1.TRUE : Bv1.FALSE;
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }

    static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(YangBoolean.class.getName());
    }
}

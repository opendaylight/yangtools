/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static com.google.common.base.Verify.verifyNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.MoreObjects;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;
import org.opendaylight.yangtools.yang.common.Uint16;

class ScalarTypeObjectTest {
    // Assume we have something like this for the base
    // see org.opendaylight.yang.gen.v1.urn.test.rev170101
    @NonNullByDefault
    public static class Id implements ScalarTypeObject<Uint16>, Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1247166106242089108L;

        private final Uint16 _value;

        private static void check_valueRange(final int value) {
            if (value >= 1 && value <= 4094) {
                return;
            }
            CodeHelpers.throwInvalidRange("[[1..4094]]", value);
        }

        //        @ConstructorParameters("value")
        // also @NonNull!
        public Id(final Uint16 _value) {
            CodeHelpers.requireValue(_value);
            if (_value != null) {
                check_valueRange(_value.intValue());
            }
            this._value = _value;
        }

        /**
         * Creates a copy from Source Object.
         *
         * @param source Source object
         */
        public Id(final Id source) {
            _value = source._value;
        }

        protected Id(final UnsafeSecret secret, final Uint16 _value) {
            verifyNotNull(secret);
            CodeHelpers.requireValue(_value);
            this._value = _value;
        }

        public static Id unsafeOf(final Uint16 value) {
            return CodeHelpers.newUnsafeScalar(value, Id::new, Id::new);
        }

        public static Id getDefaultInstance(final String defaultValue) {
            return new Id(Uint16.valueOf(defaultValue));
        }

        @Override
        public final Uint16 getValue() {
            return _value;
        }

        @Override
        public final int hashCode() {
            return CodeHelpers.wrapperHashCode(_value);
        }

        @Override
        public final boolean equals(final @Nullable Object obj) {
            return obj == this || obj instanceof Id other && _value.equals(other._value);
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(Id.class).add("value", _value).toString();
       }

    }

    @NonNullByDefault
    public static class Id2 extends Id {
        @java.io.Serial
        private static final long serialVersionUID = -1130527489749317280L;

        private static void check_valueRange(final int value) {
            if (value >= 2 && value <= 3) {
                return;
            }
            CodeHelpers.throwInvalidRange("[[2..3]]", value);
        }

        // @ConstructorParameters("value")
        public Id2(final Uint16 _value) {
            super(_value);
            check_valueRange(_value.intValue());
        }

        /**
         * Creates a copy from Source Object.
         *
         * @param source Source object
         */
        public Id2(final Id2 source) {
            super(source);
        }

        /**
         * Creates a new instance from Id
         *
         * @param source Source object
         */
        public Id2(final Id source) {
            super(source);
            // FIXME: enforcers?!
        }

        protected Id2(final UnsafeSecret secret, final Uint16 _value) {
            super(secret, _value);
        }

        public static Id2 unsafeOf(final Uint16 value) {
            return CodeHelpers.newUnsafeScalar(value, Id2::new, Id2::new);
        }

        public static Id2 getDefaultInstance(final String defaultValue) {
            return new Id2(Uint16.valueOf(defaultValue));
        }
    }

    @Test
    void testSerialization() throws Exception {
        final var one = new Id(Uint16.ONE);
        final var two = new Id2(Uint16.TWO);

        final byte[] bytes;
        try (var baos = new ByteArrayOutputStream()) {
            try (var oos = new ObjectOutputStream(baos)) {
                oos.writeObject(one);
                oos.writeObject(two);
            }
            bytes = baos.toByteArray();
        }

        assertEquals(329, bytes.length);

        try (var oos = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(one, oos.readObject());
            assertEquals(two, oos.readObject());
        }
    }

    @Test
    void testUnsafeInit() {
        assertEquals(new Id(Uint16.ONE), Id.unsafeOf(Uint16.ONE));
        assertEquals(Id.unsafeOf(Uint16.MAX_VALUE), Id2.unsafeOf(Uint16.MAX_VALUE));
    }
}

/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.concepts.Either;

@NonNullByDefault
public class DerivedStringTest {
    public static class EagerDerivedString extends CachingDerivedString<EagerDerivedString> {
        private static final long serialVersionUID = 1L;

        protected EagerDerivedString(final String str) {
            super(str);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public final int compareTo(final EagerDerivedString o) {
            return toCanonicalString().compareTo(o.toCanonicalString());
        }

        @Override
        public final CanonicalValueSupport<EagerDerivedString> support() {
            return EAGER_SUPPORT;
        }

        @Override
        public final int hashCode() {
            return toCanonicalString().hashCode();
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof String) {
                return toCanonicalString().equals(obj);
            }

            return obj instanceof DerivedString
                    && toCanonicalString().equals(((DerivedString<?>)obj).toCanonicalString());
        }

        @Override
        protected final String computeCanonicalString() {
            throw new UnsupportedOperationException();
        }
    }

    public static class LazyDerivedString extends CachingDerivedString<LazyDerivedString> {
        private static final long serialVersionUID = 1L;

        private final String str;

        protected LazyDerivedString(final String str) {
            this.str = str;
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public final int compareTo(final LazyDerivedString o) {
            return str.compareTo(o.str);
        }

        @Override
        public final CanonicalValueSupport<LazyDerivedString> support() {
            return LAZY_SUPPORT;
        }

        @Override
        public final int hashCode() {
            return str.hashCode();
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof String) {
                return str.equals(obj);
            }

            return obj instanceof DerivedString && str.equals(((DerivedString<?>)obj).toCanonicalString());
        }

        @Override
        protected final String computeCanonicalString() {
            return str;
        }
    }

    public static final class EagerDerivedStringSupport extends AbstractCanonicalValueSupport<EagerDerivedString> {
        EagerDerivedStringSupport() {
            super(EagerDerivedString.class);
        }

        @Override
        public Either<EagerDerivedString, CanonicalValueViolation> fromString(final String str) {
            return Either.ofFirst(new EagerDerivedString(str));
        }
    }

    public static final class LazyDerivedStringSupport extends AbstractCanonicalValueSupport<LazyDerivedString> {
        LazyDerivedStringSupport() {
            super(LazyDerivedString.class);
        }

        @Override
        public Either<LazyDerivedString, CanonicalValueViolation> fromString(final String str) {
            return Either.ofFirst(new LazyDerivedString(str));
        }
    }

    private static final CanonicalValueSupport<EagerDerivedString> EAGER_SUPPORT = new EagerDerivedStringSupport();
    private static final CanonicalValueSupport<LazyDerivedString> LAZY_SUPPORT = new LazyDerivedStringSupport();

    @Test
    public void testEager() {
        final DerivedString<?> foo = new EagerDerivedString("foo");
        assertSame("foo", foo.toString());
    }

    @Test
    public void testLazy() {
        final DerivedString<?> foo = new LazyDerivedString("foo");
        final String first = foo.toString();
        assertEquals("foo", first);
        assertSame(first, foo.toString());
    }

}

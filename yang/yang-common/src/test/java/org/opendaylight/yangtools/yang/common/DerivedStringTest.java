/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

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
        public final DerivedStringSupport<EagerDerivedString> support() {
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

            return obj instanceof DerivedString && toCanonicalString().equals(((DerivedString<?>)obj).toCanonicalString());
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
        public final DerivedStringSupport<LazyDerivedString> support() {
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

    public static final class EagerDerivedStringSupport extends AbstractDerivedStringSupport<EagerDerivedString> {
        EagerDerivedStringSupport() {
            super(EagerDerivedString.class);
        }

        @Override
        public EagerDerivedString fromString(final String str) {
            return new EagerDerivedString(str);
        }
    }

    public static final class LazyDerivedStringSupport extends AbstractDerivedStringSupport<LazyDerivedString> {
        LazyDerivedStringSupport() {
            super(LazyDerivedString.class);
        }

        @Override
        public LazyDerivedString fromString(final String str) {
            return new LazyDerivedString(str);
        }
    }

    private static final DerivedStringSupport<EagerDerivedString> EAGER_SUPPORT = new EagerDerivedStringSupport();
    private static final DerivedStringSupport<LazyDerivedString> LAZY_SUPPORT = new LazyDerivedStringSupport();

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

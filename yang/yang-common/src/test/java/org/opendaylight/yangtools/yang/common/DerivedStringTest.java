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
    private static final class EagerDerivedString extends CachingDerivedString<EagerDerivedString> {
        private static final long serialVersionUID = 1L;

        EagerDerivedString(final String str) {
            super(str);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(final EagerDerivedString o) {
            return canonicalString().compareTo(o.canonicalString());
        }

        @Override
        public DerivedStringSupport<EagerDerivedString> support() {
            return EAGER_SUPPORT;
        }

        @Override
        public int hashCode() {
            return canonicalString().hashCode();
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof String) {
                return canonicalString().equals(obj);
            }

            return obj instanceof DerivedString && canonicalString().equals(((DerivedString<?>)obj).canonicalString());
        }

        @Override
        protected String computeCanonicalString() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class LazyDerivedString extends CachingDerivedString<LazyDerivedString> {
        private static final long serialVersionUID = 1L;

        private final String str;

        LazyDerivedString(final String str) {
            this.str = str;
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public int compareTo(final LazyDerivedString o) {
            return str.compareTo(o.str);
        }

        @Override
        public DerivedStringSupport<LazyDerivedString> support() {
            return LAZY_SUPPORT;
        }

        @Override
        public int hashCode() {
            return str.hashCode();
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof String) {
                return str.equals(obj);
            }

            return obj instanceof DerivedString && str.equals(((DerivedString<?>)obj).canonicalString());
        }

        @Override
        protected String computeCanonicalString() {
            return new String(str);
        }
    }

    private static final DerivedStringSupport<EagerDerivedString> EAGER_SUPPORT =
            new DerivedStringSupport<EagerDerivedString>() {
        @Override
        public Class<EagerDerivedString> getRepresentationClass() {
            return EagerDerivedString.class;
        }

        @Override
        public EagerDerivedString forString(final String str) {
            return new EagerDerivedString(str);
        }
    };

    private static final DerivedStringSupport<LazyDerivedString> LAZY_SUPPORT =
            new DerivedStringSupport<LazyDerivedString>() {
        @Override
        public Class<LazyDerivedString> getRepresentationClass() {
            return LazyDerivedString.class;
        }

        @Override
        public LazyDerivedString forString(final String str) {
            return new LazyDerivedString(str);
        }
    };

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

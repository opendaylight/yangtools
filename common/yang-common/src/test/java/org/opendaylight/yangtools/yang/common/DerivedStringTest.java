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
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.concepts.Either;

@NonNullByDefault
class DerivedStringTest {
    static final class EagerDerivedStringSupport extends AbstractCanonicalValueSupport<EagerDerivedString> {
        EagerDerivedStringSupport() {
            super(EagerDerivedString.class);
        }

        @Override
        public Either<EagerDerivedString, CanonicalValueViolation> fromString(final String str) {
            return Either.ofFirst(new EagerDerivedString(str));
        }
    }

    static final class LazyDerivedStringSupport extends AbstractCanonicalValueSupport<LazyDerivedString> {
        LazyDerivedStringSupport() {
            super(LazyDerivedString.class);
        }

        @Override
        public Either<LazyDerivedString, CanonicalValueViolation> fromString(final String str) {
            return Either.ofFirst(new LazyDerivedString(str));
        }
    }

    static final CanonicalValueSupport<EagerDerivedString> EAGER_SUPPORT = new EagerDerivedStringSupport();
    static final CanonicalValueSupport<LazyDerivedString> LAZY_SUPPORT = new LazyDerivedStringSupport();

    @Test
    void testEager() {
        final var foo = new EagerDerivedString("foo");
        assertSame("foo", foo.toString());
    }

    @Test
    void testLazy() {
        final var foo = new LazyDerivedString("foo");
        final var first = foo.toString();
        assertEquals("foo", first);
        assertSame(first, foo.toString());
    }
}

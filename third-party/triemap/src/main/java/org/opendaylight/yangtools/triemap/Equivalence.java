/*
 * (C) Copyright 2016 Pantheon Technologies, s.r.o. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.triemap;

import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Internal equivalence class, similar to {@link com.google.common.base.Equivalence}, but explicitly not handling
 * nulls. We use equivalence only for keys, which are guaranteed to be non-null.
 *
 * @author Robert Varga
 */
abstract class Equivalence<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final class Equals extends Equivalence<Object> {
        private static final long serialVersionUID = 1L;

        static final Equals INSTANCE = new Equals();

        @Override
        boolean equivalent(final Object a, final Object b) {
            return a.equals(b);
        }

        @Override
        Object readResolve() {
            return INSTANCE;
        }
    }

    private static final class Identity extends Equivalence<Object> {
        private static final long serialVersionUID = 1L;

        static final Identity INSTANCE = new Identity();

        @Override
        boolean equivalent(final Object a, final Object b) {
            return a == b;
        }

        @Override
        Object readResolve() {
            return INSTANCE;
        }
    }

    static Equivalence<Object> equals() {
        return Equals.INSTANCE;
    }

    static Equivalence<Object> identity() {
        return Identity.INSTANCE;
    }

    final int hash(@Nonnull final T t) {
        int h = t.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        return h;
    }

    abstract boolean equivalent(@Nonnull T a, @Nonnull T b);

    abstract Object readResolve();
}

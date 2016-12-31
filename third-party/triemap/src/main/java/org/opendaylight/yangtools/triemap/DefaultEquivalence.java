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

import com.google.common.base.Equivalence;
import java.io.Serializable;

/**
 * Default equivalence, functionally equivalent to {@link Equivalence#equals()}, except it additionally
 * perturbs the object's hash code.
 *
 * @author Robert Varga
 */
final class DefaultEquivalence extends Equivalence<Object> implements Serializable {
    private static final long serialVersionUID = 1L;

    static final DefaultEquivalence INSTANCE = new DefaultEquivalence();

    private DefaultEquivalence() {
        // Hidden on purpose
    }

    @Override
    protected boolean doEquivalent(final Object a, final Object b) {
        return a.equals(b);
    }

    @Override
    protected int doHash(final Object t) {
        int h = t.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        return h;
    }

    @SuppressWarnings("static-method")
    private Object readResolve() {
        return INSTANCE;
    }
}

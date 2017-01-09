/*
 * (C) Copyright 2017 Pantheon Technologies, s.r.o. and others.
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

import static com.google.common.base.Verify.verify;

import com.google.common.math.IntMath;
import java.math.RoundingMode;

/**
 * Various implementation-specific constants shared across classes.
 *
 * @author Robert Varga
 */
final class Constants {
    private Constants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Size of the hash function, in bits.
     */
    static final int HASH_BITS = Integer.SIZE;

    /**
     * Size of the CNode bitmap, in bits.
     */
    static final int BITMAP_BITS = Integer.SIZE;

    /**
     * Number of hash bits consumed in each CNode level.
     */
    static final int LEVEL_BITS = 5;

    /**
     * Maximum depth of a TrieMap.
     */
    static final int MAX_DEPTH = 7;

    static {
        verify(LEVEL_BITS == IntMath.log2(BITMAP_BITS, RoundingMode.UNNECESSARY));
        verify(MAX_DEPTH == IntMath.divide(HASH_BITS, LEVEL_BITS, RoundingMode.CEILING));
    }
}

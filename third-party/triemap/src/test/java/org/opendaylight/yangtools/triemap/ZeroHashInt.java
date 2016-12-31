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

import com.google.common.base.MoreObjects;

/**
 * Utility key/value class which attacks the hasing function, causing all objects to be put into a single bucket.
 *
 * @author Robert Varga
 */
final class ZeroHashInt {
    private final int i;

    ZeroHashInt(final int i) {
        this.i = i;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ZeroHashInt && i == ((ZeroHashInt) o).i;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("i", i).add("identity", System.identityHashCode(this)).toString();
    }
}

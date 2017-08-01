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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map.Entry;

/**
 * A single entry in {@link LNodeEntries}, implements {@link Entry} in order to prevent instantiation of objects for
 * iteration.
 *
 * @author Robert Varga
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
abstract class LNodeEntry<K, V> implements Entry<K, V> {
    private final K key;
    private final V value;

    LNodeEntry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public final K getKey() {
        return key;
    }

    @Override
    public final V getValue() {
        return value;
    }

    @Override
    public final V setValue(final V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final int hashCode() {
        return EntryUtil.hash(key, value);
    }

    @SuppressFBWarnings(value = "EQ_UNUSUAL",  justification = "Equality handled by utility methods")
    @Override
    public final boolean equals(final Object obj) {
        return EntryUtil.equal(obj, key, value);
    }

    @Override
    public final String toString() {
        return EntryUtil.string(key, value);
    }
}

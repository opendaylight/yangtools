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

import java.util.Map.Entry;

/**
 * This iterator is a read-only one and does not allow for any update
 * operations on the underlying data structure.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
final class TrieMapReadOnlyIterator<K, V> extends TrieMapIterator<K, V> {
    TrieMapReadOnlyIterator (final int level, final TrieMap<K, V> ct, final boolean mustInit) {
        super (level, ct, mustInit);
    }

    TrieMapReadOnlyIterator (final int level, final TrieMap<K, V> ct) {
        this (level, ct, true);
    }
    @Override
    void initialize () {
        assert (ct.isReadOnly ());
        super.initialize ();
    }

    @Override
    public void remove () {
        throw new UnsupportedOperationException ("Operation not supported for read-only iterators");
    }

    @Override
    Entry<K, V> nextEntry(final Entry<K, V> rr) {
        // Return non-updatable entry
        return rr;
    }
}
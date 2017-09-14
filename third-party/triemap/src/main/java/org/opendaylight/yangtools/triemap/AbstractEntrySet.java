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

import static java.util.Objects.requireNonNull;

import java.util.AbstractSet;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Abstract base class for implementing {@link TrieMap} entry sets.
 *
 * @author Robert Varga
 *
 * @param <K> the type of entry keys
 * @param <V> the type of entry values
 */
abstract class AbstractEntrySet<K, V> extends AbstractSet<Entry<K, V>> {
    private final TrieMap<K, V> map;

    AbstractEntrySet(final TrieMap<K, V> map) {
        this.map = requireNonNull(map);
    }

    final TrieMap<K, V> map() {
        return map;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final boolean contains(final Object o) {
        if (!(o instanceof Entry)) {
            return false;
        }

        final Entry<?, ?> e = (Entry<?, ?>) o;
        final Object key = e.getKey();
        if (key == null) {
            return false;
        }

        final V v = map.get(key);
        return v != null && v.equals(e.getValue());
    }

    @Override
    public final int size() {
        return map.size();
    }

    @Override
    public final Spliterator<Entry<K, V>> spliterator() {
        // TODO: this is backed by an Iterator, we should be able to do better
        return Spliterators.spliterator(map.immutableIterator(), Long.MAX_VALUE,
            // XXX: Distinct as far as associated Equivalence allows
            Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }
}

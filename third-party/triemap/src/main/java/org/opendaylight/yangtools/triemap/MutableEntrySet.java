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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Support for EntrySet operations required by the Map interface.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
final class MutableEntrySet<K, V> extends AbstractEntrySet<K, V> {
    MutableEntrySet(final TrieMap<K, V> map) {
        super(map);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean add(final Entry<K, V> e) {
        final K k = e.getKey();
        checkArgument(k != null);
        final V v = e.getValue();
        checkArgument(v != null);

        final V prev = map().putIfAbsent(k, v);
        return prev == null || !v.equals(prev);
    }

    @Override
    public void clear() {
        map().clear();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map().iterator();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        if (!(o instanceof Entry)) {
            return false;
        }

        final Entry<?, ?> e = (Entry<?, ?>) o;
        final Object key = e.getKey();
        if (key == null) {
            return false;
        }
        final Object value = e.getValue();
        if (value == null) {
            return false;
        }

        return map().remove(key, value);
    }
}

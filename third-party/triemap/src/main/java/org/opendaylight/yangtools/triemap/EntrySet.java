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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;

/***
 * Support for EntrySet operations required by the Map interface
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
final class EntrySet<K, V> extends AbstractSet<Entry<K, V>> {
    private final TrieMap<K, V> map;

    EntrySet(final TrieMap<K, V> map) {
        this.map = checkNotNull(map);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map.iterator();
    }

    @Override
    public boolean contains(final Object o) {
        if (!(o instanceof Entry)) {
            return false;
        }

        final Entry<?, ?> e = (Entry<?, ?>) o;
        if (e.getKey() == null) {
            return false;
        }
        final V v = map.get(e.getKey());
        return v != null && v.equals(e.getValue());
    }

    @Override
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

        return map.remove(key, value);
    }

    @Override
    public final int size() {
        return map.size();
    }

    @Override
    public final void clear() {
        map.clear();
    }
}

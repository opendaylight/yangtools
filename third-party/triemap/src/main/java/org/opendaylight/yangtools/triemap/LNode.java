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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;

final class LNode<K, V> extends MainNode<K, V> {
    private final ListMap<K, V> listmap;

    private LNode(final ListMap<K, V> listmap) {
        this.listmap = listmap;
    }

    LNode(final K k1, final V v1, final K k2, final V v2) {
        this(ListMap.map(k1, v1, k2, v2));
    }

    LNode<K, V> inserted(final K k, final V v) {
        return new LNode<>(listmap.add(k, v));
    }

    // FIXME: can we also get the hashcode ?
    MainNode<K, V> removed(final K k, final TrieMap<K, V> ct) {
        // We only ever create ListMaps with two or more entries,  and remove them as soon as they reach one element
        // (below), so we cannot observe a null return here.
        final ListMap<K, V> map = listmap.remove(k);
        final Optional<Entry<K, V>> maybeKv = map.maybeSingleton();
        if (maybeKv.isPresent()) {
            final Entry<K, V> kv = maybeKv.get();
            // create it tombed so that it gets compressed on subsequent accesses
            return new TNode<>(kv.getKey(), kv.getValue(), ct.computeHash(kv.getKey()));
        }

        return new LNode<>(map);
    }

    Optional<V> get(final K k) {
        return listmap.get(k);
    }

    @Override
    int cachedSize(final TrieMap<K, V> ct) {
        return listmap.size();
    }

    @Override
    String string(final int lev) {
        // (" " * lev) + "LNode(%s)".format(listmap.mkString(", "))
        return "LNode";
    }

    Iterator<Entry<K, V>> iterator() {
        return listmap.iterator();
    }
}
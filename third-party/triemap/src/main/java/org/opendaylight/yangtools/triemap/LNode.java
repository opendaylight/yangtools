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

final class LNode<K, V> extends MainNode<K, V> {
    private final LNodeEntries<K, V> listmap;

    private LNode(final LNodeEntries<K, V> listmap) {
        this.listmap = listmap;
    }

    LNode(final K k1, final V v1, final K k2, final V v2) {
        this(LNodeEntries.map(k1, v1, k2, v2));
    }

    LNode<K, V> insertChild( final K k, final V v) {
        return new LNode<>(listmap.insert(k, v));
    }

    MainNode<K, V> removeChild(final LNodeEntry<K, V> entry, final int hc) {
        // We only ever create ListMaps with two or more entries,  and remove them as soon as they reach one element
        // (below), so we cannot observe a null return here.
        final LNodeEntries<K, V> map = listmap.remove(entry);
        if (map.isSingle()) {
            // create it tombed so that it gets compressed on subsequent accesses
            return new TNode<>(map.getKey(), map.getValue(), hc);
        }

        return new LNode<>(map);
    }

    MainNode<K, V> replaceChild(final LNodeEntry<K, V> entry, final V v) {
        return new LNode<>(listmap.replace(entry, v));
    }

    LNodeEntry<K, V> get(final Equivalence<? super K> equiv, final K k) {
        return listmap.findEntry(equiv, k);
    }

    @Override
    int cachedSize(final TrieMap<?, ?> ct) {
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
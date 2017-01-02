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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Mimic immutable ListMap in Scala
 *
 * @author Roman Levenstein <romixlev@gmail.com>
 *
 * @param <V>
 */
final class ListMap<K, V> {
    private final K k;
    private final V v;

    // Modified during remove0 only
    private ListMap<K, V> next;

    private ListMap(final K k, final V v) {
        this(k, v, null);
    }

    private ListMap(final K k, final V v, final ListMap<K, V> next) {
        this.k = k;
        this.v = v;
        this.next = next;
    }

    static <K,V> ListMap<K, V> map(final K k1, final V v1, final K k2, final V v2) {
        return new ListMap<>(k1, v1, new ListMap<>(k2, v2));
    }

    Optional<Entry<K, V>> maybeSingleton() {
        return next != null ? Optional.empty() : Optional.of(new SimpleImmutableEntry<>(k, v));
    }

    int size() {
        int sz = 1;
        for (ListMap<?, ?> wlk = next; wlk != null; wlk = wlk.next) {
            sz++;
        }
        return sz;
    }

    Optional<V> get(final Equivalence<? super K> equiv, final K key) {
        // We do not perform recursion on purpose here, so we do not run out of stack if the key hashing fails.
        ListMap<K, V> head = this;
        do {
            if (equiv.equivalent(head.k, key)) {
                return Optional.of(head.v);
            }

            head = head.next;
        } while (head != null);

        return Optional.empty();
    }

    ListMap<K,V> add(final Equivalence<? super K> equiv, final K key, final V value) {
        return new ListMap<>(key, value, remove(equiv, key));
    }

    ListMap<K, V> remove(final Equivalence<? super K> equiv, final K key) {
         if (!contains(equiv, key)) {
            return this;
        }

        return remove0(key);
    }

    private boolean contains(final Equivalence<? super K> equiv, final K key) {
        // We do not perform recursion on purpose here, so we do not run out of stack if the key hashing fails.
        ListMap<K, V> head = this;
        do {
            if (equiv.equivalent(head.k, key)) {
                return true;
            }
            head = head.next;
        } while (head != null);

        return false;
    }

    private ListMap<K, V> remove0(final K key) {
        ListMap<K, V> n = this;
        ListMap<K, V> ret = null;
        ListMap<K, V> lastN = null;
        while (n != null) {
            if (key.equals(n.k)) {
                n = n.next;
                continue;
            }

            if (ret != null) {
                lastN.next = new ListMap<>(n.k, n.v);
                lastN = lastN.next;
            } else {
                ret = new ListMap<>(n.k, n.v);
                lastN = ret;
            }
            n = n.next;
        }
        return ret;
    }

    Iterator<Entry<K, V>> iterator() {
        return new NodeIterator<>(this);
    }

    static final class NodeIterator<K,V> implements Iterator<Entry<K, V>> {
        private ListMap<K, V> n;

        NodeIterator(final ListMap<K, V> n) {
            this.n = n;
        }

        @Override
        public boolean hasNext() {
            return n != null;
        }

        @Override
        public Entry<K, V> next() {
            if (n == null) {
                throw new NoSuchElementException();
            }

            final Entry<K, V> res = new SimpleImmutableEntry<>(n.k, n.v);
            n = n.next;
            return res;
        }
    }
}

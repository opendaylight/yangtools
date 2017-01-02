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
import java.util.NoSuchElementException;

/**
 * Similar to Scala's ListMap. Stores a linked set of entries, guaranteed to contain unique entry keys.
 *
 * @author Robert Varga
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
abstract class LNodeEntries<K, V> extends LNodeEntry<K, V> {
    private static final class Single<K, V> extends LNodeEntries<K, V> {
        Single(final K k, final V v) {
            super(k, v);
        }

        @Override
        boolean isSingle() {
            return true;
        }

        @Override
        LNodeEntries<K, V> next() {
            return null;
        }
    }

    private static final class Multiple<K, V> extends LNodeEntries<K, V> {
        // Modified during remove only
        LNodeEntries<K, V> next;

        // Used in remove() only
        Multiple(final LNodeEntries<K, V> e) {
            this(e.getKey(), e.getValue(), null);
        }

        Multiple(final K k, final V v, final LNodeEntries<K, V> next) {
            super(k, v);
            this.next = next;
        }

        @Override
        boolean isSingle() {
            return next == null;
        }

        @Override
        LNodeEntries<K, V> next() {
            return next;
        }
    }

    LNodeEntries(final K k, final V v) {
        super(k, v);
    }

    static <K,V> LNodeEntries<K, V> map(final K k1, final V v1, final K k2, final V v2) {
        return new Multiple<>(k1, v1, new Single<>(k2, v2));
    }

    abstract boolean isSingle();

    abstract LNodeEntries<K, V> next();

    final LNodeEntry<K, V> findEntry(final Equivalence<? super K> equiv, final K key) {
        // We do not perform recursion on purpose here, so we do not run out of stack if the key hashing fails.
        LNodeEntries<K, V> entry = this;
        do {
            if (equiv.equivalent(entry.getKey(), key)) {
                return entry;
            }

            entry = entry.next();
        } while (entry != null);

        return null;
    }

    final LNodeEntries<K,V> insert(final K key, final V value) {
        return new Multiple<>(key, value, this);
    }

    final LNodeEntries<K, V> replace(final LNodeEntry<K, V> entry, final V v) {
        final LNodeEntries<K, V> removed = remove(entry);
        return removed == null ? new Single<>(entry.getKey(), v) : new Multiple<>(entry.getKey(), v, removed);
    }

    final LNodeEntries<K, V> remove(final LNodeEntry<K, V> entry) {
        if (entry == this) {
            return next();
        }

        final Multiple<K, V> ret = new Multiple<>(this);

        Multiple<K, V> last = ret;
        LNodeEntries<K, V> cur = next();
        while (cur != null) {
            // We cannot use equals() here, as it is wired to key/value equality,
            // which we really do not want.
            if (entry == cur) {
                last.next = cur.next();
                return ret;
            }

            final Multiple<K, V> tmp = new Multiple<>(cur);
            last.next = tmp;
            last = tmp;
            cur = cur.next();
        }

        throw new IllegalStateException(String.format("Entry %s not found", entry));
    }

    // No need to specialize these two methods, as they are invoked only from a stable LNode, at which point it is
    // guaranteed to be a Multiple.
    final int size() {
        int sz = 1;
        for (LNodeEntries<?, ?> wlk = next(); wlk != null; wlk = wlk.next()) {
            sz++;
        }
        return sz;
    }

    final Iterator<Entry<K, V>> iterator() {
        return new NodeIterator<>(this);
    }

    static final class NodeIterator<K,V> implements Iterator<Entry<K, V>> {
        private LNodeEntries<K, V> n;

        NodeIterator(final LNodeEntries<K, V> n) {
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

            final Entry<K, V> res = n;
            n = n.next();
            return res;
        }
    }
}

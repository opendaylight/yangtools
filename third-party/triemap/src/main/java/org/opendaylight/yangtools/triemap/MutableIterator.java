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

import static com.google.common.base.Preconditions.checkState;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map.Entry;

/**
 * Specialized immutable iterator for use with {@link ImmutableEntrySet}.
 *
 * @author Robert Varga
 *
 * @param <K> the type of entry keys
 * @param <V> the type of entry values
 */
final class MutableIterator<K, V> extends AbstractIterator<K, V> {
    private final MutableTrieMap<K, V> mutable;

    private Entry<K, V> lastReturned;

    MutableIterator(final MutableTrieMap<K, V> map) {
        super(map.immutableSnapshot());
        this.mutable = map;
    }

    @Override
    public void remove() {
        checkState(lastReturned != null);
        mutable.remove(lastReturned.getKey());
        lastReturned = null;
    }

    @Override
    Entry<K, V> wrapEntry(final Entry<K, V> entry) {
        lastReturned = entry;
        return new MutableEntry<>(mutable, entry);
    }

    /**
     * A mutable view of an entry in the map. Since the backing map is concurrent, its {@link #getValue()} and
     * {@link #setValue(Object)} methods cannot guarantee consistency with the base map and may produce surprising
     * results when the map is concurrently modified, either directly or via another entry/iterator.
     *
     * <p>
     * The behavior is similar to what Java 8's ConcurrentHashMap does, which is probably the most consistent handling
     * of this case without requiring expensive and revalidation.
     */
    private static final class MutableEntry<K, V> implements Entry<K, V> {
        private final MutableTrieMap<K, V> map;
        private final Entry<K, V> delegate;

        @SuppressWarnings("null")
        private V newValue = null;

        MutableEntry(final MutableTrieMap<K, V> map, final Entry<K, V> delegate) {
            this.map = map;
            this.delegate = delegate;
        }

        @Override
        public K getKey() {
            return delegate.getKey();
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the most uptodate value we have observed via this entry. It does not reflect
         *     concurrent modifications, nor does it throw {@link IllegalStateException} if the entry is removed.
         */
        @Override
        public V getValue() {
            return newValue != null ? newValue : delegate.getValue();
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the most uptodate value we have observed via this entry. It does not reflect
         *     concurrent modifications, nor does it throw {@link IllegalStateException} if the entry is removed.
         */
        @Override
        public V setValue(final V value) {
            final V ret = getValue();
            map.put(getKey(), value);
            newValue = value;
            return ret;
        }

        @Override
        public int hashCode() {
            return EntryUtil.hash(getKey(), getValue());
        }

        @SuppressFBWarnings(value = "EQ_UNUSUAL",  justification = "Equality handled by utility methods")
        @Override
        public boolean equals(final Object obj) {
            return EntryUtil.equal(obj, getKey(), getValue());
        }

        @Override
        public String toString() {
            return EntryUtil.string(getKey(), getValue());
        }
    }
}

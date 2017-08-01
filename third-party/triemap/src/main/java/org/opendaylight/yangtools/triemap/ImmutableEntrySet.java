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

import static org.opendaylight.yangtools.triemap.ImmutableTrieMap.unsupported;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * {@link AbstractEntrySet} implementation guarding against attempts to mutate the underlying map.
 *
 * @author Robert Varga
 *
 * @param <K> the type of entry keys
 * @param <V> the type of entry values
 */
final class ImmutableEntrySet<K, V> extends AbstractEntrySet<K, V> {
    ImmutableEntrySet(final TrieMap<K, V> map) {
        super(map);
    }

    @Override
    public void clear() {
        throw unsupported();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return map().immutableIterator();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw unsupported();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw unsupported();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw unsupported();
    }
}

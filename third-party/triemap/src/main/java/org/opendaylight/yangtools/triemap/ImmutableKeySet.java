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

import static org.opendaylight.yangtools.triemap.ImmutableTrieMap.unsupported;

import com.google.common.collect.Iterators;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An immutable view of a TrieMap's key set.
 *
 * @author Robert Varga
 *
 * @param <K> the type of keys
 */
final class ImmutableKeySet<K> extends AbstractKeySet<K> {
    ImmutableKeySet(final TrieMap<K, ?> map) {
        super(map);
    }

    @Override
    public Iterator<K> iterator() {
        return Iterators.transform(map().immutableIterator(), Entry::getKey);
    }

    @Override
    public void clear() {
        throw unsupported();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        throw unsupported();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean retainAll(final Collection<?> c) {
        throw unsupported();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean removeAll(final Collection<?> c) {
        throw unsupported();
    }
}

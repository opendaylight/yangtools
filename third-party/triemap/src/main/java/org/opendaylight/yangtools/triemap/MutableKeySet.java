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

/**
 * A mutable view of a TrieMap's key set.
 *
 * @author Robert Varga
 *
 * @param <K> the type of keys
 */
final class MutableKeySet<K> extends AbstractKeySet<K> {
    MutableKeySet(final MutableTrieMap<K, ?> map) {
        super(map);
    }

    @Override
    public Iterator<K> iterator() {
        final AbstractIterator<K, ?> itr = map().iterator();
        return new Iterator<K>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public K next() {
                return itr.next().getKey();
            }

            @Override
            public void remove() {
                itr.remove();
            }
        };
    }

    @Override
    public void clear() {
        map().clear();
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public boolean remove(final Object o) {
        return map().remove(o) != null;
    }
}

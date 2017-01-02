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

import java.util.Map.Entry;

/**
 * Common marker interface for nodes which act as an immutable {@link Entry}.
 *
 * @author Robert Varga
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
interface EntryNode<K, V> extends Entry<K, V> {
    @Override
    default public V setValue(final V value) {
        throw new UnsupportedOperationException();
    }
}

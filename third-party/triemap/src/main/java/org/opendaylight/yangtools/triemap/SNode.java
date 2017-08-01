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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

final class SNode<K, V> extends BasicNode implements EntryNode<K, V> {
    final K key;
    final V value;
    final int hc;

    SNode(final K key, final V value, final int hc) {
        this.key = key;
        this.value = value;
        this.hc = hc;
    }

    SNode<K, V> copy() {
        return new SNode<>(key, value, hc);
    }

    TNode<K, V> copyTombed() {
        return new TNode<>(key, value, hc);
    }

    SNode<K, V> copyUntombed() {
        return new SNode<>(key, value, hc);
    }

    @Override
    String string(final int lev) {
        // ("  " * lev) + "SNode(%s, %s, %x)".format(k, v, hc);
        return "SNode";
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return EntryUtil.hash(key, value);
    }

    @SuppressFBWarnings(value = "EQ_UNUSUAL",  justification = "Equality handled by utility methods")
    @Override
    public boolean equals(final Object obj) {
        return EntryUtil.equal(obj, key, value);
    }

    @Override
    public String toString() {
        return EntryUtil.string(key, value);
    }
}

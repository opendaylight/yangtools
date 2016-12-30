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
import java.util.Map.Entry;

final class SNode<K, V> extends BasicNode implements KVNode<K, V> {
    final K k;
    final V v;
    final int hc;

    SNode(final K k, final V v, final int hc) {
        this.k = k;
        this.v = v;
        this.hc = hc;
    }

    SNode<K, V> copy() {
        return new SNode<>(k, v, hc);
    }

    TNode<K, V> copyTombed() {
        return new TNode<>(k, v, hc);
    }

    SNode<K, V> copyUntombed() {
        return new SNode<>(k, v, hc);
    }

    @Override
    public Entry<K, V> kvPair() {
        return new SimpleImmutableEntry<>(k, v);
    }

    @Override
    String string(final int lev) {
        // ("  " * lev) + "SNode(%s, %s, %x)".format(k, v, hc);
        return "SNode";
    }
}
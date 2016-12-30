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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class MainNode<K, V> extends BasicNode {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<MainNode, MainNode> PREV_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(MainNode.class, MainNode.class, "prev");

    private volatile MainNode<K, V> prev = null;

    abstract int cachedSize(TrieMap<K, V> ct);

    final boolean CAS_PREV(final MainNode<K, V> oldval, final MainNode<K, V> nval) {
        return PREV_UPDATER.compareAndSet(this, oldval, nval);
    }

    final void WRITE_PREV(final MainNode<K, V> nval) {
        prev = nval;
    }

    final MainNode<K, V> READ_PREV() {
        return prev;
    }
}

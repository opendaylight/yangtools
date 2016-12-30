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

abstract class INodeBase<K, V> extends BasicNode {

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<INodeBase, MainNode> MAINNODE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(INodeBase.class, MainNode.class, "mainnode");

    public final Gen gen;

    private volatile MainNode<K, V> mainnode;

    INodeBase(final Gen generation, final MainNode<K, V> mainnode) {
        gen = generation;
        this.mainnode = mainnode;
    }

    final boolean CAS(final MainNode<K, V> old, final MainNode<K, V> n) {
        return MAINNODE_UPDATER.compareAndSet(this, old, n);
    }

    final MainNode<K, V> READ() {
        return mainnode;
    }
}

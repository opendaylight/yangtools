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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

abstract class CNodeBase<K, V> extends MainNode<K, V> {
    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<CNodeBase> CSIZE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(CNodeBase.class, "csize");

    private volatile int csize = -1;

    final boolean CAS_SIZE(final int oldval, final int nval) {
        return CSIZE_UPDATER.compareAndSet(this, oldval, nval);
    }

    final int READ_SIZE() {
        return csize;
    }
}

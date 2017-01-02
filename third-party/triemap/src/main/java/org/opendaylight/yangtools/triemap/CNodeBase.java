/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2012, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.opendaylight.yangtools.triemap;



import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;



abstract class CNodeBase<K, V> extends MainNode<K, V> {

    public static final AtomicIntegerFieldUpdater<CNodeBase> updater = AtomicIntegerFieldUpdater.newUpdater(CNodeBase.class, "csize");

    public volatile int csize = -1;

    public boolean CAS_SIZE(final int oldval, final int nval) {
    return updater.compareAndSet(this, oldval, nval);
    }

    public void WRITE_SIZE(final int nval) {
    updater.set(this, nval);
    }

    public int READ_SIZE() {
    return updater.get(this);
    }

}
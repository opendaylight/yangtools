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

import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.yangtools.triemap.Constants.MAX_DEPTH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

class TrieMapIterator<K, V> implements Iterator<Entry<K, V>> {
    private int level;
    protected TrieMap<K, V> ct;
    private final boolean mustInit;
    private final BasicNode[][] stack = new BasicNode[MAX_DEPTH][];
    private final int[] stackpos = new int[MAX_DEPTH];
    private int depth = -1;
    private Iterator<Entry<K, V>> subiter = null;
    private EntryNode<K, V> current = null;
    private Entry<K, V> lastReturned = null;

    TrieMapIterator (final int level, final TrieMap<K, V> ct, final boolean mustInit) {
        this.level = level;
        this.ct = ct;
        this.mustInit = mustInit;
        if (this.mustInit) {
            initialize ();
        }
    }

    TrieMapIterator (final int level, final TrieMap<K, V> ct) {
        this (level, ct, true);
    }


    @Override
    public boolean hasNext() {
        return (current != null) || (subiter != null);
    }

    @Override
    public Entry<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final Entry<K, V> r;
        if (subiter != null) {
            r = subiter.next();
            checkSubiter();
        } else {
            r = current;
            advance();
        }

        lastReturned = r;
        if (r != null) {
            final Entry<K, V> rr = r;
            return nextEntry(rr);
        }
        return r;
    }

    Entry<K, V> nextEntry(final Entry<K, V> rr) {
        return new Entry<K, V>() {
            @SuppressWarnings("null")
            private V updated = null;

            @Override
            public K getKey () {
                return rr.getKey ();
            }

            @Override
            public V getValue () {
                return (updated == null) ? rr.getValue (): updated;
            }

            @Override
            public V setValue (final V value) {
                updated = value;
                return ct.replace (getKey (), value);
            }
        };
    }

    private void readin (final INode<K, V> in) {
        MainNode<K, V> m = in.gcasRead (ct);
        if (m instanceof CNode) {
            CNode<K, V> cn = (CNode<K, V>) m;
            depth += 1;
            stack [depth] = cn.array;
            stackpos [depth] = -1;
            advance ();
        } else if (m instanceof TNode) {
            current = (TNode<K, V>) m;
        } else if (m instanceof LNode) {
            subiter = ((LNode<K, V>) m).iterator();
            checkSubiter ();
        } else if (m == null) {
            current = null;
        }
    }

    // @inline
    private void checkSubiter () {
        if (!subiter.hasNext ()) {
            subiter = null;
            advance ();
        }
    }

    // @inline
    void initialize () {
        // assert (ct.isReadOnly());
        readin(ct.RDCSS_READ_ROOT());
    }

    void advance () {
        if (depth >= 0) {
            int npos = stackpos [depth] + 1;
            if (npos < stack [depth].length) {
                stackpos [depth] = npos;
                BasicNode elem = stack [depth] [npos];
                if (elem instanceof SNode) {
                    current = (SNode<K, V>) elem;
                } else if (elem instanceof INode) {
                    readin ((INode<K, V>) elem);
                }
            } else {
                depth -= 1;
                advance ();
            }
        } else {
            current = null;
        }
    }

    protected TrieMapIterator<K, V> newIterator(final int _lev, final TrieMap<K, V> _ct, final boolean _mustInit) {
        return new TrieMapIterator<> (_lev, _ct, _mustInit);
    }

    protected void dupTo(final TrieMapIterator<K, V> it) {
        it.level = this.level;
        it.ct = this.ct;
        it.depth = this.depth;
        it.current = this.current;

        // these need a deep copy
        System.arraycopy (this.stack, 0, it.stack, 0, 7);
        System.arraycopy (this.stackpos, 0, it.stackpos, 0, 7);

        // this one needs to be evaluated
        if (this.subiter == null) {
            it.subiter = null;
        } else {
            List<Entry<K, V>> lst = toList (this.subiter);
            this.subiter = lst.iterator ();
            it.subiter = lst.iterator ();
        }
    }

    // /** Returns a sequence of iterators over subsets of this iterator.
    // * It's used to ease the implementation of splitters for a parallel
    // version of the TrieMap.
    // */
    // protected def subdivide(): Seq[Iterator[(K, V)]] = if (subiter ne
    // null) {
    // // the case where an LNode is being iterated
    // val it = subiter
    // subiter = null
    // advance()
    // this.level += 1
    // Seq(it, this)
    // } else if (depth == -1) {
    // this.level += 1
    // Seq(this)
    // } else {
    // var d = 0
    // while (d <= depth) {
    // val rem = stack(d).length - 1 - stackpos(d)
    // if (rem > 0) {
    // val (arr1, arr2) = stack(d).drop(stackpos(d) + 1).splitAt(rem / 2)
    // stack(d) = arr1
    // stackpos(d) = -1
    // val it = newIterator(level + 1, ct, false)
    // it.stack(0) = arr2
    // it.stackpos(0) = -1
    // it.depth = 0
    // it.advance() // <-- fix it
    // this.level += 1
    // return Seq(this, it)
    // }
    // d += 1
    // }
    // this.level += 1
    // Seq(this)
    // }

    private List<Entry<K, V>> toList (final Iterator<Entry<K, V>> it) {
        ArrayList<Entry<K, V>> list = new ArrayList<> ();
        while (it.hasNext ()) {
            list.add (it.next());
        }
        return list;
    }

    @Override
    public void remove() {
        checkState(lastReturned != null);
        ct.remove(lastReturned.getKey());
        lastReturned = null;
    }
}
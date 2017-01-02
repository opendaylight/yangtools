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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.yangtools.triemap.LookupResult.RESTART;
import static org.opendaylight.yangtools.triemap.PresencePredicate.ABSENT;
import static org.opendaylight.yangtools.triemap.PresencePredicate.PRESENT;

import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/***
 * This is a port of Scala's TrieMap class from the Scala Collections library. This implementation does not support
 * null keys nor null values.
 *
 * @author Roman Levenstein &lt;romixlev@gmail.com&gt;
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public final class TrieMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K,V>, Serializable {
    private static final AtomicReferenceFieldUpdater<TrieMap, Object> ROOT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(TrieMap.class, Object.class, "root");
    private static final long serialVersionUID = 1L;

    /**
     * EntrySet
     */
    private final EntrySet entrySet = new EntrySet();
    private final Equivalence<? super K> equiv;
    private final boolean readOnly;

    private volatile Object root;

    private TrieMap(final INode<K, V> r, final Equivalence<? super K> equiv, final boolean readOnly) {
        this.root = r;
        this.equiv = equiv;
        this.readOnly = readOnly;
    }

    TrieMap(final Equivalence<? super K> equiv) {
        this(newRootNode(), equiv, false);
    }

    public TrieMap() {
        this(Equivalence.equals());
    }

    /* internal methods */

    final Equivalence<? super K> equiv() {
        return equiv;
    }

    private static <K,V> INode<K, V> newRootNode() {
        final Gen gen = new Gen();
        return new INode<>(gen, new CNode<>(gen));
    }

    final boolean CAS_ROOT(final Object ov, final Object nv) {
        checkState(!readOnly, "Attempted to modify a read-only snapshot");
        return ROOT_UPDATER.compareAndSet (this, ov, nv);
    }

    // FIXME: abort = false by default
    final INode<K, V> readRoot(final boolean abort) {
        return RDCSS_READ_ROOT(abort);
    }

    final INode<K, V> readRoot() {
        return RDCSS_READ_ROOT(false);
    }

    final INode<K, V> RDCSS_READ_ROOT() {
        return RDCSS_READ_ROOT(false);
    }

    final INode<K, V> RDCSS_READ_ROOT(final boolean abort) {
        final Object r = /* READ */root;
        if (r instanceof INode) {
            return (INode<K, V>) r;
        }

        checkState(r instanceof RDCSS_Descriptor, "Unhandled root %s", r);
        return RDCSS_Complete(abort);
    }

    private INode<K, V> RDCSS_Complete(final boolean abort) {
        while (true) {
            final Object r = /* READ */root;
            if (r instanceof INode) {
                return (INode<K, V>) r;
            }

            checkState(r instanceof RDCSS_Descriptor, "Unhandled root %s", r);
            final RDCSS_Descriptor<K, V> desc = (RDCSS_Descriptor<K, V>) r;
            final INode<K, V> ov = desc.old;
            final MainNode<K, V> exp = desc.expectedmain;
            final INode<K, V> nv = desc.nv;

            if (abort) {
                if (CAS_ROOT(desc, ov)) {
                    return ov;
                }

                // Tail recursion: return RDCSS_Complete(abort);
                continue;
            }

            final MainNode<K, V> oldmain = ov.gcasRead(this);
            if (oldmain == exp) {
                if (CAS_ROOT(desc, nv)) {
                    desc.committed = true;
                    return nv;
                }

                // Tail recursion: return RDCSS_Complete(abort);
                continue;
            }

            if (CAS_ROOT(desc, ov)) {
                return ov;
            }

            // Tail recursion: return RDCSS_Complete(abort);
        }
    }

    private boolean RDCSS_ROOT(final INode<K, V> ov, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
        final RDCSS_Descriptor<K, V> desc = new RDCSS_Descriptor<> (ov, expectedmain, nv);
        if (CAS_ROOT(ov, desc)) {
            RDCSS_Complete(false);
            return /* READ */desc.committed;
        }

        return false;
    }

    private void inserthc(final K k, final int hc, final V v) {
        // TODO: this is called from serialization only, which means we should not be observing any races,
        //       hence we should not need to pass down the entire tree, just equality (I think).
        final INode<K, V> r = RDCSS_READ_ROOT();
        final boolean success = r.rec_insert(k, v, hc, 0, null, this);
        Verify.verify(success, "Concurrent modification during serialization of map %s", this);
    }

    private Optional<V> insertifhc(final K k, final int hc, final V v, final Object cond) {
        Optional<V> res;
        do {
            // Keep looping as long as we do not get a reply
            res = RDCSS_READ_ROOT().rec_insertif(k, v, hc, cond, 0, null, this);
        } while (res == null);

        return res;
    }

    private V lookuphc(final K k, final int hc) {
        Object res;
        do {
            // Keep looping as long as RESTART is being indicated
            res = RDCSS_READ_ROOT().rec_lookup(k, hc, 0, null, this);
        } while (res == RESTART);

        return (V) res;
    }

    private Optional<V> removehc(final K k, final V v, final int hc) {
        Optional<V> res;
        do {
            // Keep looping as long as we do not get a reply
            res = RDCSS_READ_ROOT().rec_remove(k, v, hc, 0, null, this);
        } while (res == null);

        return res;
    }

    /**
     * Ensure this instance is read-write, throw UnsupportedOperationException
     * otherwise. Used by Map-type methods for quick check.
     */
    private void ensureReadWrite() {
        if (readOnly) {
            throw new UnsupportedOperationException("Attempted to modify a read-only view");
        }
    }

    boolean isReadOnly() {
        return readOnly;
    }

    /* public methods */

    /**
     * Returns a snapshot of this TrieMap. This operation is lock-free and
     * linearizable.
     *
     * The snapshot is lazily updated - the first time some branch in the
     * snapshot or this TrieMap are accessed, they are rewritten. This means
     * that the work of rebuilding both the snapshot and this TrieMap is
     * distributed across all the threads doing updates or accesses subsequent
     * to the snapshot creation.
     */
    public TrieMap<K, V> snapshot() {
        while (true) {
            final INode<K, V> r = RDCSS_READ_ROOT();
            final MainNode<K, V> expmain = r.gcasRead(this);
            if (RDCSS_ROOT(r, expmain, r.copyToGen(new Gen(), this))) {
                return new TrieMap<> (r.copyToGen(new Gen(), this), equiv, readOnly);
            }

            // Tail recursion: return snapshot();
        }
    }

    /**
     * Returns a read-only snapshot of this TrieMap. This operation is lock-free
     * and linearizable.
     *
     * The snapshot is lazily updated - the first time some branch of this
     * TrieMap are accessed, it is rewritten. The work of creating the snapshot
     * is thus distributed across subsequent updates and accesses on this
     * TrieMap by all threads. Note that the snapshot itself is never rewritten
     * unlike when calling the `snapshot` method, but the obtained snapshot
     * cannot be modified.
     *
     * This method is used by other methods such as `size` and `iterator`.
     */
    public TrieMap<K, V> readOnlySnapshot() {
        // Is it a snapshot of a read-only snapshot?
        if (readOnly) {
            return this;
        }

        while (true) {
            final INode<K, V> r = RDCSS_READ_ROOT();
            final MainNode<K, V> expmain = r.gcasRead(this);
            if (RDCSS_ROOT(r, expmain, r.copyToGen (new Gen(), this))) {
                return new TrieMap<>(r, equiv, true);
            }

            // Tail recursion: return readOnlySnapshot();
        }
    }

    @Override
    public void clear() {
        boolean success;
        do {
            final INode<K, V> r = RDCSS_READ_ROOT();
            success = RDCSS_ROOT(r, r.gcasRead(this), newRootNode());
        } while (!success);
    }

    int computeHash(final K k) {
        return equiv.hash(k);
    }

    boolean equal(final K k1, final K k2) {
        return equiv.equivalent(k1, k2);
    }

    @Override
    public V get(final Object key) {
        final K k = (K) checkNotNull(key);
        return lookuphc(k, computeHash(k));
    }

    @Override
    public V put(final K key, final V value) {
        ensureReadWrite();
        final K k = checkNotNull(key);
        return insertifhc(k, computeHash(k), checkNotNull(value), null).orElse(null);
    }

    void add(final K key, final V value) {
        final K k = checkNotNull(key);
        inserthc(k, computeHash(k), checkNotNull(value));
    }

    @Override
    public V remove(final Object key) {
        ensureReadWrite();
        final K k = (K) checkNotNull(key);
        return removehc(k, (V) null, computeHash(k)).orElse(null);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        ensureReadWrite();
        final K k = checkNotNull(key);
        return insertifhc(k, computeHash(k), checkNotNull(value), ABSENT).orElse(null);
    }

    @Override
    public boolean remove(final Object key, final Object v) {
        ensureReadWrite();
        final K k = (K) checkNotNull(key);
        return removehc(k, (V) checkNotNull(v), computeHash(k)).isPresent();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        ensureReadWrite();
        final K k = checkNotNull(key);
        return insertifhc(k, computeHash(k), checkNotNull(newValue), checkNotNull(oldValue)).isPresent();
    }

    @Override
    public V replace(final K key, final V value) {
        ensureReadWrite();
        final K k = checkNotNull(key);
        return insertifhc (k, computeHash(k), checkNotNull(value), PRESENT).orElse(null);
    }

    /***
     * Return an iterator over a TrieMap.
     *
     * If this is a read-only snapshot, it would return a read-only iterator.
     *
     * If it is the original TrieMap or a non-readonly snapshot, it would return
     * an iterator that would allow for updates.
     *
     * @return
     */
    Iterator<Entry<K, V>> iterator() {
        return readOnly ? new TrieMapReadOnlyIterator<>(0, this) : new TrieMapIterator<>(0, this);
    }

    /***
     * Return an iterator over a TrieMap.
     * This is a read-only iterator.
     *
     * @return
     */
    Iterator<Entry<K, V>> readOnlyIterator() {
        return new TrieMapReadOnlyIterator<>(0, readOnly ? this : readOnlySnapshot());
    }

    private int cachedSize() {
        INode<K, V> r = RDCSS_READ_ROOT ();
        return r.cachedSize (this);
    }

    @Override
    public int size() {
        return readOnly ? cachedSize() : readOnlySnapshot().size();
    }

    @Override
    public boolean containsKey(final Object key) {
        return get(key) != null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new ExternalForm(this);
    }

    private static final class RDCSS_Descriptor<K, V> {
        INode<K, V> old;
        MainNode<K, V> expectedmain;
        INode<K, V> nv;
        volatile boolean committed = false;

        RDCSS_Descriptor (final INode<K, V> old, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
            this.old = old;
            this.expectedmain = expectedmain;
            this.nv = nv;
        }
    }

    /***
     * This iterator is a read-only one and does not allow for any update
     * operations on the underlying data structure.
     *
     * @param <K>
     * @param <V>
     */
    private static final class TrieMapReadOnlyIterator<K, V> extends TrieMapIterator<K, V> {
        TrieMapReadOnlyIterator (final int level, final TrieMap<K, V> ct, final boolean mustInit) {
            super (level, ct, mustInit);
        }

        TrieMapReadOnlyIterator (final int level, final TrieMap<K, V> ct) {
            this (level, ct, true);
        }
        @Override
        void initialize () {
            assert (ct.isReadOnly ());
            super.initialize ();
        }

        @Override
        public void remove () {
            throw new UnsupportedOperationException ("Operation not supported for read-only iterators");
        }

        @Override
        Entry<K, V> nextEntry(final Entry<K, V> rr) {
            // Return non-updatable entry
            return rr;
        }
    }

    private static class TrieMapIterator<K, V> implements Iterator<Entry<K, V>> {
        private int level;
        protected TrieMap<K, V> ct;
        private final boolean mustInit;
        private final BasicNode[][] stack = new BasicNode[7][];
        private final int[] stackpos = new int[7];
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

            Entry<K, V> r = null;
            if (subiter != null) {
                r = subiter.next ();
                checkSubiter ();
            } else {
                r = current;
                advance ();
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
//            assert (ct.isReadOnly ());
            INode<K, V> r = ct.RDCSS_READ_ROOT ();
            readin (r);
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

        protected TrieMapIterator<K, V> newIterator (final int _lev, final TrieMap<K, V> _ct, final boolean _mustInit) {
            return new TrieMapIterator<> (_lev, _ct, _mustInit);
        }

        protected void dupTo (final TrieMapIterator<K, V> it) {
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

        void printDebug () {
            System.out.println ("ctrie iterator");
            System.out.println (Arrays.toString (stackpos));
            System.out.println ("depth: " + depth);
            System.out.println ("curr.: " + current);
            // System.out.println(stack.mkString("\n"));
        }

        @Override
        public void remove() {
            checkState(lastReturned != null);
            ct.remove(lastReturned.getKey());
            lastReturned = null;
        }
    }

    /***
     * Support for EntrySet operations required by the Map interface
     */
    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return TrieMap.this.iterator ();
        }

        @Override
        public final boolean contains(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            final Entry<?, ?> e = (Entry<?, ?>) o;
            if (e.getKey() == null) {
                return false;
            }
            final V v = get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        @Override
        public final boolean remove(final Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            final Entry<?, ?> e = (Entry<K, V>) o;
            final Object key = e.getKey();
            if (key == null) {
                return false;
            }
            final Object value = e.getValue();
            if (value == null) {
                return false;
            }

            return TrieMap.this.remove(key, value);
        }

        @Override
        public final int size () {
            return Iterators.size(iterator());
        }

        @Override
        public final void clear () {
            TrieMap.this.clear ();
        }
    }
}

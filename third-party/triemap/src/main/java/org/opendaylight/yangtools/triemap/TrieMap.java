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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/***
 * This is a port of Scala's TrieMap class from the Scala Collections library.
 *
 * @author Roman Levenstein <romixlev@gmail.com>
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TrieMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K,V>, Serializable {
    private static final AtomicReferenceFieldUpdater<TrieMap, Object> ROOT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(TrieMap.class, Object.class, "root");
    private static final long serialVersionUID = 1L;
    private static final Field READONLY_FIELD;
    private static final TrieMap EMPTY = new TrieMap();

    static {
        final Field f;
        try {
            f = TrieMap.class.getDeclaredField("readOnly");
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        } catch (SecurityException e) {
            throw new ExceptionInInitializerError(e);
        }
        f.setAccessible(true);
        READONLY_FIELD = f;
    }

    /**
     * EntrySet
     */
    private transient final EntrySet entrySet = new EntrySet ();

    public static <K,V> TrieMap<K,V> empty () {
        return EMPTY;
    }

    // static class MangledHashing<K> extends Hashing<K> {
    // int hash(K k) {
    // return util.hashing.byteswap32(k);
    // }
    // }

    private static class RDCSS_Descriptor<K, V> {
        INode<K, V> old;
        MainNode<K, V> expectedmain;
        INode<K, V> nv;
        volatile boolean committed = false;

        public RDCSS_Descriptor (final INode<K, V> old, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
            this.old = old;
            this.expectedmain = expectedmain;
            this.nv = nv;
        }
    }

    private final Hashing<K> hashingobj;
    private final Equiv<K> equalityobj;

    Hashing<K> hashing () {
        return hashingobj;
    }

    Equiv<K> equality () {
        return equalityobj;
    }

    private transient volatile Object root;
    private final transient boolean readOnly;

    TrieMap (final Hashing<K> hashf, final Equiv<K> ef, final boolean readOnly) {
        this.hashingobj = hashf;
        this.equalityobj = ef;
        this.readOnly = readOnly;
    }

    TrieMap (final Object r, final Hashing<K> hashf, final Equiv<K> ef, final boolean readOnly) {
        this(hashf, ef, readOnly);
        this.root = r;
    }

    public TrieMap (final Hashing<K> hashf, final Equiv<K> ef) {
        this(newRootNode(), hashf, ef, false);
    }

    public TrieMap () {
        this (new Hashing.Default<K>(), Equiv.universal);
    }

    /* internal methods */

    // private void writeObject(java.io.ObjectOutputStream out) {
    // out.writeObject(hashf);
    // out.writeObject(ef);
    //
    // Iterator it = iterator();
    // while (it.hasNext) {
    // val (k, v) = it.next();
    // out.writeObject(k);
    // out.writeObject(v);
    // }
    // out.writeObject(TrieMapSerializationEnd);
    // }
    //
    // private TrieMap readObject(java.io.ObjectInputStream in) {
    // root = INode.newRootNode();
    // rootupdater = AtomicReferenceFieldUpdater.newUpdater(TrieMap.class,
    // Object.class, "root");
    //
    // hashingobj = in.readObject();
    // equalityobj = in.readObject();
    //
    // Object obj = null;
    // do {
    // obj = in.readObject();
    // if (obj != TrieMapSerializationEnd) {
    // K k = (K)obj;
    // V = (V)in.readObject();
    // update(k, v);
    // }
    // } while (obj != TrieMapSerializationEnd);
    // }

    private static <K,V> INode<K,V> newRootNode() {
        final Gen gen = new Gen();
        return new INode<>(gen, new CNode<>(gen));
    }

    final boolean CAS_ROOT (final Object ov, final Object nv) {
        if (isReadOnly()) {
            throw new IllegalStateException("Attempted to modify a read-only snapshot");
        }
        return ROOT_UPDATER.compareAndSet (this, ov, nv);
    }

    // FIXME: abort = false by default
    final INode<K, V> readRoot (final boolean abort) {
        return RDCSS_READ_ROOT (abort);
    }

    final INode<K, V> readRoot () {
        return RDCSS_READ_ROOT (false);
    }

    final INode<K, V> RDCSS_READ_ROOT () {
        return RDCSS_READ_ROOT (false);
    }

    final INode<K, V> RDCSS_READ_ROOT (final boolean abort) {
        Object r = /* READ */root;
        if (r instanceof INode) {
            return (INode<K, V>) r;
        } else if (r instanceof RDCSS_Descriptor) {
            return RDCSS_Complete (abort);
        }
        throw new RuntimeException ("Should not happen");
    }

    private final INode<K, V> RDCSS_Complete (final boolean abort) {
        while (true) {
            Object v = /* READ */root;
            if (v instanceof INode) {
                return (INode<K, V>) v;
            } else if (v instanceof RDCSS_Descriptor) {
                RDCSS_Descriptor<K, V> desc = (RDCSS_Descriptor<K, V>) v;
                INode<K, V> ov = desc.old;
                MainNode<K, V> exp = desc.expectedmain;
                INode<K, V> nv = desc.nv;

                if (abort) {
                    if (CAS_ROOT (desc, ov)) {
                        return ov;
                    } else {
                        // return RDCSS_Complete (abort);
                        // tailrec
                        continue;
                    }
                } else {
                    MainNode<K, V> oldmain = ov.gcasRead (this);
                    if (oldmain == exp) {
                        if (CAS_ROOT (desc, nv)) {
                            desc.committed = true;
                            return nv;
                        } else {
                            // return RDCSS_Complete (abort);
                            // tailrec
                            continue;
                        }
                    } else {
                        if (CAS_ROOT (desc, ov)) {
                            return ov;
                        } else {
                            // return RDCSS_Complete (abort);
                            // tailrec
                            continue;

                        }
                    }
                }
            }

            throw new RuntimeException ("Should not happen");
        }
    }

    private boolean RDCSS_ROOT (final INode<K, V> ov, final MainNode<K, V> expectedmain, final INode<K, V> nv) {
        RDCSS_Descriptor<K, V> desc = new RDCSS_Descriptor<> (ov, expectedmain, nv);
        if (CAS_ROOT (ov, desc)) {
            RDCSS_Complete (false);
            return /* READ */desc.committed;
        } else {
            return false;
        }
    }

    private void inserthc (final K k, final int hc, final V v) {
        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();
            if (!r.rec_insert (k, v, hc, 0, null, r.gen, this)) {
                // inserthc (k, hc, v);
                // tailrec
                continue;
            }
            break;
        }
    }

    private Option<V> insertifhc (final K k, final int hc, final V v, final Object cond) {
        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();

            Option<V> ret = r.rec_insertif (k, v, hc, cond, 0, null, r.gen, this);
            if (ret == null) {
                // return insertifhc (k, hc, v, cond);
                // tailrec
                continue;
            } else {
                return ret;
            }
        }
    }

    private Object lookuphc(final K k, final int hc) {
        while (true) {
            final INode<K, V> r = RDCSS_READ_ROOT ();
            final Object res = r.rec_lookup(k, hc, 0, null, r.gen, this);
            if (!INode.RESTART.equals(res)) {
                return res;
            }

            // Tail recursion: lookuphc(k, hc)
        }
    }

    private Option<V> removehc (final K k, final V v, final int hc) {
        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();
            Option<V> res = r.rec_remove (k, v, hc, 0, null, r.gen, this);
            if (res != null) {
                return res;
            } else {
                // return removehc (k, v, hc);
                // tailrec
                continue;
            }
        }
    }

    /**
     * Ensure this instance is read-write, throw UnsupportedOperationException
     * otherwise. Used by Map-type methods for quick check.
     */
    private void ensureReadWrite() {
        if (isReadOnly()) {
            throw new UnsupportedOperationException("Attempted to modify a read-only view");
        }
    }

    public String string () {
        // RDCSS_READ_ROOT().string(0);
        return "Root";
    }

    /* public methods */

    // public Seq<V> seq() {
    // return this;
    // }

    // override def par = new ParTrieMap(this)

    // static TrieMap empty() {
    // return new TrieMap();
    // }

    final boolean isReadOnly () {
        return readOnly;
    }

    final boolean nonReadOnly () {
        return !readOnly;
    }

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

    final public TrieMap<K, V> snapshot () {
        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();
            final MainNode<K, V> expmain = r.gcasRead (this);
            if (RDCSS_ROOT (r, expmain, r.copyToGen (new Gen (), this))) {
                return new TrieMap<> (r.copyToGen (new Gen (), this), hashing (), equality (), readOnly);
            } else {
                // return snapshot ();
                // tailrec
                continue;
            }
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
    final public TrieMap<K, V> readOnlySnapshot () {
        // Is it a snapshot of a read-only snapshot?
        if(!nonReadOnly ()) {
            return this;
        }

        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();
            MainNode<K, V> expmain = r.gcasRead (this);
            if (RDCSS_ROOT (r, expmain, r.copyToGen (new Gen (), this))) {
                return new TrieMap<> (r, hashing (), equality (), true);
            } else {
                // return readOnlySnapshot ();
                continue;
            }
        }
    }

    @Override
    final public void clear () {
        while (true) {
            INode<K, V> r = RDCSS_READ_ROOT ();
            if (!RDCSS_ROOT(r, r.gcasRead(this), newRootNode())) {
                continue;
            }else{
                return;
            }
        }
    }

    // @inline
    int computeHash (final K k) {
        return hashingobj.hash (k);
    }

    final V lookup (final K k) {
        int hc = computeHash (k);
//        return (V) lookuphc (k, hc);
        Object o = lookuphc (k, hc);
        if(o instanceof Some) {
            return ((Some<V>)o).get ();
        } else if(o instanceof None) {
            return null;
        } else {
            return (V)o;
        }
    }

    final V apply (final K k) {
        int hc = computeHash (k);
        Object res = lookuphc (k, hc);
        if (res == null) {
            throw new NoSuchElementException ();
        } else {
            return (V) res;
        }
    }

//    final public Option<V> get (K k) {
//        int hc = computeHash (k);
//        return Option.makeOption ((V)lookuphc (k, hc));
//    }

    @Override
    final public V get (final Object k) {
        return lookup((K)k);
    }

    final public Option<V> putOpt(final Object key, final Object value) {
        int hc = computeHash ((K)key);
        return insertifhc ((K)key, hc, (V)value, null);
    }

    @Override
    final public V put (final Object key, final Object value) {
        ensureReadWrite();
        int hc = computeHash ((K)key);
        Option<V> ov = insertifhc ((K)key, hc, (V)value, null);
        if(ov instanceof Some) {
            Some<V> sv = (Some<V>)ov;
            return sv.get ();
        } else {
            return null;
        }
    }

    final public void update (final K k, final V v) {
        int hc = computeHash (k);
        inserthc (k, hc, v);
    }

    final public TrieMap<K, V> add (final K k, final V v) {
        update (k, v);
        return this;
    }

    final Option<V> removeOpt (final K k) {
        int hc = computeHash (k);
        return removehc (k, (V) null, hc);
    }

    @Override
    final public V remove (final Object k) {
        ensureReadWrite();
        int hc = computeHash ((K)k);
        Option<V> ov = removehc ((K)k, (V) null, hc);
        if(ov instanceof Some) {
            Some<V> sv = (Some<V>)ov;
            return sv.get();
        } else {
            return null;
        }
    }

//    final public TrieMap<K, V> remove (Object k) {
//        removeOpt ((K)k);
//        return this;
//    }

    final public Option<V> putIfAbsentOpt (final K k, final V v) {
        int hc = computeHash (k);
        return insertifhc (k, hc, v, INode.KEY_ABSENT);
    }

    @Override
    final public V putIfAbsent (final Object k, final Object v) {
        ensureReadWrite();
        int hc = computeHash ((K)k);
        Option<V> ov = insertifhc ((K)k, hc, (V)v, INode.KEY_ABSENT);
        if(ov instanceof Some) {
            Some<V> sv = (Some<V>)ov;
            return sv.get();
        } else {
            return null;
        }
    }

    @Override
    public boolean remove (final Object k, final Object v) {
        ensureReadWrite();
        int hc = computeHash ((K)k);
        return removehc ((K)k, (V)v, hc).nonEmpty ();
    }

    @Override
    public boolean replace (final K k, final V oldvalue, final V newvalue) {
        ensureReadWrite();
        int hc = computeHash (k);
        return insertifhc (k, hc, newvalue, oldvalue).nonEmpty ();
    }

    public Option<V> replaceOpt (final K k, final V v) {
        int hc = computeHash (k);
        return insertifhc (k, hc, v, INode.KEY_PRESENT);
    }

    @Override
    public V replace (final Object k, final Object v) {
        ensureReadWrite();
        int hc = computeHash ((K)k);
        Option<V> ov = insertifhc ((K)k, hc, (V)v, INode.KEY_PRESENT);
        if(ov instanceof Some) {
            Some<V> sv = (Some<V>)ov;
            return sv.get();
        } else {
            return null;
        }
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
    public Iterator<Map.Entry<K, V>> iterator () {
        if (!nonReadOnly ()) {
            return readOnlySnapshot ().readOnlyIterator ();
        } else {
            return new TrieMapIterator<> (0, this);
        }
    }

    /***
     * Return an iterator over a TrieMap.
     * This is a read-only iterator.
     *
     * @return
     */
    public Iterator<Map.Entry<K, V>> readOnlyIterator () {
        if (nonReadOnly ()) {
            return readOnlySnapshot ().readOnlyIterator ();
        } else {
            return new TrieMapReadOnlyIterator<> (0, this);
        }
    }

    private int cachedSize () {
        INode<K, V> r = RDCSS_READ_ROOT ();
        return r.cachedSize (this);
    }

    @Override
    public int size () {
        if (nonReadOnly ()) {
            return readOnlySnapshot ().size ();
        } else {
            return cachedSize ();
        }
    }

    String stringPrefix () {
        return "TrieMap";
    }

    /***
     * This iterator is a read-only one and does not allow for any update
     * operations on the underlying data structure.
     *
     * @param <K>
     * @param <V>
     */
    private static class TrieMapReadOnlyIterator<K, V> extends TrieMapIterator<K, V> {
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
        Map.Entry<K, V> nextEntry(final Map.Entry<K, V> rr) {
            // Return non-updatable entry
            return rr;
        }
    }

    private static class TrieMapIterator<K, V> implements java.util.Iterator<Map.Entry<K, V>> {
        private int level;
        protected TrieMap<K, V> ct;
        private final boolean mustInit;
        private final BasicNode[][] stack = new BasicNode[7][];
        private final int[] stackpos = new int[7];
        private int depth = -1;
        private Iterator<Map.Entry<K, V>> subiter = null;
        private KVNode<K, V> current = null;
        private Map.Entry<K, V> lastReturned = null;

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
        public boolean hasNext () {
            return (current != null) || (subiter != null);
        }

        @Override
        public Map.Entry<K, V> next () {
            if (hasNext ()) {
                Map.Entry<K, V> r = null;
                if (subiter != null) {
                    r = subiter.next ();
                    checkSubiter ();
                } else {
                    r = current.kvPair ();
                    advance ();
                }

                lastReturned = r;
                if (r != null) {
                    final Map.Entry<K, V> rr = r;
                    return nextEntry(rr);
                }
                return r;
            } else {
                // return Iterator.empty ().next ();
                return null;
            }
        }

        Map.Entry<K, V> nextEntry(final Map.Entry<K, V> rr) {
            return new Map.Entry<K, V>() {
                private V updated = null;

                @Override
                public K getKey () {
                    return rr.getKey ();
                }

                @Override
                public V getValue () {
                    return (updated == null)?rr.getValue (): updated;
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
                List<Map.Entry<K, V>> lst = toList (this.subiter);
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
                list.add (it.next ());
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
        public void remove () {
            if (lastReturned != null) {
                ct.remove (lastReturned.getKey ());
                lastReturned = null;
            } else {
                throw new IllegalStateException();
            }
        }

    }

    /** Only used for ctrie serialization. */
    // @SerialVersionUID(0L - 7237891413820527142L)
    private static long TrieMapSerializationEnd = 0L - 7237891413820527142L;


    @Override
    public boolean containsKey (final Object key) {
        return lookup ((K) key) != null;
    }


    @Override
    public Set<Map.Entry<K, V>> entrySet () {
        return entrySet;
    }

    /***
     * Support for EntrySet operations required by the Map interface
     *
     */
    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator () {
            return TrieMap.this.iterator ();
        }

        @Override
        public final boolean contains (final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            final K k = e.getKey ();
            final V v = lookup (k);
            return v != null;
        }

        @Override
        public final boolean remove (final Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            final K k = e.getKey ();
            return null != TrieMap.this.remove (k);
        }

        @Override
        public final int size () {
            int size = 0;
            for (final Iterator<?> i = iterator (); i.hasNext (); i.next ()) {
                size++;
            }
            return size;
        }

        @Override
        public final void clear () {
            TrieMap.this.clear ();
        }
    }

    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        this.root = newRootNode();

        final boolean ro = inputStream.readBoolean();
        final int size = inputStream.readInt();
        for (int i = 0; i < size; ++i) {
            final K key = (K)inputStream.readObject();
            final V value = (V)inputStream.readObject();
            add(key, value);
        }

        // Propagate the read-only bit
        try {
            READONLY_FIELD.setBoolean(this, ro);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to set read-only flag", e);
        }
    }

    private void writeObject(final ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();

        final Map<K, V> ro = readOnlySnapshot();
        outputStream.writeBoolean(isReadOnly());
        outputStream.writeInt(ro.size());

        for (Entry<K, V> e : ro.entrySet()) {
            outputStream.writeObject(e.getKey());
            outputStream.writeObject(e.getValue());
        }
    }
}

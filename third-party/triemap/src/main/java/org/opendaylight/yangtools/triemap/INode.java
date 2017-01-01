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

import java.util.Optional;

final class INode<K, V> extends INodeBase<K, V> {
    static final Object KEY_PRESENT = new Object ();
    static final Object KEY_ABSENT = new Object ();

    /**
     * Virtual result for lookup methods indicating that the lookup needs to be restarted. This is a faster version
     * of throwing a checked exception to control the restart.
     */
    static final Object RESTART = new Object();

    INode(final Gen gen, final MainNode<K, V> bn) {
        super(gen, bn);
    }

    MainNode<K, V> gcasRead(final TrieMap<K, V> ct) {
        return GCAS_READ(ct);
    }

    MainNode<K, V> GCAS_READ(final TrieMap<K, V> ct) {
        MainNode<K, V> m = /* READ */ READ();
        MainNode<K, V> prevval = /* READ */ m.READ_PREV();
        if (prevval == null) {
            return m;
        }

        return GCAS_Complete(m, ct);
    }

    private MainNode<K, V> GCAS_Complete(MainNode<K, V> m, final TrieMap<K, V> ct) {
        while (true) {
            if (m == null) {
                return null;
            }

            // complete the GCAS
            final MainNode<K, V> prev = /* READ */ m.READ_PREV();
            final INode<K, V> ctr = ct.readRoot(true);
            if (prev == null) {
                return m;
            }

            if (prev instanceof FailedNode) {
                // try to commit to previous value
                FailedNode<K, V> fn = (FailedNode<K, V>) prev;
                if (CAS(m, fn.READ_PREV())) {
                    return fn.READ_PREV();
                }

                // Tail recursion: return GCAS_Complete (/* READ */ READ(), ct);
                m = /* READ */ READ();
                continue;
            }

            // Assume that you've read the root from the generation G.
            // Assume that the snapshot algorithm is correct.
            // ==> you can only reach nodes in generations <= G.
            // ==> `gen` is <= G.
            // We know that `ctr.gen` is >= G.
            // ==> if `ctr.gen` = `gen` then they are both equal to G.
            // ==> otherwise, we know that either `ctr.gen` > G, `gen` < G,
            // or both
            if ((ctr.gen == gen) && ct.nonReadOnly()) {
                // try to commit
                if (m.CAS_PREV(prev, null)) {
                    return m;
                }

                // Tail recursion: return GCAS_Complete (m, ct);
                continue;
            }

            // try to abort
            m.CAS_PREV(prev, new FailedNode<>(prev));

            // Tail recursion: return GCAS_Complete(/* READ */ READ(), ct);
            m = /* READ */ READ();
        }
    }

    private boolean GCAS(final MainNode<K, V> old, final MainNode<K, V> n, final TrieMap<K, V> ct) {
        n.WRITE_PREV(old);
        if (CAS(old, n)) {
            GCAS_Complete(n, ct);
            return /* READ */ n.READ_PREV() == null;
        }

        return false;
    }

    private INode<K, V> inode(final MainNode<K, V> cn) {
        return new INode<>(gen, cn);
    }

    INode<K, V> copyToGen(final Gen ngen, final TrieMap<K, V> ct) {
        return new INode<>(ngen, GCAS_READ(ct));
    }

    /**
     * Inserts a key value pair, overwriting the old pair if the keys match.
     *
     * @return true if successful, false otherwise
     */
    boolean rec_insert(final K k, final V v, final int hc, final int lev, final INode<K, V> parent, final Gen startgen,
            final TrieMap<K, V> ct) {
        while (true) {
            final MainNode<K, V> m = GCAS_READ (ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multiway node
                final CNode<K, V> cn = (CNode<K, V>) m;
                final int idx = (hc >>> lev) & 0x1f;
                final int flag = 1 << idx;
                final int bmp = cn.bitmap;
                final int mask = flag - 1;
                final int pos = Integer.bitCount(bmp & mask);
                if ((bmp & flag) != 0) {
                    // 1a) insert below
                    final BasicNode cnAtPos = cn.array[pos];
                    if (cnAtPos instanceof INode) {
                        final INode<K, V> in = (INode<K, V>) cnAtPos;
                        if (startgen == in.gen) {
                            return in.rec_insert(k, v, hc, lev + 5, this, startgen, ct);
                        }
                        if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                            // Tail recursion: return rec_insert (k, v, hc, lev, parent, startgen, ct);
                            continue;
                        }

                        return false;
                    } else if (cnAtPos instanceof SNode) {
                        final SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (sn.hc == hc && ct.equal(sn.k, k)) {
                            return GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct);
                        }

                        final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                        final MainNode<K, V> nn = rn.updatedAt(pos, inode(CNode.dual(sn, sn.hc, new SNode<>(k, v, hc),
                                hc, lev + 5, gen)), gen);
                        return GCAS (cn, nn, ct);
                    }
                } else {
                    CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                    MainNode<K, V> ncnode = rn.insertedAt(pos, flag, new SNode<> (k, v, hc), gen);
                    return GCAS (cn, ncnode, ct);
                }
            } else if (m instanceof TNode) {
                clean(parent, ct, lev - 5);
                return false;
            } else if (m instanceof LNode) {
                LNode<K, V> ln = (LNode<K, V>) m;
                MainNode<K, V> nn = ln.inserted(k, v);
                return GCAS(ln, nn, ct);
            } else {
                throw new IllegalStateException("Unhandled node " + m);
            }

            throw new RuntimeException ("Should not happen");
        }
    }

    /**
     * Inserts a new key value pair, given that a specific condition is met.
     *
     * @param cond
     *            null - don't care if the key was there
     *            KEY_ABSENT - key wasn't there
     *            KEY_PRESENT - key was there
     *            other value `v` - key must be bound to `v`
     * @return null if unsuccessful, Option[V] otherwise (indicating
     *         previous value bound to the key)
     */
    Optional<V> rec_insertif(final K k, final V v, final int hc, final Object cond, final int lev,
            final INode<K, V> parent, final Gen startgen, final TrieMap<K, V> ct) {
        while (true) {
            final MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multiway node
                final CNode<K, V> cn = (CNode<K, V>) m;
                final int idx = (hc >>> lev) & 0x1f;
                final int flag = 1 << idx;
                final int bmp = cn.bitmap;
                final int mask = flag - 1;
                final int pos = Integer.bitCount(bmp & mask);

                if ((bmp & flag) != 0) {
                    // 1a) insert below
                    final BasicNode cnAtPos = cn.array[pos];
                    if (cnAtPos instanceof INode) {
                        final INode<K, V> in = (INode<K, V>) cnAtPos;
                        if (startgen == in.gen) {
                            return in.rec_insertif(k, v, hc, cond, lev + 5, this, startgen, ct);
                        }

                        if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                            // Tail recursion: return rec_insertif (k, v, hc, cond, lev, parent, startgen, ct);
                            continue;
                        }

                        return null;
                    } else if (cnAtPos instanceof SNode) {
                        final SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (cond == null) {
                            if (sn.hc == hc && ct.equal(sn.k, k)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.v);
                                }

                                return null;
                            }

                            final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                            final MainNode<K, V> nn = rn.updatedAt(pos, inode (CNode.dual(sn, sn.hc,
                                    new SNode<>(k, v, hc), hc, lev + 5, gen)), gen);
                            if (GCAS(cn, nn, ct)) {
                                return Optional.empty();
                            }

                            return null;
                        } else if (cond == INode.KEY_ABSENT) {
                            if (sn.hc == hc && ct.equal(sn.k, k)) {
                                return Optional.of(sn.v);
                            }

                            final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                            final MainNode<K, V> nn = rn.updatedAt(pos, inode (CNode.dual(sn, sn.hc,
                                new SNode<>(k, v, hc), hc, lev + 5, gen)), gen);
                            if (GCAS(cn, nn, ct)) {
                                return Optional.empty();
                            }

                            return null;
                        } else if (cond == INode.KEY_PRESENT) {
                            if (sn.hc == hc && ct.equal(sn.k, k)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.v);
                                }
                                return null;
                            }

                            return Optional.empty();
                        } else {
                            if (sn.hc == hc && ct.equal(sn.k, k) && cond.equals(sn.v)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.v);
                                }

                                return null;
                            }

                            return Optional.empty();
                        }
                    }
                } else if (cond == null || cond == INode.KEY_ABSENT) {
                    final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                    final CNode<K, V> ncnode = rn.insertedAt (pos, flag, new SNode<>(k, v, hc), gen);
                    if (GCAS(cn, ncnode, ct)) {
                        return Optional.empty();
                    }

                    return null;
                } else if (cond == INode.KEY_PRESENT) {
                    return Optional.empty();
                } else {
                    return Optional.empty();
                }
            } else if (m instanceof TNode) {
                clean(parent, ct, lev - 5);
                return null;
            } else if (m instanceof LNode) {
                // 3) an l-node
                final LNode<K, V> ln = (LNode<K, V>) m;
                if (cond == null) {
                    final Optional<V> optv = ln.get(k);
                    if (insertln(ln, k, v, ct)) {
                        return optv;
                    }
                    return null;
                } else if (cond == INode.KEY_ABSENT) {
                    final Optional<V> t = ln.get(k);
                    if (t.isPresent()) {
                        return t;
                    }
                    if (insertln(ln, k, v, ct)) {
                        return Optional.empty();
                    }
                    return null;
                } else if (cond == INode.KEY_PRESENT) {
                    final Optional<V> t = ln.get(k);
                    if (!t.isPresent()) {
                        return t;
                    }
                    if (insertln(ln, k, v, ct)) {
                        return t;
                    }
                    return null;
                } else {
                    final Optional<V> t = ln.get(k);
                    if (t.isPresent()) {
                        if (cond.equals(t.get())) {
                            if (insertln(ln, k, v, ct)) {
                                // Difference from Scala: we choose to reuse the object returned from LNode,
                                // as the identity of the value does not matter in this call graph.
                                return t;
                            }

                            return null;
                        }
                    }

                    return Optional.empty();
                }
            } else {
                throw new IllegalStateException("Unhandled node " + m);
            }

            throw new RuntimeException("Should never happen");
        }
    }

    boolean insertln(final LNode<K, V> ln, final K k, final V v, final TrieMap<K, V> ct) {
        final LNode<K, V> nn = ln.inserted (k, v);
        return GCAS(ln, nn, ct);
    }

    /**
     * Looks up the value associated with the key.
     *
     * @return null if no value has been found, RESTART if the operation
     *         wasn't successful, or any other value otherwise
     */
    Object rec_lookup(final K k, final int hc, final int lev, final INode<K, V> parent, final Gen startgen,
            final TrieMap<K, V> ct) {
        while (true) {
            final MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multinode
                final CNode<K, V> cn = (CNode<K, V>) m;
                final int idx = (hc >>> lev) & 0x1f;
                final int flag = 1 << idx;
                final int bmp = cn.bitmap;
                if ((bmp & flag) == 0) {
                    // 1a) bitmap shows no binding
                    return null;
                }

                // 1b) bitmap contains a value - descend
                final int pos = (bmp == 0xffffffff) ? idx : Integer.bitCount(bmp & (flag - 1));
                final BasicNode sub = cn.array[pos];
                if (sub instanceof INode) {
                    final INode<K, V> in = (INode<K, V>) sub;
                    if (ct.isReadOnly() || (startgen == in.gen)) {
                        return in.rec_lookup(k, hc, lev + 5, this, startgen, ct);
                    }

                    if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                        // Tail recursion: return rec_lookup(k, hc, lev, parent, startgen, ct);
                        continue;
                    }

                    // used to be throw RestartException
                    return RESTART;
                } else if (sub instanceof SNode) {
                    // 2) singleton node
                    final SNode<K, V> sn = (SNode<K, V>) sub;
                    if (sn.hc == hc && ct.equal(sn.k, k)) {
                        return sn.v;
                    }

                    return null;
                }
            } else if (m instanceof TNode) {
                // 3) non-live node
                return cleanReadOnly((TNode<K, V>) m, lev, parent, ct, k, hc);
            } else if (m instanceof LNode) {
                // 5) an l-node
                return ((LNode<K, V>) m).get(k).orElse(null);
            } else {
                throw new IllegalStateException("Unhandled node " + m);
            }

            throw new RuntimeException ("Should not happen");
        }
    }

    private Object cleanReadOnly(final TNode<K, V> tn, final int lev, final INode<K, V> parent,
            final TrieMap<K, V> ct, final K k, final int hc) {
        if (ct.nonReadOnly()) {
            // used to be throw RestartException
            clean(parent, ct, lev - 5);
            return RESTART;
        }

        if (tn.hc == hc && ct.equal(tn.k, k)) {
            return tn.v;
        }

        return null;
    }

    /**
     * Removes the key associated with the given value.
     *
     * @param v
     *            if null, will remove the key regardless of the value;
     *            otherwise removes only if binding contains that exact key
     *            and value
     * @return null if not successful, an Option[V] indicating the previous
     *         value otherwise
     */
    Optional<V> rec_remove(final K k, final V v, final int hc, final int lev, final INode<K, V> parent,
            final Gen startgen, final TrieMap<K, V> ct) {
        final MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

        if (m instanceof CNode) {
            final CNode<K, V> cn = (CNode<K, V>) m;
            final int idx = (hc >>> lev) & 0x1f;
            final int bmp = cn.bitmap;
            final int flag = 1 << idx;
            if ((bmp & flag) == 0) {
                return Optional.empty();
            }

            final int pos = Integer.bitCount(bmp & (flag - 1));
            final BasicNode sub = cn.array[pos];
            Optional<V> res = null;
            if (sub instanceof INode) {
                final INode<K, V> in = (INode<K, V>) sub;
                if (startgen == in.gen) {
                    res = in.rec_remove(k, v, hc, lev + 5, this, startgen, ct);
                } else {
                    if (GCAS(cn, cn.renewed (startgen, ct), ct)) {
                        res = rec_remove(k, v, hc, lev, parent, startgen, ct);
                    } else {
                        res = null;
                    }
                }

            } else if (sub instanceof SNode) {
                final SNode<K, V> sn = (SNode<K, V>) sub;
                if (sn.hc == hc && ct.equal(sn.k, k) && (v == null || v.equals(sn.v))) {
                    final MainNode<K, V> ncn = cn.removedAt(pos, flag, gen).toContracted(lev);
                    if (GCAS(cn, ncn, ct)) {
                        res = Optional.of(sn.v);
                    } else {
                        res = null;
                    }
                } else {
                    res = Optional.empty();
                }
            }

            if (res == null || !res.isPresent()) {
                return res;
            }

            if (parent != null) {
                // never tomb at root
                final MainNode<K, V> n = GCAS_READ(ct);
                if (n instanceof TNode) {
                    cleanParent(n, parent, ct, hc, lev, startgen);
                }
            }

            return res;
        } else if (m instanceof TNode) {
            clean(parent, ct, lev - 5);
            return null;
        } else if (m instanceof LNode) {
            final LNode<K, V> ln = (LNode<K, V>) m;
            if (v == null) {
                final Optional<V> optv = ln.get(k);
                final MainNode<K, V> nn = ln.removed(k, ct);
                if (GCAS(ln, nn, ct)) {
                    return optv;
                }

                return null;
            }

            final Optional<V> tmp = ln.get(k);
            if (tmp.isPresent() && v.equals(tmp.get())) {
                final MainNode<K, V> nn = ln.removed(k, ct);
                if (GCAS(ln, nn, ct)) {
                    return tmp;
                }

                return null;
            }

            // Key not found or value does not match: we have not removed anything
            return Optional.empty();
        } else {
            throw new IllegalStateException("Unhandled node " + m);
        }
    }

    private void cleanParent(final Object nonlive, final INode<K, V> parent, final TrieMap<K, V> ct, final int hc,
            final int lev, final Gen startgen) {
        while (true) {
            final MainNode<K, V> pm = parent.GCAS_READ(ct);
            if ((!(pm instanceof CNode))) {
                // parent is no longer a cnode, we're done
                return;
            }

            final CNode<K, V> cn = (CNode<K, V>) pm;
            final int idx = (hc >>> (lev - 5)) & 0x1f;
            final int bmp = cn.bitmap;
            final int flag = 1 << idx;
            if ((bmp & flag) == 0) {
                // somebody already removed this i-node, we're done
                return;
            }

            final int pos = Integer.bitCount(bmp & (flag - 1));
            final BasicNode sub = cn.array[pos];
            if (sub == this) {
                if (nonlive instanceof TNode) {
                    final TNode<K, V> tn = (TNode<K, V>) nonlive;
                    MainNode<K, V> ncn = cn.updatedAt(pos, tn.copyUntombed(), gen).toContracted(lev - 5);
                    if (!parent.GCAS(cn, ncn, ct)) {
                        if (ct.readRoot().gen == startgen) {
                            // Tail recursion: cleanParent(nonlive, parent, ct, hc, lev, startgen);
                            continue;
                        }
                    }
                }
            }
            break;
        }
    }

    private void clean(final INode<K, V> nd, final TrieMap<K, V> ct, final int lev) {
        final MainNode<K, V> m = nd.GCAS_READ(ct);
        if (m instanceof CNode) {
            final CNode<K, V> cn = (CNode<K, V>) m;
            nd.GCAS(cn, cn.toCompressed(ct, lev, gen), ct);
        }
    }

    int cachedSize(final TrieMap<K, V> ct) {
        MainNode<K, V> m = GCAS_READ(ct);
        return m.cachedSize(ct);
    }

    // /* this is a quiescent method! */
    // def string(lev: Int) = "%sINode -> %s".format("  " * lev, mainnode
    // match {
    // case null => "<null>"
    // case tn: TNode[_, _] => "TNode(%s, %s, %d, !)".format(tn.k, tn.v,
    // tn.hc)
    // case cn: CNode[_, _] => cn.string(lev)
    // case ln: LNode[_, _] => ln.string(lev)
    // case x => "<elem: %s>".format(x)
    // })

    @Override
    String string(final int lev) {
        return "INode";
    }
}
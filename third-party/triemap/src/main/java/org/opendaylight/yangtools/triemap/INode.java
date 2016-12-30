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
        } else {
            return GCAS_Complete(m, ct);
        }
    }

    private MainNode<K, V> GCAS_Complete(MainNode<K, V> m, final TrieMap<K, V> ct) {
        while (true) {
            if (m == null) {
                return null;
            } else {
                // complete the GCAS
                MainNode<K, V> prev = /* READ */ m.READ_PREV();
                INode<K, V> ctr = ct.readRoot(true);

                if (prev == null) {
                    return m;
                }

                if (prev instanceof FailedNode) {
                    // try to commit to previous value
                    FailedNode<K, V> fn = (FailedNode<K, V>) prev;
                    if (CAS(m, fn.READ_PREV())) {
                        return fn.READ_PREV();
                    } else {
                        // Tailrec
                        // return GCAS_Complete (/* READ */mainnode, ct);
                        m = /* READ */ READ();
                        continue;
                    }
                } else if (prev instanceof MainNode) {
                    // Assume that you've read the root from the generation
                    // G.
                    // Assume that the snapshot algorithm is correct.
                    // ==> you can only reach nodes in generations <= G.
                    // ==> `gen` is <= G.
                    // We know that `ctr.gen` is >= G.
                    // ==> if `ctr.gen` = `gen` then they are both equal to
                    // G.
                    // ==> otherwise, we know that either `ctr.gen` > G,
                    // `gen` <
                    // G,
                    // or both
                    if ((ctr.gen == gen) && ct.nonReadOnly()) {
                        // try to commit
                        if (m.CAS_PREV(prev, null)) {
                            return m;
                        } else {
                            // return GCAS_Complete (m, ct);
                            // tailrec
                            continue;
                        }
                    } else {
                        // try to abort
                        m.CAS_PREV(prev, new FailedNode<>(prev));
                        return GCAS_Complete(/* READ */ READ(), ct);
                    }
                }
            }
            throw new RuntimeException ("Should not happen");
        }
    }

    boolean GCAS(final MainNode<K, V> old, final MainNode<K, V> n, final TrieMap<K, V> ct) {
        n.WRITE_PREV (old);
        if (CAS (old, n)) {
            GCAS_Complete (n, ct);
            return /* READ */ n.READ_PREV() == null;
        } else {
            return false;
        }
    }

    private boolean equal(final K k1, final K k2, final TrieMap<K, V> ct) {
        return ct.equality().equiv(k1, k2);
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
            MainNode<K, V> m = GCAS_READ (ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multiway node
                CNode<K, V> cn = (CNode<K, V>) m;
                int idx = (hc >>> lev) & 0x1f;
                int flag = 1 << idx;
                int bmp = cn.bitmap;
                int mask = flag - 1;
                int pos = Integer.bitCount(bmp & mask);
                if ((bmp & flag) != 0) {
                    // 1a) insert below
                    BasicNode cnAtPos = cn.array[pos];
                    if (cnAtPos instanceof INode) {
                        INode<K, V> in = (INode<K, V>) cnAtPos;
                        if (startgen == in.gen) {
                            return in.rec_insert(k, v, hc, lev + 5, this, startgen, ct);
                        } else {
                            if (GCAS (cn, cn.renewed(startgen, ct), ct)) {
                                // return rec_insert (k, v, hc, lev, parent,
                                // startgen, ct);
                                // tailrec
                                continue;
                            } else {
                                return false;
                            }
                        }
                    } else if (cnAtPos instanceof SNode) {
                        SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (sn.hc == hc && equal(sn.k, k, ct)) {
                            return GCAS (cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct);
                        } else {
                            CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                            MainNode<K, V> nn = rn.updatedAt(pos, inode(CNode.dual(sn, sn.hc, new SNode<>(k, v, hc),
                                hc, lev + 5, gen)), gen);
                            return GCAS (cn, nn, ct);
                        }
                    }
                } else {
                    CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed (gen, ct);
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
    Option<V> rec_insertif(final K k, final V v, final int hc, final Object cond, final int lev,
            final INode<K, V> parent, final Gen startgen, final TrieMap<K, V> ct) {
        while (true) {
            MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multiway node
                CNode<K, V> cn = (CNode<K, V>) m;
                int idx = (hc >>> lev) & 0x1f;
                int flag = 1 << idx;
                int bmp = cn.bitmap;
                int mask = flag - 1;
                int pos = Integer.bitCount(bmp & mask);

                if ((bmp & flag) != 0) {
                    // 1a) insert below
                    BasicNode cnAtPos = cn.array [pos];
                    if (cnAtPos instanceof INode) {
                        INode<K, V> in = (INode<K, V>) cnAtPos;
                        if (startgen == in.gen) {
                            return in.rec_insertif(k, v, hc, cond, lev + 5, this, startgen, ct);
                        } else {
                            if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                                // return rec_insertif (k, v, hc, cond, lev,
                                // parent, startgen, ct);
                                // tailrec
                                continue;
                            } else {
                                return null;
                            }
                        }
                    } else if (cnAtPos instanceof SNode) {
                        SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (cond == null) {
                            if (sn.hc == hc && equal(sn.k, k, ct)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<> (k, v, hc), gen), ct)) {
                                    return Option.makeOption(sn.v);
                                } else {
                                    return null;
                                }
                            } else {
                                CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                                MainNode<K, V> nn = rn.updatedAt(pos, inode (CNode.dual(sn, sn.hc,
                                    new SNode<>(k, v, hc), hc, lev + 5, gen)), gen);
                                if (GCAS(cn, nn, ct)) {
                                    return Option.makeOption(); // None;
                                } else {
                                    return null;
                                }
                            }

                        } else if (cond == INode.KEY_ABSENT) {
                            if (sn.hc == hc && equal (sn.k, k, ct)) {
                                return Option.makeOption(sn.v);
                            } else {
                                CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed (gen, ct);
                                MainNode<K, V> nn = rn.updatedAt(pos, inode (CNode.dual(sn, sn.hc,
                                    new SNode<>(k, v, hc), hc, lev + 5, gen)), gen);
                                if (GCAS(cn, nn, ct)) {
                                    return Option.makeOption(); // None
                                } else {
                                    return null;
                                }
                            }
                        } else if (cond == INode.KEY_PRESENT) {
                            if (sn.hc == hc && equal (sn.k, k, ct)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Option.makeOption(sn.v);
                                } else {
                                    return null;
                                }

                            }
                            else {
                                return Option.makeOption();// None;
                            }
                        } else {
                            if (sn.hc == hc && equal (sn.k, k, ct) && sn.v == cond) {
                                if (GCAS(cn, cn.updatedAt (pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Option.makeOption(sn.v);
                                } else {
                                    return null;
                                }
                            }
                            else {
                                return Option.makeOption(); // None
                            }
                        }

                    }
                } else if (cond == null || cond == INode.KEY_ABSENT) {
                    CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                    CNode<K, V> ncnode = rn.insertedAt (pos, flag, new SNode<>(k, v, hc), gen);
                    if (GCAS(cn, ncnode, ct)) {
                        return Option.makeOption(); // None
                    } else {
                        return null;
                    }
                } else if (cond == INode.KEY_PRESENT) {
                    return Option.makeOption(); // None;
                }
                else {
                    return Option.makeOption(); // None
                }
            } else if (m instanceof TNode) {
                clean(parent, ct, lev - 5);
                return null;
            } else if (m instanceof LNode) {
                // 3) an l-node
                LNode<K, V> ln = (LNode<K, V>) m;
                if (cond == null) {
                    Option<V> optv = ln.get(k);
                    if (insertln(ln, k, v, ct)) {
                        return optv;
                    } else {
                        return null;
                    }
                } else if (cond == INode.KEY_ABSENT) {
                    Option<V> t = ln.get(k);
                    if (t == null) {
                        if (insertln(ln, k, v, ct)) {
                            return Option.makeOption();// None
                        } else {
                            return null;
                        }
                    } else {
                        return t;
                    }
                } else if (cond == INode.KEY_PRESENT) {
                    Option<V> t = ln.get(k);
                    if (t != null) {
                        if (insertln(ln, k, v, ct)) {
                            return t;
                        } else {
                            return null;
                        }
                    } else {
                        return null; // None
                    }
                } else {
                    Option<V> t = ln.get (k);
                    if (t != null) {
                        if (((Some<V>) t).get() == cond) {
                            if (insertln(ln, k, v, ct)) {
                                return new Some<>((V) cond);
                            } else {
                                return null;
                            }
                        } else {
                            return Option.makeOption ();
                        }
                    }
                }
            }

            //                throw new RuntimeException ("Should not happen");
        }
    }

    boolean insertln(final LNode<K, V> ln, final K k, final V v, final TrieMap<K, V> ct) {
        LNode<K, V> nn = ln.inserted (k, v);
        return GCAS (ln, nn, ct);
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
            MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

            if (m instanceof CNode) {
                // 1) a multinode
                final CNode<K, V> cn = (CNode<K, V>) m;
                int idx = (hc >>> lev) & 0x1f;
                int flag = 1 << idx;
                int bmp = cn.bitmap;
                if ((bmp & flag) == 0) {
                    return null; // 1a) bitmap shows no binding
                } else { // 1b) bitmap contains a value - descend
                    int pos = (bmp == 0xffffffff) ? idx : Integer.bitCount(bmp & (flag - 1));
                    final BasicNode sub = cn.array[pos];
                    if (sub instanceof INode) {
                        INode<K, V> in = (INode<K, V>) sub;
                        if (ct.isReadOnly() || (startgen == ((INodeBase<K, V>) sub).gen)) {
                            return in.rec_lookup(k, hc, lev + 5, this, startgen, ct);
                        } else {
                            if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                                // return rec_lookup (k, hc, lev, parent,
                                // startgen, ct);
                                // Tailrec
                                continue;
                            } else {
                                return RESTART; // used to be throw RestartException
                            }
                        }
                    } else if (sub instanceof SNode) {
                        // 2) singleton node
                        SNode<K, V> sn = (SNode<K, V>) sub;
                        if (((SNode) sub).hc == hc && equal (sn.k, k, ct)) {
                            return sn.v;
                        } else {
                            return null;
                        }
                    }
                }
            } else if (m instanceof TNode) {
                // 3) non-live node
                return cleanReadOnly((TNode<K, V>) m, lev, parent, ct, k, hc);
            } else if (m instanceof LNode) {
                // 5) an l-node
                Option<V> tmp = ((LNode<K, V>) m).get (k);
                return (tmp instanceof Option) ? ((Option<V>) tmp) : null;
            }

            throw new RuntimeException ("Should not happen");
        }
    }

    private Object cleanReadOnly(final TNode<K, V> tn, final int lev, final INode<K, V> parent,
            final TrieMap<K, V> ct, final K k, final int hc) {
        if (ct.nonReadOnly()) {
            clean(parent, ct, lev - 5);
            return RESTART; // used to be throw RestartException
        } else {
            if (tn.hc == hc && equal(tn.k, k, ct)) {
                return tn.v;
            } else {
                return null;
            }
        }
    }

    /**
     * Removes the key associated with the given value.
     *
     * @param v
     *            if null, will remove the key irregardless of the value;
     *            otherwise removes only if binding contains that exact key
     *            and value
     * @return null if not successful, an Option[V] indicating the previous
     *         value otherwise
     */
    Option<V> rec_remove(final K k, final V v, final int hc, final int lev, final INode<K, V> parent,
            final Gen startgen, final TrieMap<K, V> ct) {
        MainNode<K, V> m = GCAS_READ(ct); // use -Yinline!

        if (m instanceof CNode) {
            CNode<K, V> cn = (CNode<K, V>) m;
            int idx = (hc >>> lev) & 0x1f;
            int bmp = cn.bitmap;
            int flag = 1 << idx;
            if ((bmp & flag) == 0) {
                return Option.makeOption();
            } else {
                int pos = Integer.bitCount(bmp & (flag - 1));
                BasicNode sub = cn.array [pos];
                Option<V> res = null;
                if (sub instanceof INode) {
                    INode<K, V> in = (INode<K, V>) sub;
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
                    SNode<K, V> sn = (SNode<K, V>) sub;
                    if (sn.hc == hc && equal(sn.k, k, ct) && (v == null || v.equals(sn.v))) {
                        MainNode<K, V> ncn = cn.removedAt(pos, flag, gen).toContracted(lev);
                        if (GCAS(cn, ncn, ct)) {
                            res = Option.makeOption(sn.v);
                        } else {
                            res = null;
                        }
                    } else {
                        res = Option.makeOption ();
                    }
                }

                if (res instanceof None || (res == null)) {
                    return res;
                } else {
                    if (parent != null) { // never tomb at root
                        MainNode<K, V> n = GCAS_READ(ct);
                        if (n instanceof TNode) {
                            cleanParent(n, parent, ct, hc, lev, startgen);
                        }
                    }

                    return res;
                }
            }
        } else if (m instanceof TNode) {
            clean(parent, ct, lev - 5);
            return null;
        } else if (m instanceof LNode) {
            LNode<K, V> ln = (LNode<K, V>) m;
            if (v == null) {
                Option<V> optv = ln.get(k);
                MainNode<K, V> nn = ln.removed(k, ct);
                if (GCAS (ln, nn, ct)) {
                    return optv;
                } else {
                    return null;
                }
            } else {
                Option<V> tmp = ln.get(k);
                if (tmp instanceof Some) {
                    Some<V> tmp1 = (Some<V>) tmp;
                    if (tmp1.get () == v) {
                        MainNode<K, V> nn = ln.removed(k, ct);
                        if (GCAS(ln, nn, ct)) {
                            return tmp;
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        throw new RuntimeException ("Should not happen");
    }

    void cleanParent(final Object nonlive, final INode<K, V> parent, final TrieMap<K, V> ct, final int hc,
            final int lev, final Gen startgen) {
        while (true) {
            MainNode<K, V> pm = parent.GCAS_READ(ct);
            if (pm instanceof CNode) {
                CNode<K, V> cn = (CNode<K, V>) pm;
                int idx = (hc >>> (lev - 5)) & 0x1f;
                int bmp = cn.bitmap;
                int flag = 1 << idx;
                if ((bmp & flag) == 0) {
                    // somebody already removed this i-node, we're done
                } else {
                    int pos = Integer.bitCount(bmp & (flag - 1));
                    BasicNode sub = cn.array[pos];
                    if (sub == this) {
                        if (nonlive instanceof TNode) {
                            TNode<K, V> tn = (TNode<K, V>) nonlive;
                            MainNode<K, V> ncn = cn.updatedAt(pos, tn.copyUntombed (), gen).toContracted (lev - 5);
                            if (!parent.GCAS(cn, ncn, ct)) {
                                if (ct.readRoot().gen == startgen) {
                                    // cleanParent (nonlive, parent, ct, hc,
                                    // lev, startgen);
                                    // tailrec
                                    continue;
                                }
                            }
                        }
                    }
                }
            } else {
                // parent is no longer a cnode, we're done
            }
            break;
        }
    }

    private void clean(final INode<K, V> nd, final TrieMap<K, V> ct, final int lev) {
        MainNode<K, V> m = nd.GCAS_READ(ct);
        if (m instanceof CNode) {
            CNode<K, V> cn = (CNode<K, V>) m;
            nd.GCAS(cn, cn.toCompressed(ct, lev, gen), ct);
        }
    }

    boolean isNullInode(final TrieMap<K, V> ct) {
        return GCAS_READ(ct) == null;
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
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

import static org.opendaylight.yangtools.triemap.Constants.LEVEL_BITS;
import static org.opendaylight.yangtools.triemap.LookupResult.RESTART;
import static org.opendaylight.yangtools.triemap.PresencePredicate.ABSENT;
import static org.opendaylight.yangtools.triemap.PresencePredicate.PRESENT;

import com.google.common.base.VerifyException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class INode<K, V> extends BasicNode {
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<INode, MainNode> MAINNODE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(INode.class, MainNode.class, "mainnode");

    private final Gen gen;

    private volatile MainNode<K, V> mainnode;

    INode(final Gen gen, final MainNode<K, V> mainnode) {
        this.gen = gen;
        this.mainnode = mainnode;
    }

    MainNode<K, V> gcasRead(final TrieMap<?, ?> ct) {
        return GCAS_READ(ct);
    }

    private MainNode<K, V> GCAS_READ(final TrieMap<?, ?> ct) {
        MainNode<K, V> m = /* READ */ mainnode;
        MainNode<K, V> prevval = /* READ */ m.READ_PREV();
        if (prevval == null) {
            return m;
        }

        return GCAS_Complete(m, ct);
    }

    private MainNode<K, V> GCAS_Complete(final MainNode<K, V> oldmain, final TrieMap<?, ?> ct) {
        MainNode<K, V> m = oldmain;
        while (m != null) {
            // complete the GCAS
            final MainNode<K, V> prev = /* READ */ m.READ_PREV();
            final INode<?, ?> ctr = ct.readRoot(true);
            if (prev == null) {
                return m;
            }

            if (prev instanceof FailedNode) {
                // try to commit to previous value
                FailedNode<K, V> fn = (FailedNode<K, V>) prev;
                if (MAINNODE_UPDATER.compareAndSet(this, m, fn.READ_PREV())) {
                    return fn.READ_PREV();
                }

                // Tail recursion: return GCAS_Complete(/* READ */ mainnode, ct);
                m = /* READ */ mainnode;
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
            if (ctr.gen == gen && !ct.isReadOnly()) {
                // try to commit
                if (m.CAS_PREV(prev, null)) {
                    return m;
                }

                // Tail recursion: return GCAS_Complete(m, ct);
                continue;
            }

            // try to abort
            m.CAS_PREV(prev, new FailedNode<>(prev));

            // Tail recursion: return GCAS_Complete(/* READ */ mainnode, ct);
            m = /* READ */ mainnode;
        }

        return null;
    }

    private boolean GCAS(final MainNode<K, V> old, final MainNode<K, V> n, final TrieMap<?, ?> ct) {
        n.WRITE_PREV(old);
        if (MAINNODE_UPDATER.compareAndSet(this, old, n)) {
            GCAS_Complete(n, ct);
            return /* READ */ n.READ_PREV() == null;
        }

        return false;
    }

    private INode<K, V> inode(final MainNode<K, V> cn) {
        return new INode<>(gen, cn);
    }

    INode<K, V> copyToGen(final Gen ngen, final TrieMap<?, ?> ct) {
        return new INode<>(ngen, GCAS_READ(ct));
    }

    /**
     * Inserts a key value pair, overwriting the old pair if the keys match.
     *
     * @return true if successful, false otherwise
     */
    boolean rec_insert(final K key, final V value, final int hc, final int lev, final INode<K, V> parent,
            final TrieMap<K, V> ct) {
        return rec_insert(key, value, hc, lev, parent, gen, ct);
    }

    private boolean rec_insert(final K k, final V v, final int hc, final int lev, final INode<K, V> parent,
            final Gen startgen, final TrieMap<K, V> ct) {
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
                            return in.rec_insert(k, v, hc, lev + LEVEL_BITS, this, startgen, ct);
                        }
                        if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                            // Tail recursion: return rec_insert(k, v, hc, lev, parent, startgen, ct);
                            continue;
                        }

                        return false;
                    } else if (cnAtPos instanceof SNode) {
                        final SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (sn.hc == hc && ct.equal(sn.key, k)) {
                            return GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct);
                        }

                        final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                        final MainNode<K, V> nn = rn.updatedAt(pos, inode(
                            CNode.dual(sn, k, v, hc, lev + LEVEL_BITS, gen)), gen);
                        return GCAS(cn, nn, ct);
                    } else {
                        throw CNode.invalidElement(cnAtPos);
                    }
                }

                final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                final MainNode<K, V> ncnode = rn.insertedAt(pos, flag, new SNode<>(k, v, hc), gen);
                return GCAS(cn, ncnode, ct);
            } else if (m instanceof TNode) {
                clean(parent, ct, lev - LEVEL_BITS);
                return false;
            } else if (m instanceof LNode) {
                final LNode<K, V> ln = (LNode<K, V>) m;
                final LNodeEntry<K, V> entry = ln.get(ct.equiv(), k);
                return entry != null ? replaceln(ln, entry, v, ct) : insertln(ln, k, v, ct);
            } else {
                throw invalidElement(m);
            }
        }
    }

    private static VerifyException invalidElement(final BasicNode elem) {
        throw new VerifyException("An INode can host only a CNode, a TNode or an LNode, not " + elem);
    }

    @SuppressFBWarnings(value = "NP_OPTIONAL_RETURN_NULL",
            justification = "Returning null Optional indicates the need to restart.")
    private Optional<V> insertDual(final TrieMap<K, V> ct, final CNode<K, V> cn, final int pos, final SNode<K, V> sn,
            final K k, final V v, final int hc, final int lev) {
        final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
        final MainNode<K, V> nn = rn.updatedAt(pos, inode(CNode.dual(sn, k, v, hc, lev + LEVEL_BITS, gen)), gen);
        return GCAS(cn, nn, ct) ? Optional.empty() : null;
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
            final INode<K, V> parent, final TrieMap<K, V> ct) {
        return rec_insertif(k, v, hc, cond, lev, parent, gen, ct);
    }

    @SuppressFBWarnings(value = "NP_OPTIONAL_RETURN_NULL",
            justification = "Returning null Optional indicates the need to restart.")
    private Optional<V> rec_insertif(final K k, final V v, final int hc, final Object cond, final int lev,
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
                            return in.rec_insertif(k, v, hc, cond, lev + LEVEL_BITS, this, startgen, ct);
                        }

                        if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                            // Tail recursion: return rec_insertif(k, v, hc, cond, lev, parent, startgen, ct);
                            continue;
                        }

                        return null;
                    } else if (cnAtPos instanceof SNode) {
                        final SNode<K, V> sn = (SNode<K, V>) cnAtPos;
                        if (cond == null) {
                            if (sn.hc == hc && ct.equal(sn.key, k)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.value);
                                }

                                return null;
                            }

                            return insertDual(ct, cn, pos, sn, k, v, hc, lev);
                        } else if (cond == ABSENT) {
                            if (sn.hc == hc && ct.equal(sn.key, k)) {
                                return Optional.of(sn.value);
                            }

                            return insertDual(ct, cn, pos, sn, k, v, hc, lev);
                        } else if (cond == PRESENT) {
                            if (sn.hc == hc && ct.equal(sn.key, k)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.value);
                                }
                                return null;
                            }

                            return Optional.empty();
                        } else {
                            if (sn.hc == hc && ct.equal(sn.key, k) && cond.equals(sn.value)) {
                                if (GCAS(cn, cn.updatedAt(pos, new SNode<>(k, v, hc), gen), ct)) {
                                    return Optional.of(sn.value);
                                }

                                return null;
                            }

                            return Optional.empty();
                        }
                    } else {
                        throw CNode.invalidElement(cnAtPos);
                    }
                } else if (cond == null || cond == ABSENT) {
                    final CNode<K, V> rn = (cn.gen == gen) ? cn : cn.renewed(gen, ct);
                    final CNode<K, V> ncnode = rn.insertedAt(pos, flag, new SNode<>(k, v, hc), gen);
                    if (GCAS(cn, ncnode, ct)) {
                        return Optional.empty();
                    }

                    return null;
                } else {
                    return Optional.empty();
                }
            } else if (m instanceof TNode) {
                clean(parent, ct, lev - LEVEL_BITS);
                return null;
            } else if (m instanceof LNode) {
                // 3) an l-node
                final LNode<K, V> ln = (LNode<K, V>) m;
                final LNodeEntry<K, V> entry = ln.get(ct.equiv(), k);

                if (cond == null) {
                    if (entry != null) {
                        return replaceln(ln, entry, v, ct) ? Optional.of(entry.getValue()) : null;
                    }

                    return insertln(ln, k, v, ct) ? Optional.empty() : null;
                } else if (cond == ABSENT) {
                    if (entry != null) {
                        return Optional.of(entry.getValue());
                    }

                    return insertln(ln, k, v, ct) ? Optional.empty() : null;
                } else if (cond == PRESENT) {
                    if (entry == null) {
                        return Optional.empty();
                    }

                    return replaceln(ln, entry, v, ct) ? Optional.of(entry.getValue()) : null;
                } else {
                    if (entry == null || !cond.equals(entry.getValue())) {
                        return Optional.empty();
                    }

                    return replaceln(ln, entry, v, ct) ? Optional.of(entry.getValue()) : null;
                }
            } else {
                throw invalidElement(m);
            }
        }
    }

    private boolean insertln(final LNode<K, V> ln, final K k, final V v, final TrieMap<K, V> ct) {
        return GCAS(ln, ln.insertChild(k, v), ct);
    }

    private boolean replaceln(final LNode<K, V> ln, final LNodeEntry<K, V> entry, final V v, final TrieMap<K, V> ct) {
        return GCAS(ln, ln.replaceChild(entry, v), ct);
    }

    /**
     * Looks up the value associated with the key.
     *
     * @return null if no value has been found, RESTART if the operation
     *         wasn't successful, or any other value otherwise
     */
    Object rec_lookup(final K k, final int hc, final int lev, final INode<K, V> parent, final TrieMap<K, V> ct) {
        return rec_lookup(k, hc, lev, parent, gen, ct);
    }

    private Object rec_lookup(final K k, final int hc, final int lev, final INode<K, V> parent, final Gen startgen,
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
                        return in.rec_lookup(k, hc, lev + LEVEL_BITS, this, startgen, ct);
                    }

                    if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                        // Tail recursion: return rec_lookup(k, hc, lev, parent, startgen, ct);
                        continue;
                    }

                    return RESTART;
                } else if (sub instanceof SNode) {
                    // 2) singleton node
                    final SNode<K, V> sn = (SNode<K, V>) sub;
                    if (sn.hc == hc && ct.equal(sn.key, k)) {
                        return sn.value;
                    }

                    return null;
                } else {
                    throw CNode.invalidElement(sub);
                }
            } else if (m instanceof TNode) {
                // 3) non-live node
                return cleanReadOnly((TNode<K, V>) m, lev, parent, ct, k, hc);
            } else if (m instanceof LNode) {
                // 5) an l-node
                final LNodeEntry<K, V> entry = ((LNode<K, V>) m).get(ct.equiv(), k);
                return entry != null ? entry.getValue() : null;
            } else {
                throw invalidElement(m);
            }
        }
    }

    private Object cleanReadOnly(final TNode<K, V> tn, final int lev, final INode<K, V> parent,
            final TrieMap<K, V> ct, final K k, final int hc) {
        if (ct.isReadOnly()) {
            if (tn.hc == hc && ct.equal(tn.key, k)) {
                return tn.value;
            }

            return null;
        }

        clean(parent, ct, lev - LEVEL_BITS);
        return RESTART;
    }

    /**
     * Removes the key associated with the given value.
     *
     * @param cond
     *            if null, will remove the key regardless of the value;
     *            otherwise removes only if binding contains that exact key
     *            and value
     * @return null if not successful, an Optional indicating the previous
     *         value otherwise
     */
    Optional<V> rec_remove(final K k, final Object cond, final int hc, final int lev, final INode<K, V> parent,
            final TrieMap<K, V> ct) {
        return rec_remove(k, cond, hc, lev, parent, gen, ct);
    }

    @SuppressFBWarnings(value = "NP_OPTIONAL_RETURN_NULL",
            justification = "Returning null Optional indicates the need to restart.")
    private Optional<V> rec_remove(final K k, final Object cond, final int hc, final int lev, final INode<K, V> parent,
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
            final Optional<V> res;
            if (sub instanceof INode) {
                final INode<K, V> in = (INode<K, V>) sub;
                if (startgen == in.gen) {
                    res = in.rec_remove(k, cond, hc, lev + LEVEL_BITS, this, startgen, ct);
                } else {
                    if (GCAS(cn, cn.renewed(startgen, ct), ct)) {
                        res = rec_remove(k, cond, hc, lev, parent, startgen, ct);
                    } else {
                        res = null;
                    }
                }
            } else if (sub instanceof SNode) {
                final SNode<K, V> sn = (SNode<K, V>) sub;
                if (sn.hc == hc && ct.equal(sn.key, k) && (cond == null || cond.equals(sn.value))) {
                    final MainNode<K, V> ncn = cn.removedAt(pos, flag, gen).toContracted(lev);
                    if (GCAS(cn, ncn, ct)) {
                        res = Optional.of(sn.value);
                    } else {
                        res = null;
                    }
                } else {
                    res = Optional.empty();
                }
            } else {
                throw CNode.invalidElement(sub);
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
            clean(parent, ct, lev - LEVEL_BITS);
            return null;
        } else if (m instanceof LNode) {
            final LNode<K, V> ln = (LNode<K, V>) m;
            final LNodeEntry<K, V> entry = ln.get(ct.equiv(), k);
            if (entry == null) {
                // Key was not found, hence no modification is needed
                return Optional.empty();
            }

            final V value = entry.getValue();
            if (cond != null && !cond.equals(value)) {
                // Value does not match
                return Optional.empty();
            }

            return GCAS(ln, ln.removeChild(entry, hc), ct) ? Optional.of(value) : null;
        } else {
            throw invalidElement(m);
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
            final int idx = (hc >>> (lev - LEVEL_BITS)) & 0x1f;
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
                    final TNode<?, ?> tn = (TNode<?, ?>) nonlive;
                    final MainNode<K, V> ncn = cn.updatedAt(pos, tn.copyUntombed(), gen).toContracted(lev - LEVEL_BITS);
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

    int size(final ImmutableTrieMap<?, ?> ct) {
        return GCAS_READ(ct).size(ct);
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

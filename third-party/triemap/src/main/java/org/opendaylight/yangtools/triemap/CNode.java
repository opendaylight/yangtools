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

final class CNode<K, V> extends CNodeBase<K, V> {
    private static final BasicNode[] EMPTY_ARRAY = new BasicNode[0];

    final int bitmap;
    final BasicNode[] array;
    final Gen gen;

    CNode(final Gen gen) {
        this(gen, 0, EMPTY_ARRAY);
    }

    private CNode(final Gen gen, final int bitmap, final BasicNode... array) {
        this.bitmap = bitmap;
        this.array = array;
        this.gen = gen;
    }

    // this should only be called from within read-only snapshots
    @Override
    int cachedSize(final TrieMap<K, V> ct) {
        final int currsz = READ_SIZE();
        if (currsz != -1) {
            return currsz;
        }

        final int sz = computeSize(ct);
        while (READ_SIZE () == -1) {
            CAS_SIZE (-1, sz);
        }
        return READ_SIZE ();
    }

    // lends itself towards being parallelizable by choosing
    // a random starting offset in the array
    // => if there are concurrent size computations, they start
    // at different positions, so they are more likely to
    // to be independent
    private int computeSize(final TrieMap<K, V> ct) {
        int i = 0;
        int sz = 0;
        // final int offset = (array.length > 0) ?
        // // util.Random.nextInt(array.length) /* <-- benchmarks show that
        // // this causes observable contention */
        // scala.concurrent.forkjoin.ThreadLocalRandom.current.nextInt (0,
        // array.length)
        // : 0;

        final int offset = 0;
        while (i < array.length) {
            int pos = (i + offset) % array.length;
            BasicNode elem = array [pos];
            if (elem instanceof SNode) {
                sz += 1;
            } else if (elem instanceof INode) {
                sz += ((INode<K, V>) elem).cachedSize (ct);
            }
            i += 1;
        }
        return sz;
    }

    CNode<K, V> updatedAt(final int pos, final BasicNode nn, final Gen gen) {
        int len = array.length;
        BasicNode[] narr = new BasicNode[len];
        System.arraycopy(array, 0, narr, 0, len);
        narr[pos] = nn;
        return new CNode<>(gen, bitmap, narr);
    }

    CNode<K, V> removedAt(final int pos, final int flag, final Gen gen) {
        BasicNode[] arr = array;
        int len = arr.length;
        BasicNode[] narr = new BasicNode[len - 1];
        System.arraycopy(arr, 0, narr, 0, pos);
        System.arraycopy(arr, pos + 1, narr, pos, len - pos - 1);
        return new CNode<>(gen, bitmap ^ flag, narr);
    }

    CNode<K, V> insertedAt(final int pos, final int flag, final BasicNode nn, final Gen gen) {
        int len = array.length;
        int bmp = bitmap;
        BasicNode[] narr = new BasicNode[len + 1];
        System.arraycopy(array, 0, narr, 0, pos);
        narr [pos] = nn;
        System.arraycopy(array, pos, narr, pos + 1, len - pos);
        return new CNode<>(gen, bmp | flag, narr);
    }

    /**
     * Returns a copy of this cnode such that all the i-nodes below it are
     * copied to the specified generation `ngen`.
     */
    CNode<K, V> renewed(final Gen ngen, final TrieMap<K, V> ct) {
        int i = 0;
        BasicNode[] arr = array;
        int len = arr.length;
        BasicNode[] narr = new BasicNode[len];
        while (i < len) {
            BasicNode elem = arr[i];
            if (elem instanceof INode) {
                INode<K, V> in = (INode<K, V>) elem;
                narr [i] = in.copyToGen(ngen, ct);
            } else if (elem instanceof BasicNode) {
                narr [i] = elem;
            }
            i += 1;
        }
        return new CNode<>(ngen, bitmap, narr);
    }

    private BasicNode resurrect(final INode<K, V> inode, final Object inodemain) {
        if (inodemain instanceof TNode) {
            TNode<K, V> tn = (TNode<K, V>) inodemain;
            return tn.copyUntombed();
        }

        return inode;
    }

    MainNode<K, V> toContracted(final int lev) {
        if (array.length == 1 && lev > 0) {
            if (array [0] instanceof SNode) {
                SNode<K, V> sn = (SNode<K, V>) array[0];
                return sn.copyTombed();
            } else {
                return this;
            }
        } else {
            return this;
        }
    }

    // - if the branching factor is 1 for this CNode, and the child
    // is a tombed SNode, returns its tombed version
    // - otherwise, if there is at least one non-null node below,
    // returns the version of this node with at least some null-inodes
    // removed (those existing when the op began)
    // - if there are only null-i-nodes below, returns null
    MainNode<K, V> toCompressed(final TrieMap<K, V> ct, final int lev, final Gen gen) {
        int bmp = bitmap;
        int i = 0;
        BasicNode[] arr = array;
        BasicNode[] tmparray = new BasicNode[arr.length];
        while (i < arr.length) { // construct new bitmap
            BasicNode sub = arr[i];
            if (sub instanceof INode) {
                INode<K, V> in = (INode<K, V>) sub;
                MainNode<K, V> inodemain = in.gcasRead (ct);
                assert (inodemain != null);
                tmparray [i] = resurrect (in, inodemain);
            } else if (sub instanceof SNode) {
                tmparray [i] = sub;
            }
            i += 1;
        }

        return new CNode<K, V>(gen, bmp, tmparray).toContracted(lev);
    }

    @Override
    String string(final int lev) {
        // "CNode %x\n%s".format(bitmap, array.map(_.string(lev +
        // 1)).mkString("\n"));
        return "CNode";
    }

    /*
     * quiescently consistent - don't call concurrently to anything
     * involving a GCAS!!
     */
    // protected Seq<K,V> collectElems() {
    // array flatMap {
    // case sn: SNode[K, V] => Some(sn.kvPair)
    // case in: INode[K, V] => in.mainnode match {
    // case tn: TNode[K, V] => Some(tn.kvPair)
    // case ln: LNode[K, V] => ln.listmap.toList
    // case cn: CNode[K, V] => cn.collectElems
    // }
    // }
    // }

    // protected Seq<String> collectLocalElems() {
    // // array flatMap {
    // // case sn: SNode[K, V] => Some(sn.kvPair._2.toString)
    // // case in: INode[K, V] => Some(in.toString.drop(14) + "(" + in.gen +
    // ")")
    // // }
    // return null;
    // }

    @Override
    public String toString () {
        // val elems = collectLocalElems
        // "CNode(sz: %d; %s)".format(elems.size,
        // elems.sorted.mkString(", "))
        return "CNode";
    }

    static <K, V> MainNode<K,V> dual(final SNode<K, V> x, final int xhc, final SNode<K, V> y, final int yhc,
            final int lev, final Gen gen) {
        if (lev < 35) {
            int xidx = (xhc >>> lev) & 0x1f;
            int yidx = (yhc >>> lev) & 0x1f;
            int bmp = (1 << xidx) | (1 << yidx);

            if (xidx == yidx) {
                INode<K, V> subinode = new INode<>(gen, dual(x, xhc, y, yhc, lev + 5, gen));
                return new CNode<>(gen, bmp, subinode);
            } else {
                if (xidx < yidx) {
                    return new CNode<>(gen, bmp, x, y);
                } else {
                    return new CNode<>(gen, bmp, y, x);
                }
            }
        } else {
            return new LNode<>(x.k, x.v, y.k, y.v);
        }
    }
}

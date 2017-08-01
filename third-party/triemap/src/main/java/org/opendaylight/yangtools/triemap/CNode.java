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

import static org.opendaylight.yangtools.triemap.Constants.HASH_BITS;
import static org.opendaylight.yangtools.triemap.Constants.LEVEL_BITS;

import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import java.util.concurrent.ThreadLocalRandom;

final class CNode<K, V> extends MainNode<K, V> {
    private static final BasicNode[] EMPTY_ARRAY = new BasicNode[0];

    final int bitmap;
    final BasicNode[] array;
    final Gen gen;

    // Since concurrent computation should lead to same results we can update this field without any synchronization.
    private volatile int csize = NO_SIZE;

    private CNode(final Gen gen, final int bitmap, final BasicNode... array) {
        this.bitmap = bitmap;
        this.array = array;
        this.gen = gen;
    }

    CNode(final Gen gen) {
        this(gen, 0, EMPTY_ARRAY);
    }

    static <K, V> MainNode<K,V> dual(final SNode<K, V> x, final K key, final V value, final int hc, final int lev,
            final Gen gen) {
        return dual(x, x.hc, new SNode<>(key, value, hc), hc, lev, gen);
    }

    private static <K, V> MainNode<K,V> dual(final SNode<K, V> x, final int xhc, final SNode<K, V> y, final int yhc,
            final int lev, final Gen gen) {
        if (lev >= HASH_BITS) {
            return new LNode<>(x.key, x.value, y.key, y.value);
        }

        final int xidx = (xhc >>> lev) & 0x1f;
        final int yidx = (yhc >>> lev) & 0x1f;
        final int bmp = (1 << xidx) | (1 << yidx);

        if (xidx == yidx) {
            return new CNode<>(gen, bmp, new INode<>(gen, dual(x, xhc, y, yhc, lev + LEVEL_BITS, gen)));
        }

        return xidx < yidx ? new CNode<>(gen, bmp, x, y) : new CNode<>(gen, bmp, y, x);
    }

    @Override
    int trySize() {
        return csize;
    }

    @Override
    int size(final ImmutableTrieMap<?, ?> ct) {
        int sz;
        return (sz = csize) != NO_SIZE ? sz : (csize = computeSize(ct));
    }

    static VerifyException invalidElement(final BasicNode elem) {
        throw new VerifyException("A CNode can contain only CNodes and SNodes, not " + elem);
    }

    // lends itself towards being parallelizable by choosing
    // a random starting offset in the array
    // => if there are concurrent size computations, they start
    // at different positions, so they are more likely to
    // to be independent
    private int computeSize(final ImmutableTrieMap<?, ?> ct) {
        final int len = array.length;
        switch (len) {
            case 0:
                return 0;
            case 1:
                return elementSize(array[0], ct);
            default:
                final int offset = ThreadLocalRandom.current().nextInt(len);
                int sz = 0;
                for (int i = offset; i < len; ++i) {
                    sz += elementSize(array[i], ct);
                }
                for (int i = 0; i < offset; ++i) {
                    sz += elementSize(array[i], ct);
                }
                return sz;
        }
    }

    private static int elementSize(final BasicNode elem, final ImmutableTrieMap<?, ?> ct) {
        if (elem instanceof SNode) {
            return 1;
        } else if (elem instanceof INode) {
            return ((INode<?, ?>) elem).size(ct);
        } else {
            throw invalidElement(elem);
        }
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
        BasicNode[] narr = new BasicNode[len + 1];
        System.arraycopy(array, 0, narr, 0, pos);
        narr [pos] = nn;
        System.arraycopy(array, pos, narr, pos + 1, len - pos);
        return new CNode<>(gen, bitmap | flag, narr);
    }

    /**
     * Returns a copy of this cnode such that all the i-nodes below it are
     * copied to the specified generation `ngen`.
     */
    CNode<K, V> renewed(final Gen ngen, final TrieMap<K, V> ct) {
        int i = 0;
        final BasicNode[] arr = array;
        final int len = arr.length;
        final BasicNode[] narr = new BasicNode[len];
        while (i < len) {
            final BasicNode elem = arr[i];
            if (elem instanceof INode) {
                narr[i] = ((INode<?, ?>) elem).copyToGen(ngen, ct);
            } else if (elem != null) {
                narr[i] = elem;
            }
            i += 1;
        }
        return new CNode<>(ngen, bitmap, narr);
    }

    MainNode<K, V> toContracted(final int lev) {
        if (array.length == 1 && lev > 0) {
            if (array[0] instanceof SNode) {
                return ((SNode<K, V>) array[0]).copyTombed();
            }
            return this;
        }

        return this;
    }

    // - if the branching factor is 1 for this CNode, and the child
    // is a tombed SNode, returns its tombed version
    // - otherwise, if there is at least one non-null node below,
    // returns the version of this node with at least some null-inodes
    // removed (those existing when the op began)
    // - if there are only null-i-nodes below, returns null
    MainNode<K, V> toCompressed(final TrieMap<?, ?> ct, final int lev, final Gen gen) {
        int bmp = bitmap;
        int i = 0;
        BasicNode[] arr = array;
        BasicNode[] tmparray = new BasicNode[arr.length];
        while (i < arr.length) { // construct new bitmap
            BasicNode sub = arr[i];
            if (sub instanceof INode) {
                final INode<?, ?> in = (INode<?, ?>) sub;
                final MainNode<?, ?> inodemain = Verify.verifyNotNull(in.gcasRead(ct));
                tmparray [i] = resurrect(in, inodemain);
            } else if (sub instanceof SNode) {
                tmparray [i] = sub;
            }
            i += 1;
        }

        return new CNode<K, V>(gen, bmp, tmparray).toContracted(lev);
    }

    private static BasicNode resurrect(final INode<?, ?> inode, final MainNode<?, ?> inodemain) {
        return inodemain instanceof TNode ? ((TNode<?, ?>) inodemain).copyUntombed() : inode;
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
    public String toString() {
        // val elems = collectLocalElems
        // "CNode(sz: %d; %s)".format(elems.size,
        // elems.sorted.mkString(", "))
        return "CNode";
    }
}

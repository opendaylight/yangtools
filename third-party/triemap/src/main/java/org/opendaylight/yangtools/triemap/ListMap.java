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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mimic immutable ListMap in Scala
 *
 * @author Roman Levenstein <romixlev@gmail.com>
 *
 * @param <V>
 */
abstract class ListMap<K,V> {

    ListMap<K,V> next;

    static <K,V> ListMap<K, V> map(final K k, final V v, final ListMap<K, V> tail){
        return new Node<> (k, v, tail);
    }

    static <K,V> ListMap<K, V> map(final K k, final V v){
        return new Node<> (k, v, null);
    }

    static <K,V> ListMap<K, V> map(final K k1, final V v1, final K k2, final V v2){
        return new Node<> (k1, v1, new Node<>(k2,v2, null));
    }

    public abstract int size ();

    public abstract boolean isEmpty() ;

    abstract public boolean contains(K k, V v);

    abstract public boolean contains(K key);

    abstract public Option<V> get (K key);

    abstract public ListMap<K, V> add (K key, V value);

    abstract public ListMap<K, V> remove (K key);

    abstract public Iterator<Map.Entry<K, V>> iterator();


    static class EmptyListMap<K,V> extends ListMap<K, V> {
        @Override
        public ListMap<K,V> add (final K key, final V value) {
            return ListMap.map(key, value, null);
        }

        @Override
        public boolean contains(final K k, final V v) {
            return false;
        }

        @Override
        public boolean contains(final K k) {
            return false;
        }

        @Override
        public ListMap<K,V> remove(final K key) {
            return this;
        }

        @Override
        public int size () {
            return 0;
        }

        @Override
        public boolean isEmpty () {
            return true;
        }

        @Override
        public Option<V> get (final K key) {
            return Option.makeOption (null);
        }

        @Override
        public Iterator<Entry<K, V>> iterator () {
            return new EmptyListMapIterator<> ();
        }

        static class EmptyListMapIterator<K,V> implements Iterator<Entry<K, V>> {

            @Override
            public boolean hasNext () {
                return false;
            }

            @Override
            public Entry<K, V> next () {
                return null;
            }

            @Override
            public void remove () {
                throw new RuntimeException("Operation not supported");
            }

        }
    }

    static class Node<K,V> extends ListMap<K, V> {
        final K k;
        final V v;

        Node(final K k, final V v, final ListMap<K,V> next) {
            this.k = k;
            this.v = v;
            this.next = next;
        }

        @Override
        public ListMap<K,V> add (final K key, final V value) {
            return ListMap.map(key, value, remove (key));
        }

        @Override
        public boolean contains(final K k, final V v) {
            if(k.equals (this.k) && v.equals (this.v)) {
                return true;
            } else if(next != null) {
                return next.contains (k, v);
            }
            return false;
        }

        @Override
        public boolean contains(final K k) {
            if(k.equals (this.k)) {
                return true;
            } else if(next != null) {
                return next.contains (k);
            }
            return false;
        }

        @Override
        public ListMap<K,V> remove(final K key) {
            if(!contains(key)) {
                return this;
            } else {
                return remove0(key);
            }
        }

        private ListMap<K, V> remove0 (final K key) {
            ListMap<K, V> n = this;
            ListMap<K, V> newN = null;
            ListMap<K, V> lastN = null;
            while (n != null) {
                if(n instanceof EmptyListMap) {
                    newN.next = n;
                    break;
                }
                Node<K, V> nn = (Node<K, V>)n;
                if (key.equals (nn.k)) {
                    n = n.next;
                    continue;
                } else {
                    if (newN != null) {
                        lastN.next = ListMap.map (nn.k, nn.v, null);
                        lastN = lastN.next;
                    } else {
                        newN = ListMap.map (nn.k, nn.v, null);
                        lastN = newN;
                    }
                }
                n = n.next;
            }
            return newN;
        }

        @Override
        public int size () {
            if(next == null) {
                return 1;
            } else {
                return 1+next.size ();
            }
        }

        @Override
        public boolean isEmpty () {
            return false;
        }

        @Override
        public Option<V> get (final K key) {
            if(key.equals (k)) {
                return Option.makeOption (v);
            }
            if(next != null) {
                return next.get (key);
            }
            return Option.makeOption (null);
        }


        @Override
        public Iterator<Entry<K, V>> iterator () {
            return new NodeIterator<> (this);
        }

        static class NodeIterator<K,V> implements Iterator<Entry<K, V>> {
            ListMap<K, V> n;

            public NodeIterator (final Node<K, V> n) {
                this.n = n;
            }

            @Override
            public boolean hasNext () {
//                return n!= null && n.next != null && !(n.next instanceof EmptyListMap);
                return n!= null && !(n instanceof EmptyListMap);
            }

            @Override
            public Entry<K, V> next () {
                if (n instanceof Node) {
                    Node<K, V> nn = (Node<K, V>) n;
                    Entry<K, V> res = new SimpleImmutableEntry<> (nn.k, nn.v);
                    n = n.next;
                    return res;
                } else {
                    return null;
                }
            }

            @Override
            public void remove () {
                throw new RuntimeException("Operation not supported");
            }

        }
    }
}

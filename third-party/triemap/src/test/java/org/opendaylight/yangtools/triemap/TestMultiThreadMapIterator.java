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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class TestMultiThreadMapIterator {
    private static final int NTHREADS = 7;

    @Test
    public void testMultiThreadMapIterator () {
        final Map<Object, Object> bt = new TrieMap<> ();
        for (int j = 0; j < 50 * 1000; j++) {
            final Object[] objects = getObjects (j);
            for (final Object o : objects) {
                bt.put (o, o);
            }
        }

      System.out.println ("Size of initialized map is " + bt.size ());
      int count = 0;
        {
            final ExecutorService es = Executors.newFixedThreadPool (NTHREADS);
            for (int i = 0; i < NTHREADS; i++) {
                final int threadNo = i;
                es.execute (new Runnable () {
                    @Override
                    public void run () {
                        for (Entry<Object, Object> e : bt.entrySet ()) {
                            if (accepts (threadNo, NTHREADS, e.getKey ())) {
                                String newValue = "TEST:" + threadNo;
                                e.setValue (newValue);
                            }
                        }
                    }
                });
            }

            es.shutdown ();
            try {
                es.awaitTermination (3600L, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace ();
            }
        }

        count = 0;
        for (final Map.Entry<Object, Object> kv : bt.entrySet ()) {
            Object value = kv.getValue ();
            TestHelper.assertTrue (value instanceof String);
            count++;
        }
        TestHelper.assertEquals (50000 + 2000 + 1000 + 100, count);

        final ConcurrentHashMap<Object, Object> removed = new ConcurrentHashMap<> ();

        {
            final ExecutorService es = Executors.newFixedThreadPool (NTHREADS);
            for (int i = 0; i < NTHREADS; i++) {
                final int threadNo = i;
                es.execute (new Runnable () {
                    @Override
                    public void run () {
                        for (final Iterator<Map.Entry<Object, Object>> i = bt.entrySet ().iterator (); i.hasNext ();) {
                            final Entry<Object, Object> e = i.next ();
                            Object key = e.getKey ();
                            if (accepts (threadNo, NTHREADS, key)) {
                                if (null == bt.get (key)) {
                                    System.out.println (key);
                                }
                                i.remove ();
                                if (null != bt.get (key)) {
                                    System.out.println (key);
                                }
                                removed.put (key, key);
                            }
                        }
                    }
                });
            }

            es.shutdown ();
            try {
                es.awaitTermination (3600L, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace ();
            }
        }

        count = 0;
        for (final Object value : bt.keySet ()) {
            value.toString ();
            count++;
        }
        for (final Object o : bt.keySet ()) {
            if (!removed.contains (bt.get (o))) {
                System.out.println ("Not removed: " + o);
            }
        }
        TestHelper.assertEquals (0, count);
        TestHelper.assertEquals (0, bt.size ());
        TestHelper.assertTrue (bt.isEmpty ());
    }

    protected static boolean accepts (final int threadNo, final int nThreads, final Object key) {
        int val = getKeyValue (key);
        if(val>=0) {
            return val % nThreads == threadNo;
        } else {
            return false;
        }
    }

    private static int getKeyValue (final Object key) {
        int val = 0;
        if (key instanceof Integer) {
            val = ((Integer) key).intValue ();
        }
        else if (key instanceof Character) {
            val = Math.abs (Character.getNumericValue ((Character) key) + 1);
        }
        else if (key instanceof Short) {
            val = ((Short) key).intValue () + 2;
        }
        else if (key instanceof Byte) {
            val = ((Byte) key).intValue () + 3;
        } else {
            return -1;
        }
        return val;
    }

    static Object[] getObjects (final int j) {
        final Collection<Object> results = new LinkedList<> ();
        results.add (Integer.valueOf (j));
        if (j < 2000) {
            results.add (Character.valueOf ((char) j));
        }
        if (j < 1000) {
            results.add (Short.valueOf ((short) j));
        }
        if (j < 100) {
            results.add (Byte.valueOf ((byte) j));
        }

        return results.toArray ();
    }
}

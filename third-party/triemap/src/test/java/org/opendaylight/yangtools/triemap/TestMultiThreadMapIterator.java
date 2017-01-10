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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMultiThreadMapIterator {
    private static final Logger LOG = LoggerFactory.getLogger(TestMultiThreadMapIterator.class);
    private static final int NTHREADS = 7;

    @Test
    public void testMultiThreadMapIterator() throws InterruptedException {
        final Map<Object, Object> bt = TrieMap.create();
        for (int j = 0; j < 50 * 1000; j++) {
            for (final Object o : getObjects(j)) {
                bt.put(o, o);
            }
        }

        LOG.debug("Size of initialized map is {}", bt.size());
        int count = 0;
        {
            final ExecutorService es = Executors.newFixedThreadPool(NTHREADS);
            for (int i = 0; i < NTHREADS; i++) {
                final int threadNo = i;
                es.execute(() -> {
                    for (Entry<Object, Object> e : bt.entrySet()) {
                        if (accepts(threadNo, NTHREADS, e.getKey())) {
                            String newValue = "TEST:" + threadNo;
                            e.setValue(newValue);
                        }
                    }
                });
            }

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);
        }

        count = 0;
        for (final Map.Entry<Object, Object> kv : bt.entrySet()) {
            assertTrue(kv.getValue() instanceof String);
            count++;
        }
        assertEquals(50000 + 2000 + 1000 + 100, count);

        final ConcurrentHashMap<Object, Object> removed = new ConcurrentHashMap<>();
        {
            final ExecutorService es = Executors.newFixedThreadPool(NTHREADS);
            for (int i = 0; i < NTHREADS; i++) {
                final int threadNo = i;
                es.execute(() -> {
                    for (final Iterator<Map.Entry<Object, Object>> it = bt.entrySet().iterator(); it.hasNext();) {
                        final Entry<Object, Object> e = it.next();
                        Object key = e.getKey();
                        if (accepts(threadNo, NTHREADS, key)) {
                            if (null == bt.get(key)) {
                                LOG.error("Key {} is not present", key);
                            }
                            it.remove();
                            if (null != bt.get(key)) {
                                LOG.error("Key {} is still present", key);
                            }
                            removed.put(key, key);
                        }
                    }
                });
            }

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);
        }

        count = 0;
        for (final Object value : bt.keySet()) {
            value.toString();
            count++;
        }
        for (final Object o : bt.keySet()) {
            if (!removed.contains(bt.get(o))) {
                LOG.error("Not removed: {}", o);
            }
        }
        assertEquals(0, count);
        assertEquals(0, bt.size());
        assertTrue(bt.isEmpty());
    }

    protected static boolean accepts(final int threadNo, final int nrThreads, final Object key) {
        final int val = getKeyValue(key);
        return val >= 0 ? val % nrThreads == threadNo : false;
    }

    private static int getKeyValue(final Object key) {
        if (key instanceof Integer) {
            return ((Integer) key).intValue();
        } else if (key instanceof Character) {
            return Math.abs(Character.getNumericValue((Character) key) + 1);
        } else if (key instanceof Short) {
            return ((Short) key).intValue() + 2;
        } else if (key instanceof Byte) {
            return ((Byte) key).intValue() + 3;
        } else {
            return -1;
        }
    }

    static Collection<Object> getObjects(final int j) {
        final Collection<Object> results = new ArrayList<>(4);
        results.add(Integer.valueOf(j));
        if (j < 2000) {
            results.add(Character.valueOf((char) j));
        }
        if (j < 1000) {
            results.add(Short.valueOf((short) j));
        }
        if (j < 100) {
            results.add(Byte.valueOf((byte) j));
        }

        return results;
    }
}

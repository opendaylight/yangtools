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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMultiThreadAddDelete {
    private static final Logger LOG = LoggerFactory.getLogger(TestMultiThreadAddDelete.class);
    private static final int RETRIES = 1;
    private static final int N_THREADS = 7;
    private static final int COUNT = 50 * 1000;

    @Test
    public void testMultiThreadAddDelete() throws InterruptedException {
        for (int j = 0; j < RETRIES; j++) {
            final Map<Object, Object> bt = TrieMap.create();

            {
                final ExecutorService es = Executors.newFixedThreadPool(N_THREADS);
                for (int i = 0; i < N_THREADS; i++) {
                    final int threadNo = i;
                    es.execute(() -> {
                        for (int k = 0; k < COUNT; k++) {
                            if (k % N_THREADS == threadNo) {
                                bt.put(Integer.valueOf(k), Integer.valueOf(k));
                            }
                        }
                    });
                }
                es.shutdown();
                es.awaitTermination(5, TimeUnit.MINUTES);
            }

            assertEquals(COUNT, bt.size());
            assertFalse(bt.isEmpty());

            {
                final ExecutorService es = Executors.newFixedThreadPool(N_THREADS);
                for (int i = 0; i < N_THREADS; i++) {
                    final int threadNo = i;
                    es.execute(() -> {
                        for (int k = 0; k < COUNT; k++) {
                            if (k % N_THREADS == threadNo) {
                                bt.remove(Integer.valueOf(k));
                            }
                        }
                    });
                }
                es.shutdown();
                es.awaitTermination(5, TimeUnit.MINUTES);
            }


            assertEquals(0, bt.size());
            assertTrue(bt.isEmpty());

            {
                final ExecutorService es = Executors.newFixedThreadPool(N_THREADS);
                for (int i = 0; i < N_THREADS; i++) {
                    final int threadNo = i;
                    es.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (int j = 0; j < COUNT; j++) {
                                if (j % N_THREADS == threadNo) {
                                    bt.put(Integer.valueOf(j), Integer.valueOf(j));
                                    if (!bt.containsKey(Integer.valueOf(j))) {
                                        LOG.error("Key {} not present", j);
                                    }
                                    bt.remove(Integer.valueOf(j));
                                    if (bt.containsKey(Integer.valueOf(j))) {
                                        LOG.error("Key {} is still present", j);
                                    }
                                }
                            }
                        }
                    });
                }
                es.shutdown();
                es.awaitTermination(5, TimeUnit.MINUTES);
            }

            assertEquals(0, bt.size());
            assertTrue(bt.isEmpty());
        }
    }
}

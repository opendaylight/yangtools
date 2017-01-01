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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class TestMultiThreadInserts {
    @Test
    public void testMultiThreadInserts () throws InterruptedException{
        final int nThreads = 2;
        final ExecutorService es = Executors.newFixedThreadPool(nThreads);
        final TrieMap<Object, Object> bt = new TrieMap<>();
        for (int i = 0; i < nThreads; i++) {
            final int threadNo = i;
            es.execute (() -> {
                for (int j = 0; j < 500 * 1000; j++) {
                    if (j % nThreads == threadNo) {
                        bt.put (Integer.valueOf (j), Integer.valueOf (j));
                    }
                }
            });
        }

        es.shutdown ();
        es.awaitTermination(5, TimeUnit.MINUTES);

        for (int j = 0; j < 500 * 1000; j++) {
            final Object lookup = bt.lookup (Integer.valueOf (j));
            assertEquals(Integer.valueOf(j), lookup);
        }
    }
}

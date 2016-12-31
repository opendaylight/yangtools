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

import org.junit.Test;

public class TestInstantiationSpeed {
    private static final int COUNT = 1000000;
    private static final int ITERATIONS = 10;
    private static final int WARMUP = 20;

    private static long runIteration() {
        final TrieMap<?, ?>[] maps = new TrieMap<?, ?>[COUNT];
        final long start = System.nanoTime();

        for (int i = 0; i < COUNT; ++i) {
            maps[i] = new TrieMap<>();
        }

        final long stop = System.nanoTime();
        return stop - start;
    }

    @Test
    public void testInstantiation() {

        for (int i = 0; i < WARMUP; ++i) {
            final long time = runIteration();
            System.out.println(String.format("Warmup %s took %sns (%sns)", i, time, time / COUNT));
        }

        long acc = 0;
        for (int i = 0; i < ITERATIONS; ++i) {
            final long time = runIteration();
            System.out.println(String.format("Iteration %s took %sns (%sns)", i, time, time / COUNT));
            acc += time;
        }

        System.out.println("Instantiation cost " + acc / ITERATIONS / COUNT + "ns");
    }
}

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

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInstantiationSpeed {
    private static final Logger LOG = LoggerFactory.getLogger(TestInstantiationSpeed.class);
    private static final int COUNT = 1000000;
    private static final int ITERATIONS = 10;
    private static final int WARMUP = 10;

    private static Stopwatch runIteration() {
        final TrieMap<?, ?>[] maps = new TrieMap<?, ?>[COUNT];

        final Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < COUNT; ++i) {
            maps[i] = new TrieMap<>();
        }
        watch.stop();

        // Do not allow optimizations
        LOG.trace("Maps: {}", (Object) maps);
        return watch;
    }

    private static long elapsedToNs(final Stopwatch watch) {
        return watch.elapsed(TimeUnit.NANOSECONDS) / COUNT;
    }

    @Ignore
    @Test
    public void testInstantiation() {

        for (int i = 0; i < WARMUP; ++i) {
            final Stopwatch time = runIteration();
            LOG.debug("Warmup {} took {} ({} ns)", i, time, elapsedToNs(time));
        }

        long acc = 0;
        for (int i = 0; i < ITERATIONS; ++i) {
            final Stopwatch time = runIteration();
            LOG.debug("Iteration {} took {} ({} ns)", i, time, elapsedToNs(time));
            acc += time.elapsed(TimeUnit.NANOSECONDS);
        }

        LOG.info("Instantiation cost {} ns", acc / ITERATIONS / COUNT);
    }
}

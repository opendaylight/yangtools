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

import java.util.Map;
import org.junit.Test;

public class TestHashCollisionsRemove {
    private static final int COUNT = 50000;

    @Test
    public void  testHashCollisionsRemove() {
        final Map<Object, Object> bt = new TrieMap<>();

        for (int j = 0; j < COUNT; j++) {
            for (final Object o : TestMultiThreadMapIterator.getObjects(j)) {
                bt.put(o, o);
            }
        }

        for (int j = 0; j < COUNT; j++) {
            for (final Object o : TestMultiThreadMapIterator.getObjects(j)) {
                bt.remove(o);
            }
        }

        assertEquals(0, bt.size());
        assertTrue(bt.isEmpty());
    }
}

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
import org.junit.Test;

public class TestHashCollisionsRemoveIterator {
    private static final int COUNT = 50000;

    @Test
    public void testHashCollisionsRemoveIterator() {
        final Map<Object, Object> bt = TrieMap.create();
        for (int j = 0; j < COUNT; j++) {
            bt.put(Integer.valueOf(j), Integer.valueOf(j));
        }

        final Collection<Object> list = new ArrayList<>(COUNT);
        final Iterator<Entry<Object, Object>> it = bt.entrySet().iterator();
        while (it.hasNext()) {
            list.add(it.next().getKey());
            it.remove();
        }

        assertEquals(0, bt.size());
        assertTrue(bt.isEmpty());
        assertEquals(COUNT, list.size());
    }
}

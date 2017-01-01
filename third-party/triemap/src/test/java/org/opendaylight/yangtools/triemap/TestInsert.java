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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestInsert {
    @Test
    public void testInsert () {
        final TrieMap<Object, Object> bt = new TrieMap<>();
        assertNull(bt.put("a", "a"));
        assertNull(bt.put("b", "b"));
        assertNull(bt.put("c", "b"));
        assertNull(bt.put("d", "b"));
        assertNull(bt.put("e", "b"));

        for (int i = 0; i < 10000; i++) {
            assertNull(bt.put(Integer.valueOf (i), Integer.valueOf(i)));
            final Object lookup = bt.lookup(Integer.valueOf(i));
            assertEquals(Integer.valueOf(i), lookup);
        }

        bt.toString();
    }
}

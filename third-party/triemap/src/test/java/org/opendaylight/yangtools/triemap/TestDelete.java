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

public class TestDelete {
    @Test
    public void testDelete () {
        final TrieMap<Object, Object> bt = new TrieMap<> ();

        for (int i = 0; i < 10000; i++) {
            TestHelper.assertEquals (null, bt.put (Integer.valueOf (i), Integer.valueOf (i)));
            final Object lookup = bt.lookup (Integer.valueOf (i));
            TestHelper.assertEquals (Integer.valueOf (i), lookup);
        }

        checkAddInsert (bt, 536);
        checkAddInsert (bt, 4341);
        checkAddInsert (bt, 8437);

        for (int i = 0; i < 10000; i++) {
            boolean removed = null != bt.remove(Integer.valueOf (i));
            TestHelper.assertEquals (Boolean.TRUE, Boolean.valueOf (removed));
            final Object lookup = bt.lookup (Integer.valueOf (i));
            TestHelper.assertEquals (null, lookup);
        }

        bt.toString ();
    }

    private static void checkAddInsert (final TrieMap<Object, Object> bt, final int k) {
        final Integer v = Integer.valueOf (k);
        bt.remove (v);
        Object foundV = bt.lookup (v);
        TestHelper.assertEquals (null, foundV);
        TestHelper.assertEquals (null, bt.put (v, v));
        foundV = bt.lookup (v);
        TestHelper.assertEquals (v, foundV);

        TestHelper.assertEquals (v, bt.put (v, Integer.valueOf (-1)));
        TestHelper.assertEquals (Integer.valueOf (-1), bt.put (v, v));
    }
}

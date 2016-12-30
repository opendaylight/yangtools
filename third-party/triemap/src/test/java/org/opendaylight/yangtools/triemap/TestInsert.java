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

public class TestInsert {
    @Test
    public void testInsert () {
        final TrieMap<Object, Object> bt = new TrieMap<> ();
        TestHelper.assertEquals (null, bt.put ("a", "a"));
        TestHelper.assertEquals (null, bt.put ("b", "b"));
        TestHelper.assertEquals (null, bt.put ("c", "b"));
        TestHelper.assertEquals (null, bt.put ("d", "b"));
        TestHelper.assertEquals (null, bt.put ("e", "b"));

        for (int i = 0; i < 10000; i++) {
            TestHelper.assertEquals (null, bt.put (Integer.valueOf (i), Integer.valueOf (i)));
            final Object lookup = bt.lookup (Integer.valueOf (i));
            TestHelper.assertEquals (Integer.valueOf (i), lookup);
        }

        bt.toString ();
    }
}

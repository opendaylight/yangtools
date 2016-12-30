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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class TestMapIterator {
    @Test
    public void testMapIterator () {
        for (int i = 0; i < 60 * 1000; i+= 400 + new Random ().nextInt (400)) {
            System.out.println (i);
            final Map<Integer, Integer> bt = new TrieMap <> ();
            for (int j = 0; j < i; j++) {
                TestHelper.assertEquals (null, bt.put (Integer.valueOf (j), Integer.valueOf (j)));
            }
            int count = 0;
            final Set<Integer> set = new HashSet<> ();
            for (final Map.Entry<Integer, Integer> e : bt.entrySet ()) {
                set.add (e.getKey ());
                count++;
            }
            for (final Integer j : set) {
                TestHelper.assertTrue (bt.containsKey (j));
            }
            for (final Integer j : bt.keySet ()) {
                TestHelper.assertTrue (set.contains (j));
            }

            TestHelper.assertEquals (i, count);
            TestHelper.assertEquals (i, bt.size ());

            for (Entry<Integer, Integer> e : bt.entrySet ()) {
                TestHelper.assertTrue (e.getValue () == bt.get (e.getKey ()));
                e.setValue (e.getValue () + 1);
                TestHelper.assertTrue (e.getValue () == e.getKey () + 1);
                TestHelper.assertTrue (e.getValue () == bt.get (e.getKey ()));
                e.setValue (e.getValue () - 1);
            }

            for (final Iterator<Integer> iter = bt.keySet ().iterator (); iter.hasNext ();) {
                final Integer k = iter.next ();
                TestHelper.assertTrue (bt.containsKey (k));
                iter.remove ();
                TestHelper.assertFalse (bt.containsKey (k));
            }

            TestHelper.assertEquals (0, bt.size ());
            TestHelper.assertTrue (bt.isEmpty ());
        }
    }
}

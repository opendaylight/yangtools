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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

public class TestHashCollisionsRemoveIterator {
    @Test
    public void testHashCollisionsRemoveIterator () {
        final Map<Object, Object> bt = new TrieMap<> ();
        int count = 50000;
        for (int j = 0; j < count; j++) {
            bt.put (Integer.valueOf (j), Integer.valueOf (j));
        }

        final Collection<Object> list = new ArrayList <> ();
        for (final Iterator<Map.Entry<Object, Object>> i = bt.entrySet ().iterator (); i.hasNext ();) {
            final Entry<Object, Object> e = i.next ();
            final Object key = e.getKey ();
            list.add (key);
            i.remove ();
        }

        TestHelper.assertEquals (0, bt.size ());
        TestHelper.assertTrue (bt.isEmpty ());
    }
}

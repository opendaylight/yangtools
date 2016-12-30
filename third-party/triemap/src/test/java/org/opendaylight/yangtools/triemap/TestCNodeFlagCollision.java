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

import java.util.Map;
import org.junit.Test;

public class TestCNodeFlagCollision {
    @Test
    public void testCNodeFlagCollision () {
        final Map<Object, Object> map = new TrieMap<> ();
        final Integer z15169 = Integer.valueOf (15169);
        final Integer z28336 = Integer.valueOf (28336);

        TestHelper.assertTrue (null == map.get (z15169));
        TestHelper.assertTrue (null == map.get (z28336));

        map.put (z15169, z15169);
        TestHelper.assertTrue (null != map.get (z15169));
        TestHelper.assertTrue (null == map.get (z28336));

        map.put (z28336, z28336);
        TestHelper.assertTrue (null != map.get (z15169));
        TestHelper.assertTrue (null != map.get (z28336));

        map.remove (z15169);

        TestHelper.assertTrue (null == map.get (z15169));
        TestHelper.assertTrue (null != map.get (z28336));

        map.remove (z28336);

        TestHelper.assertTrue (null == map.get (z15169));
        TestHelper.assertTrue (null == map.get (z28336));
    }
}

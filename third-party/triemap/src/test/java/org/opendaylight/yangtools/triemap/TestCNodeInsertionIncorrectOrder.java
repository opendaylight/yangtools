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

public class TestCNodeInsertionIncorrectOrder {

    @Test
    public void testCNodeInsertionIncorrectOrder () {
        final Map<Object, Object> map = new TrieMap<> ();
        final Integer z3884 = Integer.valueOf (3884);
        final Integer z4266 = Integer.valueOf (4266);
        map.put (z3884, z3884);
        TestHelper.assertTrue (null != map.get (z3884));

        map.put (z4266, z4266);
        TestHelper.assertTrue (null != map.get (z3884));
        TestHelper.assertTrue (null != map.get (z4266));
    }
}

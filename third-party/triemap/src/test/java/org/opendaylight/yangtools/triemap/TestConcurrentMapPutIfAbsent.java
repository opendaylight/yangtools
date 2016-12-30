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

import java.util.concurrent.ConcurrentMap;
import org.junit.Test;

public class TestConcurrentMapPutIfAbsent {
    private static final int COUNT = 50*1000;

    @Test
    public void testConcurrentMapPutIfAbsent () {
        final ConcurrentMap<Object, Object> map = new TrieMap<> ();

        for (int i = 0; i < COUNT; i++) {
            TestHelper.assertTrue (null == map.putIfAbsent (i, i));
            TestHelper.assertTrue (Integer.valueOf (i).equals (map.putIfAbsent (i, i)));
        }
    }
}

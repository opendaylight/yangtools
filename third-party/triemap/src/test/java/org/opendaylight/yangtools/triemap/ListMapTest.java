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

import static org.junit.Assert.assertFalse;

import java.util.Optional;
import org.junit.Test;

public class ListMapTest {

    /**
     * Test if Listmap.get() does not cause stack overflow.
     */
    @Test
    public void testGetOverflow() {
        ListMap<Integer, Boolean> map = ListMap.map(1, Boolean.TRUE, 2, Boolean.TRUE);

        // 30K seems to be enough to trigger the problem locally
        for (int i = 3; i < 30000; ++i) {
            map = map.add(Equivalence.equals(), i, Boolean.TRUE);
        }

        final Optional<Boolean> ret = map.get(Equivalence.equals(), 0);
        assertFalse(ret.isPresent());
    }

}

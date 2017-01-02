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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class TestSerialization {
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        TrieMap<String, String> map = TrieMap.create();

        map.put("dude-0", "tom");
        map.put("dude-1", "john");
        map.put("dude-3", "ravi");
        map.put("dude-4", "alex");

        TrieMap<String, String> expected = map.immutableSnapshot();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(expected);
        oos.close();

        final byte[] bytes = bos.toByteArray();
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bis);

        @SuppressWarnings("unchecked")
        final TrieMap<String, String> actual = (TrieMap<String, String>) ois.readObject();
        ois.close();

        Assert.assertEquals(expected, actual);
    }
}

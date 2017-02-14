/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.triemap.MutableTrieMap;
import org.opendaylight.yangtools.triemap.TrieMap;

public class ReadWriteTrieMapTest {

    @Test
    public void testMethodsOfReadWriteTrieMap() {
        final MutableTrieMap<String, String> trieMap = TrieMap.create();
        trieMap.put("0", "zero");
        trieMap.put("1", "one");

        final ReadWriteTrieMap<String, String> readWriteTrieMap = new ReadWriteTrieMap<>(trieMap, 5);

        assertNotNull("Object readOnlyTrieMap shouldn't be 'null'.", readWriteTrieMap);

        assertEquals("Size of readOnlyTrieMap should be '5'.", 5, readWriteTrieMap.size());
        assertFalse("Object readOnlyTrieMap shouldn't be empty.", readWriteTrieMap.isEmpty());

        assertTrue("Object readOnlyTrieMap should have key '0'.", readWriteTrieMap.containsKey("0"));
        assertTrue("Object readOnlyTrieMap should have value 'zero'.", readWriteTrieMap.containsValue("zero"));
        assertEquals("Object readOnlyTrieMap should have value 'zero'.", "zero", readWriteTrieMap.get("0"));

        final Map<String, String> rwMap = readWriteTrieMap;
        rwMap.put("2", "two");
        rwMap.put("3", "three");

        assertEquals("Removed value from readOnlyTrieMap should be 'one'.", "one", rwMap.remove("1"));

        final Set<String> trieMapKeySet = readWriteTrieMap.keySet();
        assertEquals("Size of keySet should be '3'.", 3, trieMapKeySet.size());

        final Collection<String> trieMapValues = readWriteTrieMap.values();
        assertEquals("Size of values should be '3'.", 3, trieMapValues.size());

        assertEquals(convertSetEntryToMap(readWriteTrieMap.entrySet()), trieMap);

        trieMap.put("2", "two");
        final ReadWriteTrieMap<String, String> readWriteTrieMap2 = new ReadWriteTrieMap<>(trieMap, 4);

        assertNotEquals(readWriteTrieMap, readWriteTrieMap2);
        assertEquals(readWriteTrieMap.hashCode(), readWriteTrieMap2.hashCode());

        final Map<String, String> readOnlyTrieMap = readWriteTrieMap.toReadOnly();
        readWriteTrieMap.clear();
        assertEquals(0, readWriteTrieMap.size());
        assertEquals(6, readOnlyTrieMap.size());
    }

    private static Map<String, String> convertSetEntryToMap(final Set<Entry<String, String>> input) {
        Map<String, String> resultMap = new HashMap<>();
        for (Entry<String, String> entry : input) {
            resultMap.put(entry.getKey(), entry.getValue());
        }
        return resultMap;
    }
}
